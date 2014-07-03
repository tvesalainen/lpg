/*
 * Copyright (C) 2012 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.Checksum;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.vesalainen.bcc.Block;
import org.vesalainen.bcc.IllegalConversionException;
import org.vesalainen.bcc.LookupList;
import org.vesalainen.bcc.MethodCompiler;
import org.vesalainen.bcc.SubClass;
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.ExecutableElementImpl.MethodBuilder;
import org.vesalainen.bcc.model.Jav;
import org.vesalainen.bcc.model.Typ;
import org.vesalainen.grammar.AnnotatedGrammar;
import org.vesalainen.grammar.GTerminal;
import org.vesalainen.grammar.Grammar;
import org.vesalainen.grammar.state.AmbiguousExpressionException;
import org.vesalainen.grammar.state.DFA;
import org.vesalainen.grammar.state.DFAState;
import org.vesalainen.grammar.state.IllegalExpressionException;
import org.vesalainen.grammar.state.NFA;
import org.vesalainen.grammar.state.NFAState;
import org.vesalainen.grammar.state.Scope;
import org.vesalainen.lpg.Item;
import org.vesalainen.lpg.Lr0State;
import org.vesalainen.lpg.State;
import static org.vesalainen.parser.ParserConstants.*;
import static org.vesalainen.parser.ParserFeature.*;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.RecoverMethod;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.annotation.TraceMethod;
import org.vesalainen.parser.util.ChecksumProvider;
import org.vesalainen.parser.util.Input;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parser.util.PeekableIterator;
import org.vesalainen.regex.MatchCompiler;
import org.vesalainen.util.EnumSetFlagger;
import org.vesalainen.util.HashMapSet;
import org.vesalainen.util.MapSet;

/**
 *
 * @author tkv
 */
public class ParserCompiler extends GenClassCompiler
{
    private final Grammar grammar;
    private final Map<Set<GTerminal>,Integer> inputMap = new HashMap<>();
    private final MapSet<Set<GTerminal>,State> inputSetUsageMap = new HashMapSet<>();
    private final Map<Integer,String> expectedMap = new HashMap<>();
    private ExecutableElement recoverMethod;
    private ExecutableElement traceMethod;
    private boolean implementsParserInfo;
    private final Set<ExecutableElement> implementedAbstractMethods = new HashSet<>();
    private int nextInput;

    private int lrkLevel;
    private final Jav jav = new Jav();

    /**
     * Creates a parser using grammar.
     * @param superClass Super class for parser. Possible parser annotations
     * are not processed.
     * @throws IOException
     */
    public ParserCompiler(TypeElement superClass) throws IOException
    {
        super(superClass);
        this.grammar = createGrammar(superClass);
        GrammarDef grammarDef = superClass.getAnnotation(GrammarDef.class);
        if (grammarDef != null)
        {
            lrkLevel = grammarDef.lrkLevel();
        }
    }
    private static Grammar createGrammar(TypeElement superClass) throws IOException
    {
        GrammarDef gDef = superClass.getAnnotation(GrammarDef.class);
        if (gDef == null)
        {
            throw new ParserException("@GrammarDef missing from "+superClass);
        }
        String grammar = gDef.grammar();
        if (grammar != null && !grammar.isEmpty())
        {
            throw new UnsupportedOperationException("@GrammarDef.grammar not supported");
        }
        return new AnnotatedGrammar(superClass);
    }
    @Override
    public void compile() throws IOException
    {
        super.compile();

        if (Typ.isAssignable(superClass.asType(), Typ.getTypeFor(ParserInfo.class)))
        {
            implementsParserInfo = true;
        }

        resolveRecoverAndTrace();
        overrideAbstractMethods();
        compileParseMethods(subClass);
        compileInputs();
        if (implementsParserInfo)
        {
            compileParserInfo();
        }
    }

