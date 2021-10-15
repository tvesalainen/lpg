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
import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.Block;
import org.vesalainen.bcc.LookupList;
import org.vesalainen.bcc.MethodCompiler;
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.ExecutableElementImpl.MethodBuilder;
import org.vesalainen.bcc.model.Typ;
import org.vesalainen.grammar.GRule;
import org.vesalainen.grammar.GTerminal;
import org.vesalainen.grammar.Grammar;
import org.vesalainen.grammar.GrammarException;
import org.vesalainen.grammar.Nonterminal;
import org.vesalainen.grammar.Symbol;
import org.vesalainen.lang.Primitives;
import org.vesalainen.lpg.Act;
import org.vesalainen.lpg.Action;
import org.vesalainen.lpg.Goto;
import org.vesalainen.lpg.LALRKParserGenerator;
import org.vesalainen.lpg.LaReduce;
import org.vesalainen.lpg.LaShift;
import org.vesalainen.lpg.LaState;
import org.vesalainen.lpg.Lr0State;
import org.vesalainen.lpg.Reduce;
import org.vesalainen.lpg.ReduceAct;
import org.vesalainen.lpg.Shift;
import org.vesalainen.lpg.ShiftReduceAct;
import org.vesalainen.lpg.State;
import org.vesalainen.lpg.TerminalAction;
import static org.vesalainen.parser.ParserConstants.*;
import static org.vesalainen.parser.ParserFeature.*;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.util.HtmlPrinter;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parser.util.NumSet;
import org.vesalainen.parser.util.Reducers;
import org.vesalainen.parser.util.SystemErrPrinter;

/**
 * ParserMethodCompiler class compiles Grammar into a Parser subclass.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class ParserMethodCompiler extends MethodCompiler
{
    private static final String RecoverMethod = "recover";
    // ParserInfo methods

    private final ParserCompiler parserCompiler;

    private Grammar g;
    private LALRKParserGenerator lrk;
    private List<Lr0State> lr0StateList;
    private List<LaState> laStateList;
    private TypeMirror parseReturnType;
    private final Deque<SubCompiler> compileQueue = new ArrayDeque<>();
    private final Set<String> compiledSet = new HashSet<>();
    private final List<String> contextList;
    private final Set<GTerminal> whiteSpaceSet = new NumSet<>();

    private boolean lineLocatorSupported;
    private boolean offsetLocatorSupported;
    private final ParseMethod parseMethod;
    private final EnumSet<ParserFeature> features;

    public ParserMethodCompiler(ParserCompiler parserCompiler, ParseMethod parseMethod, List<String> contextList)
    {
        this.parserCompiler = parserCompiler;
        this.parseMethod = parseMethod;
        this.contextList = contextList;
        this.features = ParserFeature.get(parseMethod);
        setWideIndex(features.contains(WideIndex));
    }

    public EnumSet<ParserFeature> getFeatures()
    {
        return features;
    }

    private String addCompilerRequest(SubCompiler comp)
    {
        if (!compiledSet.contains(comp.getLabel()))
        {
            compileQueue.add(comp);
            compiledSet.add(comp.getLabel());
        }
        return comp.getLabel();
    }

    @Override
    protected void implement() throws IOException
    {
        g = parserCompiler.getGrammar();
        parseReturnType = executableElement.getReturnType();
        List<? extends TypeMirror> thrownTypes = executableElement.getThrownTypes();
        try
        {
            lrk = g.getParserGenerator(parseMethod);
            ProcessingEnvironment env = parserCompiler.getProcessingEnvironment();
            if (env != null)
            {
                TypeElement thisClass = parserCompiler.getSubClass();
                DeclaredType superClass = (DeclaredType) subClass.getSuperclass();
                String simpleName = superClass.asElement().getSimpleName().toString();
                try (HtmlPrinter printer = new HtmlPrinter(env, thisClass, simpleName+"-"+parseMethod.start()+".html"))
                {
                    lrk.printAll(printer, env);
                }
                catch (FilerException ex)
                {
                    System.err.println(ex.getMessage());    // propably duplicate ?
                }
            }
        }
        catch (GrammarException ex)
        {
            g.print(System.err);
            throw ex;
        }
        for (GTerminal terminal : lrk.getTerminals())
        {
            if (terminal.isWhiteSpace())
            {
                whiteSpaceSet.add(terminal);
            }
            checkLocator(terminal.getReducerType());
        }
        for (Nonterminal nonterminal : lrk.getNonterminals())
        {
            checkLocator(nonterminal.getReducerType());
        }
        if (!whiteSpaceSet.isEmpty())
        {
            for (State state : lrk.getStateList())
            {
                state.getInputSet().addAll(whiteSpaceSet);
            }
        }
        
        for (int ii=0;ii<contextList.size();ii++)
        {
            nameArgument(contextList.get(ii), ii+2);
        }
        init();
        fixAddress("reset");
        reset();


        lr0StateList = lrk.getLr0StateList();
        laStateList = lrk.getLaStateList();
        // ----------- start --------------
        Block mainBlock = startBlock();
        fixAddress("start");

        load(TOKEN);
        ifge("afterShift");

        jsr("shiftSubroutine");
        fixAddress("afterShift");
        jsr("updateValueStack");

        compileStates();

        if (!thrownTypes.isEmpty())
        {
            addExceptionHandler(mainBlock, "bypassExceptionHandler", thrownTypes);
        }
        addExceptionHandler(mainBlock, "exceptionHandler", Exception.class);
        // after this point program control doesn't flow free. It is allowed to compile
        // independent subroutines after this
        // LA Start
        if (lrk.isLrk())
        {
            compileLaStates();
            compileUnread();
            compileLaReadInput();
        }

        //compileShift();
        compileShift();
        compileUpdateValueStack();
        compileProcessInput();
        compileSetCurrent();

        while (!compileQueue.isEmpty())
        {
            SubCompiler comp = compileQueue.pollFirst();
            comp.compile();
        }

        endBlock(mainBlock);
        // ----------- syntaxError --------------
        fixAddress("assert");
        fixAddress("syntaxError");
        if (parserCompiler.getRecoverMethod() == null)
        {
            if (parserCompiler.implementsParserInfo())
            {
                load(INPUTREADER);
                ExecutableElement recoverMethod = El.getMethod(InputReader.class, RecoverMethod, String.class, String.class);
                loadContextParameters(recoverMethod, 0);
                invokevirtual(recoverMethod);
            }
            else
            {
                load(INPUTREADER);
                ExecutableElement recoverMethod = El.getMethod(InputReader.class, RecoverMethod);
                invokevirtual(recoverMethod);
            }
        }
        else
        {
            load(THIS);
            loadContextParameters(parserCompiler.getRecoverMethod(), 0);
            invokevirtual(parserCompiler.getRecoverMethod());
        }
        goto_n("reset");

        if (!thrownTypes.isEmpty())
        {
            fixAddress("bypassExceptionHandler");
            athrow();
        }
        fixAddress("exceptionHandler");
        store(THROWABLE);
        if (parserCompiler.getRecoverMethod() == null)
        {
            load(INPUTREADER);
            load(THROWABLE);
            invokevirtual(El.getMethod(InputReader.class, RecoverMethod, Throwable.class));
        }
        else
        {
            load(THIS);
            loadContextParameters(parserCompiler.getRecoverMethod(), 0);
            invokevirtual(parserCompiler.getRecoverMethod());
            aconst_null();
            store(THROWABLE);
        }
        goto_n("reset");
    }
    private void init() throws IOException
    {
        nameArgument(INPUTREADER, 1);
        if (!features.contains(SingleThread))
        {
            // curTok
            addVariable(SP, int.class);
            addVariable(TOKEN, int.class);
            addVariable(CURTOK, int.class);
            addVariable(CURTYPE, int.class);

            int stackSize = Math.min(g.getMaxStack(), lrk.getStackSize()+lrk.getLrkLevel());
            assert stackSize > 0;
            addNewArray(STATESTACK, int[].class, stackSize);
            addNewArray(TYPESTACK, int[].class, stackSize);
            // value stack
            addNewArray(VALUESTACK, Object[].class, TypeKind.values().length);    // not all slots are used!

            for (TypeKind ot : lrk.getUsedTypes())
            {
                // value stack
                load(VALUESTACK);  // array
                iconst(Typ.getTypeNumber(ot));   // index
                newarray(Typ.getArrayType(Typ.normalizeType(ot)), stackSize);
                aastore();
                // curValue
                addVariable(CUR+ot.name(), Typ.normalizeType(ot));
                assignDefault(CUR+ot.name());
            }
            // LA init
            if (lrk.isLrk())
            {
                addVariable(LASTATE, int.class);
                addVariable(LATOKEN, int.class);
                addVariable(LALENGTH, int.class);
            }
            // locator stacks
            if (lineLocatorSupported || offsetLocatorSupported)
            {
                addNewArray(SOURCESTACK, String[].class, stackSize);
            }
            if (lineLocatorSupported)
            {
                addNewArray(LINESTACK, int[].class, stackSize);
                addNewArray(COLUMNSTACK, int[].class, stackSize);
            }
            if (offsetLocatorSupported)
            {
                addNewArray(OFFSETSTACK, int[].class, stackSize);
            }
            addVariable(THROWABLE, Throwable.class);
            assignDefault(THROWABLE);
            if (parserCompiler.implementsParserInfo())
            {
                addVariable(RuleDescription, String.class);
                assignDefault(RuleDescription);
                addVariable(ExpectedDescription, String.class);
                assignDefault(ExpectedDescription);
                addVariable(LastToken, String.class);
                assignDefault(LastToken);
            }
        }
    }
    private void reset() throws IOException
    {
        iconst(-1);
        store(TOKEN);
        iconst(-1);
        store(CURTOK);
        iconst(Typ.getTypeNumber(TypeKind.VOID));
        store(CURTYPE);
        if (lrk.isLrk())
        {
            iconst(0);
            store(LASTATE);
            iconst(-1);
            store(LATOKEN);
            iconst(0);
            store(LALENGTH);
        }
        iconst(-1);
        store(SP);

        push(1);
    }
    private void compileShift() throws IOException
    {
        String bend = createBranch();
        startSubroutine("shiftSubroutine");
        fixAddress("shiftStart");
        LookupList inputAddresses = new LookupList();
        Set<Integer> targetSet = new LinkedHashSet<>();
        for (State state : lr0StateList)
        {
            Set<GTerminal> inputSet = state.getInputSet();
            assert !inputSet.isEmpty();
            int inputNumber = parserCompiler.getInputNumber(inputSet, state);
            String target = INPUT+inputNumber;
            inputAddresses.addLookup(state.getNumber(), target);
            targetSet.add(inputNumber);
        }
        trace(Trace.STATE, -1);
        load(STATESTACK);
        load(SP);
        iaload();
        optimizedSwitch(inputAddresses);
        for (Integer target : targetSet)
        {
        // ----------- input999 --------------
            fixAddress(INPUT+target);
            setLocation();
            load(THIS);
            load(INPUTREADER);
            MethodBuilder builder = subClass.buildMethod(INPUT+target);
            builder.setReturnType(int.class);
            builder.addParameter("a1").setType(InputReader.class);
            invokespecial(builder.getExecutableElement());
            store(TOKEN);
            trace(Trace.INPUT, target);
            load(TOKEN);
            ifge(bend);
            if (parserCompiler.implementsParserInfo())
            {
                // store expected and token info
                ldc(parserCompiler.getExpected(target));
                store(ExpectedDescription);
                load(THIS);
                load(TOKEN);
                builder = subClass.buildMethod(GETTOKEN);
                builder.setReturnType(String.class);
                builder.addParameter("a1").setType(int.class);                    
                invokespecial(builder.getExecutableElement());
                store(LastToken);
            }
            goto_n("syntaxError");
        }
        fixAddress(bend);

        load(TOKEN);
        store(CURTOK);

        bend = createBranch();
        LookupList terminalNumbers = new LookupList();
        terminalNumbers.addLookup(0, bend); // Eof
        for (GTerminal t : lrk.getTerminals())
        {
            if (t.getExpression() != null)
            {
                terminalNumbers.addLookup(t.getNumber(), "$term-"+t.toString());
            }
        }
        load(CURTOK);
        optimizedSwitch(terminalNumbers);
        for (GTerminal t : lrk.getTerminals())
        {
        // ----------- terminal --------------
            if (t.getExpression() != null)
            {
                fixAddress("$term-"+t.toString());
                ExecutableElement reducer = t.getReducer();
                if (reducer != null)
                {
                    // if terminal reducer was abstract, there is no need to call
                    // it. These methods have no effect on stack
                    if (!parserCompiler.implementedAbstract(reducer))
                    {
                        load(THIS);
                    }
                    // anyway we have to do the type conversion on stack
                    List<? extends VariableElement> params = reducer.getParameters();
                    TypeMirror returnType = reducer.getReturnType();
                    if (params.size() > 0)
                    {
                        if (params.get(0).getAnnotation(ParserContext.class) != null)
                        {
                            // Terminal value is omitted but @ParserContext is present
                            loadContextParameters(reducer, 0);
                        }
                        else
                        {
                            load(INPUTREADER);
                            TypeMirror paramType = params.get(0).asType();
                            if (!Typ.isAssignable(Typ.getTypeFor(InputReader.class), paramType))
                            {
                                // if param[0] is not InputReader or CharSequence -> convert
                                ExecutableElement convertMethod;
                                if (Typ.isPrimitive(paramType))
                                {
                                    String typeName = paramType.getKind().name().toLowerCase();
                                    String methodName = "parse"+typeName.toUpperCase().substring(0, 1)+typeName.substring(1);
                                    int radix = t.getBase();
                                    boolean signed = t.isSigned();
                                    if (radix == -1)
                                    {
                                        convertMethod = El.getMethod(Primitives.class, methodName, CharSequence.class);
                                        if (convertMethod == null)
                                        {
                                            throw new IllegalArgumentException(Primitives.class.getCanonicalName()+"."+methodName+"(java.lang.CharSequence) not found");
                                        }
                                        invokestatic(convertMethod);
                                    }
                                    else
                                    {
                                        convertMethod = El.getMethod(Primitives.class, methodName, CharSequence.class, int.class, boolean.class);
                                        if (convertMethod == null)
                                        {
                                            throw new IllegalArgumentException(Primitives.class.getCanonicalName()+"."+methodName+"(java.lang.CharSequence, int, boolean) not found");
                                        }
                                        tconst(radix);
                                        tconst(signed);
                                        invokestatic(convertMethod);
                                    }
                                }
                                else
                                {
                                    if (Typ.isSameType(paramType, Typ.String))
                                    {
                                        convertMethod = El.getMethod(InputReader.class, "getString");
                                        invokevirtual(convertMethod);
                                    }
                                    else
                                    {
                                        throw new IllegalArgumentException("no parse method for non primitive type "+paramType+" at "+t);
                                    }
                                }
                            }
                            loadContextParameters(reducer, 1);
                        }
                    }
                    if (!parserCompiler.implementedAbstract(reducer))
                    {
                        invokevirtual(reducer);
                    }
                    // if type was other than void we are storing the result in
                    // local variable CURxxx
                    TypeMirror normalizedType = Typ.normalizeType(returnType.getKind());
                    TypeKind ot = normalizedType.getKind();
                    if (ot != TypeKind.VOID)
                    {
                        callSetLocation(returnType);
                        store(CUR+ot.name());
                    }
                    setCurrentType(Typ.getTypeNumber(ot));
                }
                goto_n(bend);
            }
        }
        fixAddress(bend);
        load(INPUTREADER);
        invokevirtual(El.getMethod(InputReader.class, "clear"));
        if (!whiteSpaceSet.isEmpty())
        {
            LookupList wspList = new LookupList();
            for (GTerminal wsp : whiteSpaceSet)
            {
                ExecutableElement reducer = wsp.getReducer();
                if (reducer != null && reducer.getReturnType().getKind() != TypeKind.VOID)
                {
                    wspList.addLookup(wsp.getNumber(), wsp+"-shiftInsert");
                }
                else
                {
                    wspList.addLookup(wsp.getNumber(), "shiftStart");
                }
            }
            load(TOKEN);
            optimizedSwitch("wspContinue", wspList);
            for (GTerminal wsp : whiteSpaceSet)
            {
                ExecutableElement reducer = wsp.getReducer();
                
                if (reducer != null)
                {
                    TypeMirror returnType = reducer.getReturnType();
                    TypeMirror normalizedType = Typ.normalizeType(returnType.getKind());
                    if (returnType.getKind() != TypeKind.VOID)
                    {
                        fixAddress(wsp+"-shiftInsert");
                        TypeKind ot = normalizedType.getKind();
                        load(INPUTREADER);
                        load(CUR+ot.name());
                        ExecutableElement insertMethod = El.getMethod(El.getTypeElement(InputReader.class.getCanonicalName()), "insert", returnType);
                        if (insertMethod == null)
                        {
                            throw new IllegalArgumentException("method "+InputReader.class.getCanonicalName()+".insert("+returnType+") not found");
                        }
                        checkcast(insertMethod.getParameters().get(0).asType());
                        invokevirtual(insertMethod);
                        features.add(UsePushback);
                        goto_n("shiftStart");
                    }
                }
            }
            fixAddress("wspContinue");
        }
        endSubroutine();
    }

    private void loadContextParameters(ExecutableElement reducer, int start) throws IOException
    {
        List<? extends VariableElement> parameters = reducer.getParameters();
        for (int ii=start;ii < parameters.size();ii++)
        {
            ParserContext parserContext = parameters.get(ii).getAnnotation(ParserContext.class);
            if (parserContext != null)
            {
                load(parserContext.value());
            }
            else
            {
                if (Typ.isAssignable(parameters.get(ii).asType(), Typ.getTypeFor(InputReader.class)))
                {
                    load(INPUTREADER);
                }
                else
                {
                    throw new IllegalArgumentException("reducer "+reducer+" has extra parameters which are not @ParserContext");
                }
            }
        }
    }
    private void compileUpdateValueStack() throws IOException
    {
        startSubroutine("updateValueStack");
        LookupList ll = new LookupList();
        ll.addLookup(Typ.getTypeNumber(TypeKind.VOID), "setCurrent-Void");
        for (TypeKind ot : lrk.getUsedTypes())
        {
            // value stack
            ll.addLookup(Typ.getTypeNumber(ot), ot+"-cur");
        }
        getCurrentType();
        optimizedSwitch(ll);
        for (TypeKind ot : lrk.getUsedTypes())
        {
            // value stack
            fixAddress(ot+"-cur");

            load(TYPESTACK);
            load(SP);
            load(CURTYPE);
            iastore();

            load(VALUESTACK);  // valueStack
            getCurrentType();       // valueStack curType
            aaload();             // stackXXX
            checkcast(Typ.getArrayType(Typ.normalizeType(ot)));
            load(SP);          // stackXXX spXXX 
            load(CUR+ot.name());   // stackXXX spXXX curXXX
            tastore(Typ.normalizeType(ot));
            goto_n("setCurrent-Exit");
        }
        fixAddress("setCurrent-Void");

        load(TYPESTACK);
        load(SP);
        iconst(Typ.getTypeNumber(TypeKind.VOID));
        iastore();

        fixAddress("setCurrent-Exit");
        trace(Trace.PUSHVALUE, -1);

        endSubroutine();
    }

    private void compileLaReadInput() throws IOException
    {
        String bend = createBranch();
        startSubroutine("readLaInputSubroutine");
        fixAddress("laReadStart");
        LookupList inputAddresses = new LookupList();
        Set<Integer> targetSet = new LinkedHashSet<>();
        for (State state : laStateList)
        {
            Set<GTerminal> inputList = state.getInputSet();
            assert !inputList.isEmpty();
            int inputNumber = parserCompiler.getInputNumber(inputList, state);
            String target = LAINPUT+inputNumber;
            inputAddresses.addLookup(state.getNumber(), target);
            targetSet.add(inputNumber);
        }
        load(LASTATE);
        optimizedSwitch(inputAddresses);
        for (Integer target : targetSet)
        {
        // ----------- input999 --------------
            fixAddress(LAINPUT+target);
            setLocation();
            load(THIS);
            load(INPUTREADER);
            MethodBuilder builder = subClass.buildMethod(INPUT+target);
            builder.setReturnType(int.class);
            builder.addParameter("a1").setType(InputReader.class);
            invokevirtual(builder.getExecutableElement());
            store(LATOKEN);
            // La token buffer
            load(INPUTREADER);
            invokevirtual(El.getMethod(InputReader.class, "getLength"));
            load(LALENGTH);
            iadd();
            store(LALENGTH);
            trace(Trace.LAINPUT, target);
            load(INPUTREADER);
            invokevirtual(El.getMethod(InputReader.class, "clear"));

            load(LATOKEN);
            ifge(bend);
            if (parserCompiler.implementsParserInfo())
            {
                // store expected and token info
                ldc(parserCompiler.getExpected(target));
                store(ExpectedDescription);
                load(THIS);
                load(LATOKEN);
                builder = subClass.buildMethod(GETTOKEN);
                builder.setReturnType(String.class);
                builder.addParameter("a1").setType(int.class);                    
                invokespecial(builder.getExecutableElement());
                store(LastToken);
            }
            goto_n("syntaxError");
            // till here
        }
        fixAddress(bend);
        if (!whiteSpaceSet.isEmpty())
        {
            LookupList wspList = new LookupList();
            for (GTerminal wsp : whiteSpaceSet)
            {
                ExecutableElement reducer = wsp.getReducer();
                if (reducer != null && reducer.getReturnType().getKind() != TypeKind.VOID)
                {
                    wspList.addLookup(wsp.getNumber(), wsp+"-laReadInsert");
                }
                else
                {
                    wspList.addLookup(wsp.getNumber(), "laReadStart");
                }
            }
            load(LATOKEN);
            optimizedSwitch("laWspContinue", wspList);
            for (GTerminal wsp : whiteSpaceSet)
            {
                ExecutableElement reducer = wsp.getReducer();
                if (reducer != null)
                {
                    TypeMirror returnType = reducer.getReturnType();
                    TypeMirror normalizedType = Typ.normalizeType(returnType.getKind());
                    if (returnType.getKind() != TypeKind.VOID)
                    {
                        fixAddress(wsp+"-laReadInsert");
                        TypeKind ot = normalizedType.getKind();
                        load(INPUTREADER);
                        load(CUR+ot.name());
                        ExecutableElement insertMethod = El.getMethod(El.getTypeElement(InputReader.class.getCanonicalName()), "insert", returnType);
                        if (insertMethod == null)
                        {
                            throw new IllegalArgumentException("method "+InputReader.class.getCanonicalName()+".insert("+returnType+") not found");
                        }
                        checkcast(insertMethod.getParameters().get(0).asType());
                        invokevirtual(insertMethod);
                        features.add(UsePushback);
                        goto_n("laReadStart");
                    }
                }
            }
            fixAddress("laWspContinue");
        }
        endSubroutine();
    }

    private void compileShiftAction(Shift shift) throws IOException, NoSuchMethodException, NoSuchFieldException
    {
        GTerminal symbol = shift.getSymbol();
        Action action = shift.getAction();
        if (action instanceof Lr0State)
        {
            Lr0State lr0State = (Lr0State) action;
            push(lr0State.getNumber());
            iconst(-1);
            store(TOKEN);
            trace(Trace.SHIFT, lr0State.getNumber());
            goto_n("start");    // shift to state
        }
        else
        {
            if (action instanceof GRule)
            {
                // Shift/Reduce
                GRule rule = (GRule) action;
                trace(Trace.SHRD, rule.getNumber());
                trace(Trace.BEFOREREDUCE, rule.getOriginalNumber());
                inc(SP, 1);
                String t = addCompilerRequest(new ReductSubCompiler(rule));
                jsr(t);   // shift/reduce
                trace(Trace.AFTERREDUCE, rule.getOriginalNumber());
                iconst(-1);
                store(TOKEN);
                Nonterminal nt = rule.getLeft();
                if (!nt.isStart())
                {
                    String t2 = addCompilerRequest(new GotoCompiler(nt));
                    goto_n(t2);
                }
                else
                {
                    assert false;   //
                }
            }
            else
            {
                if (action instanceof LaState)
                {
                    LaState state = (LaState) action;
                    trace(Trace.LASHIFT, state.getNumber());
                    iconst(state.getNumber());
                    store(LASTATE);
                    jsr("readLaInputSubroutine");
                    goto_n("laStateStart");    // shift to state
                }
                else
                {
                    assert false;
                }
            }
        }
    }

    private void compileReduceAction(Reduce reduce) throws IOException
    {
        GRule rule = reduce.getRule();
        String target = addCompilerRequest(new ReductCompiler(rule));
        goto_n(target);
    }

    private void compileLaShiftAction(LaShift laShift) throws IOException, NoSuchMethodException, NoSuchFieldException
    {
        Act act = laShift.getAct();
        if (act instanceof LaState)
        {
            // La Shift
            LaState state = (LaState) act;
            trace(Trace.LASHIFT, state.getNumber());
            iconst(state.getNumber());
            store(LASTATE);
            jsr("readLaInputSubroutine");
            goto_n("laStateStart");    // shift to state
        }
        else
        {
            if (act instanceof ShiftReduceAct)
            {
                // La Sh/Rd
                ShiftReduceAct ract = (ShiftReduceAct) act;
                GRule rule = ract;
                trace(Trace.LASHRD, rule.getNumber());
                jsr("updateValueStack");
                trace(Trace.BEFOREREDUCE, rule.getOriginalNumber());
                inc(SP, 1);
                String target = addCompilerRequest(new ReductSubCompiler(rule));
                jsr(target);   // shift/reduce
                //inc(SP, 1);
                trace(Trace.AFTERREDUCE, rule.getOriginalNumber());
                jsr("unreadSubroutine");
                load(INPUTREADER);
                invokevirtual(El.getMethod(InputReader.class, "clear"));
                iconst(-1);
                store(TOKEN);
                Nonterminal nt = rule.getLeft();
                if (!nt.isStart())
                {
                    String t2 = addCompilerRequest(new GotoCompiler(nt));
                    goto_n(t2);
                }
                else
                {
                    assert false;   //
                }
            }
            else
            {
                if (act instanceof Lr0State)
                {
                    // Shift
                    Lr0State lr0State = (Lr0State) act;
                    trace(Trace.GOTOLA2LR, lr0State.getNumber());
                    jsr("unreadSubroutine");
                    load(INPUTREADER);
                    invokevirtual(El.getMethod(InputReader.class, "clear"));
                    push(lr0State.getNumber());
                    iconst(-1);
                    store(TOKEN);
                    goto_n("start");    // shift to state
                }
                else
                {
                    assert false;
                }
            }
        }
    }

    private void compileLaReduceAction(LaReduce laReduce) throws IOException, NoSuchMethodException, NoSuchFieldException
    {
        Act act = laReduce.getAct();
        if (act instanceof ReduceAct)
        {
            GRule rule = (GRule) act;
            exitLa();
            String target = addCompilerRequest(new ReductCompiler(rule));
            goto_n(target);   // reduce
        }
        else
        {
            throw new UnsupportedOperationException("not supported yet");
        }
    }

    private void compileStates() throws IOException
    {
        trace(Trace.STATE, -1);
        load(STATESTACK);
        load(SP);
        iaload();
        List<String> stateTargets = new ArrayList<>();
        for (State state : lr0StateList)
        {
            stateTargets.add(state.toString());
        }
        tableswitch(1, lr0StateList.size(), stateTargets);

        for (Lr0State state : lr0StateList)
        {
            fixAddress(state.toString());
            Set<GTerminal> inputList = state.getInputSet();
            assert !inputList.isEmpty();
            LookupList lookupList = new LookupList();
            StringBuilder expected = new StringBuilder();

            //assert state.getDefaultReduce() == null;

            for (Shift shift : state.getShiftList())
            {
                String target = addCompilerRequest(new TerminalActionCompiler(shift));
                lookupList.addLookup(shift.getSymbol().getNumber(), target);
                //terminalActionSet.add(shift);
                if (expected.length() == 0)
                {
                    expected.append("\n  "+shift.getSymbol());
                }
                else
                {
                    expected.append("\n| "+shift.getSymbol());
                }
            }
            for (Reduce reduce : state.getReduceList())
            {
                String target = addCompilerRequest(new TerminalActionCompiler(reduce));
                lookupList.addLookup(reduce.getSymbol().getNumber(), target);
                //terminalActionSet.add(reduce);
                if (expected.length() == 0)
                {
                    expected.append("\n  "+reduce.getSymbol());
                }
                else
                {
                    expected.append("\n| "+reduce.getSymbol());
                }
            }
            load(CURTOK);
            optimizedSwitch(state+"syntaxError", lookupList);
            fixAddress(state+"syntaxError");
            if (parserCompiler.getRecoverMethod() == null)
            {
                if (parserCompiler.implementsParserInfo())
                {
                    load(INPUTREADER);
                    ldc(expected.toString());
                    load(THIS);
                    load(TOKEN);
                    MethodBuilder builder = subClass.buildMethod(GETTOKEN);
                    builder.setReturnType(String.class);
                    builder.addParameter("a1").setType(int.class);                    
                    invokespecial(builder.getExecutableElement());
                    invokevirtual(El.getMethod(InputReader.class, RecoverMethod, String.class, String.class));
                }
                else
                {
                    load(INPUTREADER);
                    invokevirtual(El.getMethod(InputReader.class, RecoverMethod));
                }
            }
            else
            {
                load(THIS);
                loadContextParameters(parserCompiler.getRecoverMethod(), 0);
                invokevirtual(parserCompiler.getRecoverMethod());
            }
            goto_n("syntaxError");
        }
    }

    private void compileLaStates() throws IOException
    {
        fixAddress("laStateStart");
        load(LASTATE);
        List<String> stateTargets = new ArrayList<>();
        for (State state : laStateList)
        {
            stateTargets.add(state.toString());
        }
        int first = laStateList.get(0).getNumber();
        tableswitch(first, first+laStateList.size()-1, stateTargets);

        for (LaState state : laStateList)
        {
            fixAddress(state.toString());
            Set<GTerminal> inputSet = state.getInputSet();
            if (!inputSet.isEmpty())
            {
                load(LATOKEN);
                LookupList lookupList = new LookupList();

                assert state.getDefaultRule() == null;
                for (LaShift shift : state.getShiftList())
                {
                    String target = addCompilerRequest(new TerminalActionCompiler(shift));
                    lookupList.addLookup(shift.getSymbol().getNumber(), target);
                    //terminalActionSet.add(shift);
                }
                for (LaReduce reduce : state.getReduceList())
                {
                    String target = addCompilerRequest(new TerminalActionCompiler(reduce));
                    lookupList.addLookup(reduce.getSymbol().getNumber(), target);
                    //terminalActionSet.add(reduce);
                }
                optimizedSwitch("syntaxError", lookupList);
            }
            else
            {
                exitLa();
                //inc(SP, -1);
                goto_n("start");
            }
        }
    }

    private void setCurrentType(int type) throws IOException
    {
        iconst(type);
        store(CURTYPE);
    }

    private void getCurrentType() throws IOException
    {
        load(CURTYPE);
    }

    private void compileSetCurrent() throws IOException
    {
    }


    private void trace(Trace action, int ctx) throws IOException
    {
        if (parserCompiler.getTraceMethod() != null)
        {
            List<? extends VariableElement> parameters = parserCompiler.getTraceMethod().getParameters();
            if (
                    parameters.size() >= 2 &&
                    parameters.get(0).asType().getKind() == TypeKind.INT &&
                    parameters.get(1).asType().getKind() == TypeKind.INT &&
                    parserCompiler.getTraceMethod().getReturnType().getKind() == TypeKind.VOID
                    )
            {
                load(THIS);
                iconst(action.ordinal());
                iconst(ctx);
                loadContextParameters(parserCompiler.getTraceMethod(), 2);
                invokevirtual(parserCompiler.getTraceMethod());
            }
            else
            {
                throw new IllegalArgumentException(parserCompiler.getTraceMethod()+" signature is not xxx(int trace, int ctx, ...");
            }
        }
    }

    public LALRKParserGenerator getLrk()
    {
        return lrk;
    }

    private void push(int state) throws IOException
    {
        inc(SP, 1);
        load(STATESTACK);
        load(SP);
        iconst(state);
        iastore();
    }

    private void exitLa() throws IOException
    {
        iconst(0);
        store(LASTATE);

        trace(Trace.EXITLA, -1);
        jsr("unreadSubroutine");

        load(INPUTREADER);
        invokevirtual(El.getMethod(InputReader.class, "clear"));
    }

    private void compileReset() throws IOException
    {
        startSubroutine("resetSubroutine");
        load(INPUTREADER);
        invokevirtual(El.getMethod(InputReader.class, "clear"));
        endSubroutine();
    }

    private void compileUnread() throws IOException
    {
        startSubroutine("unreadSubroutine");
        load(INPUTREADER);
        load(LALENGTH);
        invokevirtual(El.getMethod(InputReader.class, "unreadLa", int.class));
        iconst(0);
        store(LALENGTH);
        endSubroutine();
    }

    private void compileProcessInput() throws IOException
    {
    }

    private void checkLocator(TypeMirror rt)
    {
        if (Typ.isAssignable(rt, Typ.getTypeFor(ParserLineLocator.class)))
        {
            lineLocatorSupported = true;
        }
        if (Typ.isAssignable(rt, Typ.getTypeFor(ParserOffsetLocator.class)))
        {
            offsetLocatorSupported = true;
        }
    }
    private void setLocation() throws IOException
    {
        if (lineLocatorSupported)
        {
            load(SOURCESTACK);
            load(SP);
            load(INPUTREADER);
            invokevirtual(El.getMethod(InputReader.class, "getSource"));
            aastore();
            
            load(LINESTACK);
            load(SP);
            load(INPUTREADER);
            invokevirtual(El.getMethod(InputReader.class, "getLineNumber"));
            iastore();
            
            load(COLUMNSTACK);
            load(SP);
            load(INPUTREADER);
            invokevirtual(El.getMethod(InputReader.class, "getColumnNumber"));
            iastore();
        }
        if (offsetLocatorSupported)
        {
            load(SOURCESTACK);
            load(SP);
            load(INPUTREADER);
            invokevirtual(El.getMethod(InputReader.class, "getSource"));
            aastore();
            
            load(OFFSETSTACK);
            load(SP);
            load(INPUTREADER);
            invokevirtual(El.getMethod(InputReader.class, "getStart"));
            iastore();
            
        }
    }

    private void callSetLocation(TypeMirror returnType) throws IOException
    {
        if (lineLocatorSupported)
        {
            if (Typ.isAssignable(returnType, Typ.getTypeFor(ParserOffsetLocator.class)))
            {
                dup();
                String branch = createBranch();
                ifnull(branch);
                dup();
                load(SOURCESTACK);
                load(SP);
                aaload();
                load(LINESTACK);
                load(SP);
                iaload();
                load(COLUMNSTACK);
                load(SP);
                iaload();
                load(INPUTREADER);
                invokevirtual(El.getMethod(InputReader.class, "getLineNumber"));
                load(INPUTREADER);
                invokevirtual(El.getMethod(InputReader.class, "getColumnNumber"));
                invokevirtual(El.getMethod(ParserLineLocator.class, "setLocation", String.class, int.class, int.class, int.class, int.class));
                fixAddress(branch);
            }
        }
        if (offsetLocatorSupported)
        {
            if (Typ.isAssignable(returnType, Typ.getTypeFor(ParserOffsetLocator.class)))
            {
                dup();
                String branch = createBranch();
                ifnull(branch);
                dup();
                load(SOURCESTACK);
                load(SP);
                aaload();
                load(OFFSETSTACK);
                load(SP);
                iaload();
                load(INPUTREADER);
                invokevirtual(El.getMethod(InputReader.class, "getStart"));
                invokevirtual(El.getMethod(ParserOffsetLocator.class, "setLocation", String.class, int.class, int.class));
                fixAddress(branch);
            }
        }
    }

    private boolean handlesException(ExecutableElement recoverMethod)
    {
        for (VariableElement p : recoverMethod.getParameters())
        {
            if (Typ.isAssignable(p.asType(), Typ.getTypeFor(Exception.class)))
            {
                return true;
            }
        }
        return false;
    }
    interface SubCompiler
    {
        void compile() throws ParserCompilerException;
        String getLabel();
    }
    private class TerminalActionCompiler implements SubCompiler
    {
        private TerminalAction action;

        public TerminalActionCompiler(TerminalAction action)
        {
            this.action = action;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final TerminalActionCompiler other = (TerminalActionCompiler) obj;
            if (this.action != other.action && (this.action == null || !this.action.equals(other.action)))
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 43 * hash + (this.action != null ? this.action.hashCode() : 0);
            return hash;
        }

        public void compile() throws ParserCompilerException
        {
            try
            {
                fixAddress(getLabel());
                if (action instanceof Shift)
                {
                    compileShiftAction((Shift)action);
                }
                else
                {
                    if (action instanceof Reduce)
                    {
                        compileReduceAction((Reduce)action);
                    }
                    else
                    {
                        if (action instanceof LaShift)
                        {
                            compileLaShiftAction((LaShift)action);
                        }
                        else
                        {
                            if (action instanceof LaReduce)
                            {
                                compileLaReduceAction((LaReduce)action);
                            }
                            else
                            {
                                assert false;
                            }
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                throw new ParserCompilerException(ex);
            }
        }

        public String getLabel()
        {
            return action+"-action";
        }
    }

    private class GotoCompiler implements SubCompiler
    {
        private Nonterminal nt;

        public GotoCompiler(Nonterminal nt)
        {
            this.nt = nt;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final GotoCompiler other = (GotoCompiler) obj;
            if (this.nt != other.nt && (this.nt == null || !this.nt.equals(other.nt)))
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 97 * hash + (this.nt != null ? this.nt.hashCode() : 0);
            return hash;
        }

        @Override
        public void compile() throws ParserCompilerException
        {
            try
            {
                if (!nt.isStart())
                {
                    String label = getLabel();
                    LookupList list = new LookupList();
                    fixAddress(label);
                    load(STATESTACK);
                    load(SP);
                    iaload();
                    for (Lr0State lr0State : lr0StateList)
                    {
                        for (Goto go : lr0State.getGotoList())
                        {
                            if (nt.equals(go.getSymbol()))
                            {
                                String target = addCompilerRequest(new GotoActionCompiler(go.getAction()));
                                list.addLookup(lr0State.getNumber(), target);
                                //actionSet.add(go.getAction());
                            }
                        }
                    }
                    if (list.isEmpty())
                    {
                        lrk.printAll(new SystemErrPrinter(), parserCompiler.getProcessingEnvironment());
                        throw new IllegalArgumentException(nt+" has empty goto table. Meaning propably that it is not referenced outside it's own declaration");
                    }
                    optimizedSwitch(list);
                }
            }
            catch (Exception ex)
            {
                throw new ParserCompilerException(ex);
            }
        }

        public String getLabel()
        {
            return nt+"-goto";
        }
    }

    private class GotoActionCompiler implements SubCompiler
    {
        private Action action;

        public GotoActionCompiler(Action action)
        {
            this.action = action;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final GotoActionCompiler other = (GotoActionCompiler) obj;
            if (this.action != other.action && (this.action == null || !this.action.equals(other.action)))
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 53 * hash + (this.action != null ? this.action.hashCode() : 0);
            return hash;
        }

        public void compile() throws ParserCompilerException
        {
            try
            {
                fixAddress(getLabel());
                if (action instanceof Lr0State)
                {
                    Lr0State lr0State = (Lr0State) action;
                    trace(Trace.GOTO, lr0State.getNumber());
                    push(lr0State.getNumber());
                    goto_n("start");
                }
                else
                {
                    if (action instanceof LaState)
                    {
                        throw new UnsupportedOperationException("LaState-goto-action");
                    }
                    else
                    {
                        GRule rule = (GRule) action;
                        trace(Trace.GTRD, rule.getNumber());
                        trace(Trace.BEFOREREDUCE, rule.getOriginalNumber());
                        inc(SP, 1);
                        String target = addCompilerRequest(new ReductSubCompiler(rule));
                        jsr(target);   // shift/reduce
                        trace(Trace.AFTERREDUCE, rule.getOriginalNumber());
                        Nonterminal nt = rule.getLeft();
                        if (!nt.isStart())
                        {
                            String t = addCompilerRequest(new GotoCompiler(nt));
                            goto_n(t);
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                throw new ParserCompilerException(ex);
            }
        }

        public String getLabel()
        {
            return action+"-goto-action";
        }
    }

    private class ReductCompiler implements SubCompiler
    {
        private GRule rule;

        public ReductCompiler(GRule rule)
        {
            this.rule = rule;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final ReductCompiler other = (ReductCompiler) obj;
            if (this.rule != other.rule && (this.rule == null || !this.rule.equals(other.rule)))
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 47 * hash + (this.rule != null ? this.rule.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString()
        {
            return "ReductCompiler{" + "rule=" + rule + '}';
        }

        public void compile() throws ParserCompilerException
        {
            try
            {
                fixAddress(getLabel());
                String target = addCompilerRequest(new ReductSubCompiler(rule));
                jsr(target);   // shift/reduce
                Nonterminal nt = rule.getLeft();
                if (!nt.isStart())
                {
                    String t = addCompilerRequest(new GotoCompiler(nt));
                    goto_n(t);
                }
                else
                {
                    goto_n("assert");
                }
            }
            catch (Exception ex)
            {
                throw new ParserCompilerException(ex);
            }
        }

        public String getLabel()
        {
            return rule.toString();
        }
    }

    private class ReductSubCompiler implements SubCompiler
    {
        private GRule rule;

        public ReductSubCompiler(GRule rule)
        {
            this.rule = rule;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final ReductSubCompiler other = (ReductSubCompiler) obj;
            if (this.rule != other.rule && (this.rule == null || !this.rule.equals(other.rule)))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return "ReductSubCompiler{" + "rule=" + rule + '}';
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 47 * hash + (this.rule != null ? this.rule.hashCode() : 0);
            return hash;
        }

        public void compile() throws ParserCompilerException
        {
            try
            {
                startSubroutine(getLabel());
                // state stack
                int rhs = rule.getRight().size();
                if (rhs > 0)
                {
                    inc(SP, -rhs);
                }

                ExecutableElement reducer = rule.getReducer();
                if (reducer != null)
                {
                    List<? extends VariableElement> parameters = reducer.getParameters();
                    TypeMirror returnType = reducer.getReturnType();
                    TypeKind rot = returnType.getKind();

                    if (returnType.getKind() != TypeKind.VOID)
                    {
                        load(VALUESTACK);                // valueStack
                        iconst(Typ.getTypeNumber(rot));  // valueStack class
                        aaload();                         // stackXXX
                        checkcast(Typ.getArrayType(Typ.normalizeType(rot)));
                        load(SP);                      // stackXXX sp
                    }
                    if (
                            !parserCompiler.implementedAbstract(reducer) &&
                            !reducer.getModifiers().contains(Modifier.ABSTRACT) &&
                            !reducer.getModifiers().contains(Modifier.STATIC)
                            )
                    {
                        load(THIS);                            // this
                    }
                    int paramIndex = 0;
                    int symbolIndex = 0;
                    for (Symbol symbol : rule.getRight())
                    {
                        TypeMirror rt = symbol.getReducerType();
                        if (rt.getKind() != TypeKind.VOID)
                        {
                            TypeMirror param = parameters.get(paramIndex).asType();
                            if (!Typ.isAssignable(rt, param) && !Reducers.isGet(symbol.getReducer()))
                            {
                                String m = symbol+" returntype="+rt+" cannot be used as "+paramIndex+" argument in reducer "+reducer+" expecting "+param;
                                if (Typ.isPrimitive(rt))
                                {
                                    throw new IllegalArgumentException(m+"\n"+"Possibly primitive value used in '?'");
                                }
                                else
                                {
                                    throw new IllegalArgumentException(m);
                                }
                            }
                            load(VALUESTACK);              // this valueStack
                            TypeKind pot = rt.getKind();
                            iconst(Typ.getTypeNumber(pot));  // this valueStack indexXXX
                            aaload();                         // this stackXXX
                            checkcast(Typ.getArrayType(Typ.normalizeType(pot)));
                            load(SP);          // sp
                            iconst(symbolIndex);
                            iadd();
                            taload(rt);                    // this paramx
                            if (Typ.isPrimitive(param))
                            {
                                convert(rt, param);
                            }
                            else
                            {
                                checkcast(param);
                            }
                            paramIndex++;
                        }
                        symbolIndex++;
                    }
                    loadContextParameters(reducer, paramIndex);
                    if (!parserCompiler.implementedAbstract(reducer))
                    {
                        invoke(reducer);               // result
                    }

                    if (returnType.getKind() != TypeKind.VOID)
                    {
                        callSetLocation(returnType);
                                                            // stackXXX spXXX result
                        tastore(returnType);              //
                        load(TYPESTACK);
                        load(SP);
                        iconst(Typ.getTypeNumber(rot));
                        iastore();
                    }
                }
                if (rule.isAccepting())
                {
                    Nonterminal s = (Nonterminal) rule.getRight().get(0);
                    GRule sr = s.getLhsRule().get(0);
                    ExecutableElement r = sr.getReducer();
                    if (r != null)
                    {
                        TypeMirror type = r.getReturnType();
                        if (!Typ.isAssignable(type, parseReturnType))
                        {
                            throw new IllegalArgumentException("reducer "+r+" return "+type+" not compatible with "+parseReturnType);
                        }
                        if (type.getKind() != TypeKind.VOID)
                        {
                            TypeKind rot = type.getKind();
                            load(VALUESTACK);              // valueStack
                            iconst(Typ.getTypeNumber(rot));  // valueStack indexXXX
                            aaload();                         // stackXXX
                            checkcast(Typ.getArrayType(Typ.normalizeType(rot)));
                            iconst(0);                        // stackXXX 0
                            taload(type);                     // valueXXX
                            if (!Typ.isPrimitive(parseReturnType))
                            {
                                checkcast(parseReturnType);
                            }
                            /*
                            store(CUR+rot.name());
                            try
                            {
                                convert(CUR + rot.name(), parseReturnType); // refXXX
                            }
                            catch (IllegalConversionException ex)
                            {
                                throw new IOException("Conversion problem with "+r, ex);
                            }
                             */
                        }
                        else
                        {
                            loadDefault(parseReturnType);
                        }
                    }
                    else
                    {
                        loadDefault(parseReturnType);
                    }
                    treturn();
                    resetSubroutine();
                }
                else
                {
                    endSubroutine();
                }
            }
            catch (Exception ex)
            {
                throw new ParserCompilerException(ex);
            }
        }

        public String getLabel()
        {
            return rule.toString()+"subroutine";
        }
    }

    private class Element implements Comparable<Element>
    {
        private final int order;
        private final String left;
        private final Annotation annotation;
        private final ExecutableElement reducer;
        private final boolean terminal;

        public Element(int order, String left, Annotation annotation, ExecutableElement reducer, boolean terminal)
        {
            this.order = order;
            this.left = left;
            this.annotation = annotation;
            this.reducer = reducer;
            this.terminal = terminal;
        }

        public int compareTo(Element o)
        {
            return order - o.order;
        }

        public boolean isTerminal()
        {
            return terminal;
        }

    }
}