    public int getLrkLevel()
    {
        return lrkLevel;
    }
    int getInputNumber(Set<GTerminal> inputSet, State state)
    {
        inputSetUsageMap.add(inputSet, state);
        Integer n = inputMap.get(inputSet);
        if (n == null)
        {
            nextInput++;
            StringBuilder sb = new StringBuilder();
            for (GTerminal t : inputSet)
            {
                String expression = t.getUnescapedExpression();
                if (expression == null)
                {
                    expression = t.getName();
                }
                if (sb.length() == 0)
                {
                    sb.append("\n  "+t.getName()+"="+expression+"\n");
                }
                else
                {
                    sb.append("| "+t.getName()+"="+expression+"\n");
                }
            }
            expectedMap.put(nextInput, sb.toString());
            inputMap.put(inputSet, nextInput);
            if (grammar.getTraceLevel() > 1)
            {
                System.err.println();
                System.err.println("State "+state.getNumber());
                System.err.println("uses input"+nextInput);
                System.err.println("Expects:");
                System.err.println(sb.toString());
                System.err.println();
            }
            return nextInput;
        }
        else
        {
            return n;
        }
    }
    private void compileParseMethods(SubClass subClass) throws IOException
    {
        for (final ExecutableElement method : ElementFilter.methodsIn(El.getAllMembers(superClass)))
        {
            final ParseMethod pm = method.getAnnotation(ParseMethod.class);
            if (pm != null)
            {   
                final EnumSet<ParserFeature> features = ParserFeature.get(pm);
                ExecutableType executableType = (ExecutableType) method.asType();
                final List<String> contextList = new ArrayList<>();
                final TypeMirror parseReturnType = method.getReturnType();
                final List<? extends VariableElement> parameters = method.getParameters();
                if (parameters.size() == 0)
                {
                    throw new IllegalArgumentException("@ParseMethod method "+method+" must have at least one parameter");
                }
                if (features.contains(LowerCase) && features.contains(UpperCase))
                {
                    throw new IllegalArgumentException("@ParseMethod method "+method+" has both lower- and upper-case set");
                }
                List<TypeMirror> parseParameters = new ArrayList<>();
                parseParameters.addAll(executableType.getParameterTypes());
                parseParameters.set(0, Typ.getTypeFor(InputReader.class));
                for (int ii=1;ii<parameters.size();ii++)
                {
                    ParserContext parserContext = parameters.get(ii).getAnnotation(ParserContext.class);
                    if (parserContext != null)
                    {
                        contextList.add(parserContext.value());
                    }
                    else
                    {
                        throw new IllegalArgumentException("extra not @ParserContext parameter in "+method);
                    }
                }
                
                String parserMethodname = jav.makeUniqueJavaIdentifier(PARSEMETHODPREFIX+pm.start());
                MethodBuilder builder = subClass.buildMethod(parserMethodname);
                builder.addModifier(Modifier.PRIVATE);
                builder.setReturnType(parseReturnType);
                for (TypeMirror t : parseParameters)
                {
                    builder.addParameter("")
                            .setType(t);
                }
                final ExecutableElement parseMethod = builder.getExecutableElement();
                if (!subClass.isImplemented(parseMethod))
                {
                    ParserMethodCompiler pmc = new ParserMethodCompiler(this, pm, contextList);
                    subClass.defineMethod(pmc, parseMethod);
                    features.addAll(pmc.getFeatures()); // update features detected while compiling
                }
                
                MethodCompiler mc = new MethodCompiler()
                {
                    @Override
                    protected void implement() throws IOException
                    {
                        nameArgument(IN, 1);
                        addVariable(InputSource, InputReader.class);
                        addVariable("$throwable", Throwable.class);
                        if (parseReturnType.getKind() != TypeKind.VOID)
                        {
                            addVariable("$ret", parseReturnType);
                        }
                        for (int ii=0;ii<contextList.size();ii++)
                        {
                            nameArgument(contextList.get(ii), ii+2);
                        }
                        tload(THIS);
                        if (Typ.isAssignable(parameters.get(0).asType(), Typ.getTypeFor(InputReader.class)))
                        {
                            tload(IN);
                        }
                        else
                        {
                            List<TypeMirror> pList = new ArrayList<>();
                            pList.add(parameters.get(0).asType());  // InputReader
                            if (pm.size() != -1)
                            {
                                pList.add(Typ.Int);                 // int size
                            }
                            if (!pm.charSet().isEmpty())
                            {
                                pList.add(Typ.String);              // String charSet
                            }
                            pList.add(El.getTypeElement(EnumSet.class.getCanonicalName()).asType());               // EnumSet<ParserFeatures> features
                            ExecutableElement irc = El.getMethod(El.getTypeElement(Input.class.getCanonicalName()), "getInstance", pList.toArray(new TypeMirror[pList.size()]));
                            if (irc == null)
                            {
                                throw new ParserException(method+" signature not compatible with any Input getInstance method\n"+
                                        "params:"+pList);
                            }
                            tload(IN);
                            if (pm.size() != -1)
                            {
                                iconst(pm.size());
                            }
                            if (!pm.charSet().isEmpty())
                            {
                                tconst(pm.charSet());
                            }
                            tconst(ParserFeature.class);
                            tconst(EnumSetFlagger.getFlag(features));
                            invokestatic(El.getMethod(EnumSetFlagger.class, "getSet", Class.class, int.class));
                            invoke(irc);
                        }
                        dup();
                        tstore(InputSource);
                        if (features.contains(UseChecksum))
                        {
                            if (!Typ.isAssignable(superClass.asType(), Typ.getTypeFor(ChecksumProvider.class)))
                            {
                                throw new IllegalArgumentException(UseChecksum+" without parser implementing ChecksumProvider");
                            }
                            if (features.contains(UsePushback))
                            {
                                throw new IllegalArgumentException(UseChecksum+" with "+UsePushback+" doesn't work");
                            }
                            tload(InputSource);
                            tload(This);
                            invoke(El.getMethod(ChecksumProvider.class, "getChecksum"));
                            invoke(El.getMethod(InputReader.class, "setChecksum", Checksum.class));
                        }
                        for (int ii=0;ii<contextList.size();ii++)
                        {
                            tload(contextList.get(ii));
                        }
                        Block tryBlock = startBlock();
                        invoke(parseMethod);
                        endBlock(tryBlock);
                        if (parseReturnType.getKind() != TypeKind.VOID)
                        {
                            tstore("$ret");
                        }
                        jsr("closeIt");
                        if (parseReturnType.getKind() != TypeKind.VOID)
                        {
                            tload("$ret");
                        }
                        treturn();
                        addExceptionHandler(tryBlock, "finallyHandler", Throwable.class);
                        fixAddress("finallyHandler");
                        tstore("$throwable");
                        jsr("closeIt");
                        tload("$throwable");
                        athrow();
                        startSubroutine("closeIt");
                        if (features.contains(UseAutoClose))
                        {
                            tload(InputSource);
                            invoke(El.getMethod(InputReader.class, "close"));
                        }
                        endSubroutine();
                    }
                };
                subClass.overrideMethod(mc, method, Modifier.PUBLIC);
                
            }
        }
    }

    private void compileParserInfo() throws IOException
    {
        compileGetToken();
        compileGetRule();
        compileGetExpected();
    }
    private void compileGetToken() throws IOException
    {
        MethodCompiler mc = new MethodCompiler()
        {
            @Override
            protected void implement() throws IOException
            {
                nameArgument(TOKEN, 1);
                LookupList list = new LookupList();
                for (String symbol : grammar.getSymbols())
                {
                    int number = grammar.getNumber(symbol);
                    list.addLookup(number, symbol.toString());
                }
                tload(TOKEN);
                optimizedSwitch("error", list);
                for (String symbol : grammar.getSymbols())
                {
                    fixAddress(symbol.toString());
                    ldc(symbol.toString());
                    treturn();
                }
                fixAddress("error");
                ldc("unknown token");
                treturn();
            }
        };
        subClass.defineMethod(mc, java.lang.reflect.Modifier.PUBLIC, GETTOKEN, String.class, int.class);
    }
    private void compileGetRule() throws IOException
    {
        MethodCompiler mc = new MethodCompiler()
        {
            @Override
            protected void implement() throws IOException
            {
                nameArgument(RULE, 1);
                LookupList list = new LookupList();
                Map<Integer,String> ruleDesc = grammar.getRuleDescriptions();
                for (int number : ruleDesc.keySet())
                {
                    list.addLookup(number, "rule-"+number);
                }
                tload(RULE);
                optimizedSwitch("error", list);
                for (int number : ruleDesc.keySet())
                {
                    fixAddress("rule-"+number);
                    ldc(ruleDesc.get(number));
                    treturn();
                }
                fixAddress("error");
                ldc("unknown rule");
                treturn();
            }
        };
        subClass.defineMethod(mc, java.lang.reflect.Modifier.PUBLIC, GETRULE, String.class, int.class);
    }
    private void compileGetExpected() throws IOException
    {
        MethodCompiler mc = new MethodCompiler()
        {
            @Override
            protected void implement() throws IOException
            {
                nameArgument(INPUT, 1);
                LookupList list = new LookupList();
                for (int number : expectedMap.keySet())
                {
                    list.addLookup(number, INPUT+number);
                }
                tload(INPUT);
                optimizedSwitch("error", list);
                for (int number : expectedMap.keySet())
                {
                    fixAddress(INPUT+number);
                    ldc(expectedMap.get(number));
                    treturn();
                }
                fixAddress("error");
                ldc("unknown input");
                treturn();
            }
        };
        subClass.defineMethod(mc, java.lang.reflect.Modifier.PUBLIC, GETEXPECTED, String.class, int.class);
    }

    private void compileInputs() throws IOException
    {
        for (Set<GTerminal> set : inputMap.keySet())
        {
            if (!set.isEmpty())
            {
                int inputNumber = inputMap.get(set);
                NFA<Integer> nfa = null;
                Scope<NFAState<Integer>> nfaScope = new Scope<>(INPUT+inputNumber);
                Scope<DFAState<Integer>> dfaScope = new Scope<>(INPUT+inputNumber);
                for (GTerminal terminal : set)
                {
                    if (terminal.getExpression() != null)
                    {
                        if (nfa == null)
                        {
                            nfa = terminal.createNFA(nfaScope);
                        }
                        else
                        {
                            NFA<Integer> nfa2 = terminal.createNFA(nfaScope);
                            nfa = new NFA(nfaScope, nfa, nfa2);
                        }
                    }
                }
                try
                {
                    if (nfa != null)
                    {
                        DFA dfa = nfa.constructDFA(dfaScope);
                        MatchCompiler<Integer> ic = new MatchCompiler<>(dfa, ERROR, EOF);
                        subClass.defineMethod(ic, java.lang.reflect.Modifier.PRIVATE, INPUT+inputNumber, int.class, InputReader.class);
                    }
                    else
                    {
                        EofCompiler ec = new EofCompiler();
                        subClass.defineMethod(ec, java.lang.reflect.Modifier.PRIVATE, INPUT+inputNumber, int.class, InputReader.class);
                    }
                }
                catch (AmbiguousExpressionException ex)
                {
                    String s1 = grammar.getSymbol((Integer)ex.getToken1());
                    String s2 = grammar.getSymbol((Integer)ex.getToken2());
                    throw new AmbiguousGrammarException("expression "+getExpected(inputNumber)+"used in "+getInputUsageFor(set)+" is ambiguous. conflicting symbols "+s1+" and "+s2, ex);
                }
                catch (IllegalExpressionException ex)
                {
                    throw new AmbiguousGrammarException("grammar is ambiguous "+set+" accepts same string "+ex.getMessage()+"used in "+getInputUsageFor(set), ex);
                }
            }
        }
    }
    String getInputUsageFor(Set<GTerminal> set) throws IOException
    {
        Set<State> stateSet = inputSetUsageMap.get(set);
        if (stateSet != null)
        {
            StringBuilder sb = new StringBuilder();
            for (State state : stateSet)
            {
                if (state instanceof Lr0State)
                {
                    Lr0State s = (Lr0State) state;
                    PeekableIterator<Item> ni  = s.getKernelItemsPtr();
                    while (ni.hasNext())
                    {
                        sb.append("\n");
                        Item i = ni.next();
                        i.print(sb);
                    }
                    ni  = s.getCompleteItemsPtr();
                    while (ni.hasNext())
                    {
                        sb.append("\n");
                        Item i = ni.next();
                        i.print(sb);
                    }
                }
            }
            sb.append("\n");
            return sb.toString();
        }
        else
        {
            throw new IllegalArgumentException("state for input set "+set+" not found");
        }
    }
    String getExpected(int inputNumber)
    {
        return expectedMap.get(inputNumber);
    }
    private void resolveRecoverAndTrace()
    {
        for (ExecutableElement m : ElementFilter.methodsIn(superClass.getEnclosedElements()))
        {
            if (m.getAnnotation(RecoverMethod.class) != null)
            {
                if (recoverMethod != null)
                {
                    throw new IllegalArgumentException("there can be only one @RecoverMethod");
                }
                recoverMethod = m;
            }
            if (m.getAnnotation(TraceMethod.class) != null)
            {
                if (traceMethod != null)
                {
                    throw new IllegalArgumentException("there can be only one @TraceMethod");
                }
                traceMethod = m;
            }
        }
    }
    /**
     * Implement abstract method which have either one parameter and returning something or
     * void type not returning anything.
     * @throws IOException
     */
    private void overrideAbstractMethods() throws IOException
    {
        for (final ExecutableElement method : El.getEffectiveMethods(superClass))
        {
            if (method.getModifiers().contains(Modifier.ABSTRACT))
            {
                if (
                        method.getAnnotation(Terminal.class) != null || 
                        method.getAnnotation(Rule.class) != null || 
                        method.getAnnotation(Rules.class) != null )
                {
                    implementedAbstractMethods.add(method);
                    MethodCompiler mc = new MethodCompiler()
                    {
                        @Override
                        protected void implement() throws IOException
                        {
                            TypeMirror returnType = method.getReturnType();
                            List<? extends VariableElement> params = method.getParameters();
                            if (returnType.getKind() != TypeKind.VOID && params.size() == 1)
                            {
                                nameArgument(ARG, 1);
                                try
                                {
                                    convert(ARG, returnType);
                                }
                                catch (IllegalConversionException ex)
                                {
                                    throw new IOException("bad conversion with "+method, ex);
                                }
                                treturn();
                            }
                            else
                            {
                                if (returnType.getKind() == TypeKind.VOID && params.size() == 0)
                                {
                                    treturn();
                                }
                                else
                                {
                                    throw new IllegalArgumentException("cannot implement abstract method "+method);
                                }
                            }
                        }
                    };
                    subClass.overrideMethod(mc, method, Modifier.PROTECTED);
                }
            }
        }
    }
    

    /**
     * Creates a byte code source file to dir. File content is similar to the
     * output of javap utility. Your IDE might be able to use this file for debugging
     * the actual byte code. (NetBeans can if this file located like java source
     * files)
     *
     * Example dir = c:\src class is foo.bar.Main Source file path is
     * c:\src\foo\bar\Main.jasm
     * @param env
     * @throws IOException
     */
    public void createSource(ProcessingEnvironment env) throws IOException
    {
        subClass.createSourceFile(env);
    }

    Grammar getGrammar()
    {
        return grammar;
    }

    ExecutableElement getRecoverMethod()
    {
        return recoverMethod;
    }

    boolean implementsParserInfo()
    {
        return implementsParserInfo;
    }

    boolean implementedAbstract(ExecutableElement reducer)
    {
        return implementedAbstractMethods.contains(reducer);
    }

    ExecutableElement getTraceMethod()
    {
        return traceMethod;
    }

}
