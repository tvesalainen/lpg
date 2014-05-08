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
package org.vesalainen.grammar;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.Typ;
import static org.vesalainen.grammar.GrammarConstants.*;
import org.vesalainen.lpg.LALRKParserGenerator;
import org.vesalainen.grammar.state.DFA;
import org.vesalainen.parser.util.HashMapSet;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parser.util.MapSet;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.util.Reducers;
import org.vesalainen.regex.Regex;
import org.vesalainen.regex.Regex.Option;

/**
 * @author Timo Vesalainen
 */
public class Grammar
{
    private static final BnfGrammarIntf bnfParser = BnfGrammarFactory.newInstance();
    private static final SyntheticParserIntf syntheticParser = SyntheticParserFactory.newInstance();
    private final Set<Grammar.R> ruleSet = new HashSet<>();
    private final MapSet<String,Grammar.R> lhsMap = new HashMapSet<>();
    private final Map<String,Grammar.T> terminalMap = new HashMap<>();
    private final Map<String,Grammar.NT> nonterminalMap = new HashMap<>();
    private final Map<String,Grammar.S> symbolMap = new HashMap<>();
    private final Map<Integer,Grammar.S> numberMap = new HashMap<>();
    private Grammar.T eof;
    private final Set<Grammar.T> whiteSpaceSet = new HashSet<>();
    private int ruleNumber;
    private int symbolNumber=Symbol.FIRST_NUMBER;
    
    private int lrkLevel;
    private int maxStack;
    private int traceLevel;

    public Grammar()
    {
        this(5, 100, 0);
    }

    public Grammar(GrammarDef gd)
    {
        this(gd.lrkLevel(), gd.maxStack(), gd.traceLevel());
    }

    public Grammar(int lrkLevel, int maxStack)
    {
        this(lrkLevel, maxStack, 0);
    }
    
    public Grammar(int lrkLevel, int maxStack, int traceLevel)
    {
        this.lrkLevel = lrkLevel;
        this.maxStack = maxStack;
        this.traceLevel = traceLevel;
    }

    public Grammar(String start, Grammar sg, String eof, String... whiteSpace)
    {
        this(sg.lrkLevel, sg.maxStack, sg.traceLevel);
        if (!eof.isEmpty())
        {
            Grammar.T t = sg.terminalMap.get(eof);
            if (t == null)
            {
                throw new GrammarException("eof terminal "+eof+" not found");
            }
            addTerminal(t);
            this.eof = t;
        }
        for (String wsp : whiteSpace)
        {
            Grammar.T t = sg.terminalMap.get(wsp);
            if (t == null)
            {
                throw new GrammarException("white-space terminal "+wsp+" not found");
            }
            addTerminal(t);
            whiteSpaceSet.add(t);
        }
        Deque<String> stack = new ArrayDeque<>();
        Set<String> set = new HashSet<>();
        stack.push(start);
        set.add(start);
        while (!stack.isEmpty())
        {
            String lhs = stack.pop();
            MapSet<String,Grammar.R> lhsm = sg.lhsMap;
            if (!lhsm.containsKey(lhs))
            {
                throw new GrammarException(lhs+" not found in lhsMap");
            }
            for (Grammar.R r : lhsm.get(lhs))
            {
                addRule(r);
                for (String symbol : r.rhs)
                {
                    if (sg.isTerminal(symbol))
                    {
                        Grammar.S s = sg.getS(symbol);
                        if (s != null && s instanceof Grammar.T)
                        {
                            addTerminal((Grammar.T)s);
                        }
                        else
                        {
                            throw new GrammarException(symbol+" not found");
                        }
                    }
                    else
                    {
                        if (!set.contains(symbol))
                        {
                            stack.push(symbol);
                            set.add(symbol);
                        }
                    }
                }
            }
        }
    }

    public boolean hasNonterminal(String name)
    {
        return nonterminalMap.containsKey(name);
    }
    public boolean hasTerminal(String name)
    {
        return terminalMap.containsKey(name);
    }
    public static String quantifierRules(String nt, char quantifier, Grammar g) throws NoSuchMethodException
    {
        switch (quantifier)
        {
            case '*':
            {
                g.addSyntheticRule(
                        El.getMethod(Reducers.class, "listStart"),
                        nt+CIRCLED_ASTERISK
                        );
                g.addSyntheticRule(
                        El.getMethod(Reducers.class, "listNext", List.class, Object.class),
                        nt+CIRCLED_ASTERISK, 
                        nt+CIRCLED_ASTERISK, 
                        nt
                        );
                return nt+CIRCLED_ASTERISK;
            }
            case '+':
            {
                g.addSyntheticRule(
                        El.getMethod(Reducers.class, "listStart", Object.class),
                        nt+CIRCLED_PLUS, 
                        nt
                        );
                g.addSyntheticRule(
                        El.getMethod(Reducers.class, "listNext", List.class, Object.class),
                        nt+CIRCLED_PLUS, 
                        nt+CIRCLED_PLUS, 
                        nt
                        );
                return nt+CIRCLED_PLUS;
            }
            case '?':
            {
                g.addSyntheticRule(
                        El.getMethod(Reducers.class, "get"),
                        nt+INVERTED_QUESTION_MARK
                        );
                g.addSyntheticRule(
                        El.getMethod(Reducers.class, "get", Object.class),
                        nt+INVERTED_QUESTION_MARK, 
                        nt
                        );
                return nt+INVERTED_QUESTION_MARK;
            }
            default:
                return nt;
        }
    }
    private Grammar.S getS(String name)
    {
        return symbolMap.get(name);
    }
    /**
     * Adds new rule if the same rule doesn't exist already.
     * @param nonterminal Left hand side of the rule.
     * @param rhs Strings in BnfGrammarFactory format.
     * @see BnfGrammarFactory
     */
    protected void addRule(String reducerString, String nonterminal, String document, String... rhs)
    {
        ExecutableElement reducer = null;
        if (reducerString != null && !reducerString.isEmpty())
        {
            reducer = El.getExecutableElement(reducerString);
            if (reducer == null)
            {
                throw new IllegalArgumentException(reducerString+" method not found");
            }
        }
        addRule(reducer, nonterminal, document, false, parseRhs(rhs));
    }
    /**
     * Adds new rule if the same rule doesn't exist already. Rhs is added as-is.
     * Sequences, choices or quantifiers are not parsed.
     * @param nonterminal Left hand side of the rule.
     * @param rhs 
     * @see BnfGrammarFactory
     */
    public void addSyntheticRule(ExecutableElement reducer, String nonterminal, String... rhs)
    {
        addRule(reducer, nonterminal, "", true, Arrays.asList(rhs));
    }
    public void addSyntheticRule(ExecutableElement reducer, String nonterminal, List<String> rhs)
    {
        addRule(reducer, nonterminal, "", true, rhs);
    }
    protected void addRule(ExecutableElement reducer, String nonterminal, String document, String... rhs)
    {
        addRule(reducer, nonterminal, document, false, parseRhs(rhs));
    }
    /**
     * Adds new rule if the same rule doesn't exist already.
     * @param reducer Reducer method.
     * @param nonterminal Left hand side of the rule.
     * @param rhs Strings in BnfGrammarFactory format.
     * @see BnfGrammarFactory
     */
    public void addRule(ExecutableElement reducer, String nonterminal, String... rhs)
    {
        addRule(reducer, nonterminal, "", false, parseRhs(rhs));
    }
    public void addRule(String nonterminal, String... rhs)
    {
        addRule(null, nonterminal, "", false, parseRhs(rhs));
    }
    /**
     * Adds new rule if the same rule doesn't exist already.
     * @param nonterminal Left hand side of the rule.
     * @param rhs 
     */
    public void addRule(String nonterminal, List<String> rhs)
    {
        addRule(null, nonterminal, "", false, rhs);
    }
    /**
     * Adds new rule if the same rule doesn't exist already.
     * @param reducer Reducer method.
     * @param nonterminal Left hand side of the rule.
     * @param rhs Strings which are either nonterminal names, terminal names or 
     * anonymous terminals. Anonymous terminals are regular expressions inside 
     * apostrophes. E.g '[0-9]+'
     */
    public void addRule(ExecutableElement reducer, String nonterminal, String document, boolean synthetic, List<String> rhs)
    {
        Grammar.R rule = new Grammar.R(nonterminal, rhs, reducer, document, synthetic);
        if (!ruleSet.contains(rule))
        {
            rule.number = ruleNumber++;
            ruleSet.add(rule);
            lhsMap.add(nonterminal, rule);
            if (!nonterminalMap.containsKey(nonterminal))
            {
                Grammar.NT nt = new Grammar.NT(nonterminal);
                nonterminalMap.put(nonterminal, nt);
                symbolMap.put(nonterminal, nt);
                numberMap.put(nt.number, nt);
            }
            for (String s : rhs)
            {
                if (isAnonymousTerminal(s))
                {
                    String expression = s.substring(1, s.length()-1);
                    addAnonymousTerminal(expression);
                }
            }
        }
    }
    private void addRule(Grammar.R rule)
    {
        String ntName = rule.lhs;
        ruleSet.add(rule);
        lhsMap.add(ntName, rule);
        if (!nonterminalMap.containsKey(ntName))
        {
            Grammar.NT nt = new Grammar.NT(ntName);
            nonterminalMap.put(ntName, nt);
            symbolMap.put(ntName, nt);
            numberMap.put(nt.number, nt);
        }
    }
    private List<String> parseRhs(String... orhs)
    {
        List<String> rhs = new ArrayList<>();
        for (String s : orhs)
        {
            rhs.addAll(bnfParser.parseRhs(s, this));
        }
        return rhs;
    }

    /**
     * Adds anonymous terminal. Anonymous terminals name is 'expression'
     * @param expression
     * @param options 
     */
    public void addAnonymousTerminal(String expression, Option... options)
    {
        addTerminal(null, "'"+expression+"'", expression, "", 0, 10, options);
    }
    public void addTerminal(String reducerString, String name, String expression, int priority, int base, Option... options)
    {
        ExecutableElement reducer = null;
        if (reducerString != null && !reducerString.isEmpty())
        {
            reducer = El.getExecutableElement(reducerString);
            if (reducer == null)
            {
                throw new IllegalArgumentException(reducerString+" method not found");
            }
        }
        addTerminal(reducer, name, expression, "", priority, base, options);
    }
    public final void addTerminal(ExecutableElement reducer, String name, String expression, String documentation, int priority, int base, Option... options)
    {
        if (isAnonymousTerminal(expression))
        {
            System.err.println("warning! terminal name="+name+" expression="+expression+" might be anonymous");
        }
        if (!terminalMap.containsKey(name))
        {
            Grammar.T terminal = new Grammar.T(name, expression, documentation, priority, base, options, reducer);
            terminalMap.put(name, terminal);
            symbolMap.put(name, terminal);
            numberMap.put(terminal.number, terminal);
        }
    }
    private void addTerminal(Grammar.T terminal)
    {
        String name = terminal.name;
        if (!terminalMap.containsKey(name))
        {
            terminalMap.put(name, terminal);
            symbolMap.put(name, terminal);
            numberMap.put(terminal.number, terminal);
        }
    }
    /**
     * Return a parser generator from grammar. The same grammar can produce different
     * parsers depending for example on start rhs.
     * @param start
     * @param eof
     * @param whiteSpace
     * @return 
     */
    public LALRKParserGenerator getParserGenerator(ParseMethod parseMethod)
    {
        Grammar g = new Grammar(parseMethod.start(), this, parseMethod.eof(), parseMethod.whiteSpace());
        try
        {
            return g.createParserGenerator(parseMethod.start(), parseMethod.syntaxOnly());
        }
        catch (Throwable t)
        {
            throw new GrammarException("problem with "+parseMethod, t);
        }
    }
    public LALRKParserGenerator createParserGenerator(String start, boolean syntaxOnly) throws IOException
    {
        List<GRule> ruleList = new ArrayList<>();
        List<Symbol> symbolList = new ArrayList<>();
        List<Nonterminal> nonterminalList = new ArrayList<>();
        List<GTerminal> terminalList = new ArrayList<>();
        int ruleNum = 0;
        
        Map<String,GTerminal> tMap = new HashMap<>();
        for (Grammar.T term : terminalMap.values())
        {
            GTerminal t = null;
            if (term.equals(eof))
            {
                t = new Eof(term.number, term.name, term.expression, term.priority, term.base, false, term.options);
            }
            else
            {
                t = new GTerminal(term.number, term.name, term.expression, term.priority, term.base, whiteSpaceSet.contains(term), term.document, term.options);
            }
            if (!syntaxOnly || term.reducer == null || term.reducer.getReturnType().getKind() == TypeKind.VOID)
            {
                t.setReducer(term.reducer);
            }
            terminalList.add(t);
            tMap.put(term.name, t);
        }
        Map<String,Nonterminal> ntMap = new HashMap<>();
        for (String name : nonterminalMap.keySet())
        {
            Nonterminal nt = new Nonterminal(symbolMap.get(name).number, name);
            ntMap.put(nt.getName(), nt);
        }
        Nonterminal startRhs = ntMap.get(start);
        if (startRhs == null)
        {
            throw new GrammarException("start nonterminal "+start+" not found");
        }
        List<Symbol> startRhsList = new ArrayList<>();
        startRhsList.add(startRhs);
        Accept a = new Accept();
        nonterminalList.add(a);
        GRule acc = new GRule(a, startRhsList, false, "");
        ruleList.add(acc);
        acc.setNumber(ruleNum++);
        a.addLhsRule(acc);
        Set<String> resolved = new HashSet<>();
        Deque<String> unresolved = new ArrayDeque<>();
        resolved.add(start);
        unresolved.addLast(start);
        while (!unresolved.isEmpty())
        {
            String lhsNt = unresolved.removeFirst();
            Nonterminal lhs = ntMap.get(lhsNt);
            assert lhs != null;
            if (!nonterminalList.contains(lhs))
            {
                nonterminalList.add(lhs);
            }
            for (Grammar.R rule : lhsMap.get(lhsNt))
            {
                List<Symbol> rhs = new ArrayList<>();
                for (String sn : rule.rhs)
                {
                    Symbol symbol = ntMap.get(sn);
                    if (symbol == null)
                    {
                        symbol = tMap.get(sn);
                        if (symbol == null)
                        {
                            if (isAnonymousTerminal(sn))
                            {
                                String expression = sn.substring(1, sn.length()-1);
                                //symbol = new GTerminal(sn, expression, 0, false);
                                terminalList.add((GTerminal)symbol);
                                tMap.put(sn, (GTerminal)symbol);
                            }
                            else
                            {
                                throw new GrammarException(sn+" symbol not defined");
                            }
                        }
                    }
                    else
                    {
                        if (!resolved.contains(sn))
                        {
                            resolved.add(sn);
                            unresolved.addLast(sn);
                        }
                    }
                    rhs.add(symbol);
                }
                GRule gr = new GRule(lhs, rhs, rule.synthetic, rule.document);
                if (!syntaxOnly)
                {
                    if (rule.synthetic)
                    {
                        TypeMirror type = syntheticParser.parse(rule.lhs, this);
                        if (type.getKind() != TypeKind.VOID)
                        {
                            gr.setReducer(rule.reducer);
                        }
                    }
                    else
                    {
                        gr.setReducer(rule.reducer);
                    }
                }
                ruleList.add(gr);
                gr.setNumber(ruleNum++);
                gr.setOriginalNumber(rule.number);
                lhs.addLhsRule(gr);
            }
        }

        symbolList.addAll(terminalList);
        symbolList.addAll(nonterminalList);
        checkGrammar(ruleList, nonterminalList, terminalList);
        print(System.err);

        return new LALRKParserGenerator(start, lrkLevel, ruleList, symbolList, nonterminalList, terminalList);
    }
    /**
     * Checks the correctness of grammar
     */
    private void checkGrammar(List<GRule> ruleList, List<Nonterminal> nonterminalList, List<GTerminal> terminalList) throws IOException
    {
        for (Nonterminal nt : nonterminalList)
        {
            boolean hasLhs = false;
            for (GRule rule : ruleList)
            {
                if (nt.equals(rule.getLeft()))
                {
                    hasLhs = true;
                    break;
                }
            }
            if (!hasLhs)
            {
                print(System.err);
                throw new GrammarException("Nonterminal "+nt.toString()+" is not left part of any rule");
            }
        }
        for (GTerminal t : terminalList)
        {
            if (GTerminal.class.equals(t.getClass()))
            {
                String expr = t.getExpression();
                if (expr.isEmpty())
                {
                    print(System.err);
                    throw new GrammarException("Terminal "+t.getName()+" accepts empty string");
                }
                DFA<Integer> dfa = Regex.createDFA(expr);
                if (dfa.getRoot().isAccepting())
                {
                    print(System.err);
                    throw new GrammarException("Terminal "+t.getName()+" accepts empty string");
                }
                ExecutableElement reducer = t.getReducer();
                if (reducer != null)
                {
                    if (t.isWhiteSpace())
                    {
                        if (reducer.getReturnType().getKind() != TypeKind.VOID)
                        {
                            boolean ok = false;
                            for (ExecutableElement insert : ElementFilter.methodsIn(El.getTypeElement(InputReader.class.getCanonicalName()).getEnclosedElements()))
                            {
                                List<? extends VariableElement> parameters = insert.getParameters();
                                if (parameters.size() == 1 && Typ.isAssignable(reducer.getReturnType(), parameters.get(0).asType()))
                                {
                                    ok = true;
                                    break;
                                }
                            }
                            if (!ok)
                            {
                                throw new GrammarException("white-space terminal has return type not suitable for InputReader.insert "+reducer);
                            }
                        }
                    }
                }
            }
        }
        for (GRule rule : ruleList)
        {
            for (Symbol symbol : rule.getRight())
            {
                if (symbol instanceof Eof)
                {
                    throw new GrammarException("eof terminal "+symbol+" in "+rule.getDescription());
                }
                if (symbol instanceof GTerminal)
                {
                    GTerminal terminal = (GTerminal) symbol;
                    if (terminal.isWhiteSpace())
                    {
                        throw new GrammarException("white-space terminal "+terminal+" in "+rule.getDescription());
                    }
                }
            }
            ExecutableElement reducer = rule.getReducer();
            if (reducer != null)
            {
                List<? extends VariableElement> params = reducer.getParameters();
                int index = 0;
                for (Symbol symbol : rule.getRight())
                {
                    if (symbol instanceof GTerminal)
                    {
                        GTerminal t = (GTerminal) symbol;
                        ExecutableElement red = t.getReducer();
                        if (red != null && red.getReturnType().getKind() != TypeKind.VOID)
                        {
                            if (index >= params.size())
                            {
                                print(System.err);
                                throw new GrammarException("Too few parameters for "+rule.getDescription());
                            }
                            index++;
                        }
                    }
                    else
                    {
                        Nonterminal nt = (Nonterminal) symbol;
                        boolean nonVoid = false;
                        for (GRule r : nt.getLhsRule())
                        {
                            ExecutableElement red = r.getReducer();
                            if (red != null && red.getReturnType().getKind() != TypeKind.VOID)
                            {
                                if (index >= params.size())
                                {
                                    print(System.err);
                                    throw new GrammarException("Method "+red+"return value not consumed in "+rule.getDescription());
                                }
                                nonVoid = true;
                            }
                        }
                        if (nonVoid)
                        {
                            index++;
                        }
                    }
                }
                for (;index < params.size();index++)
                {
                    boolean pc = false;
                    ParserContext annotation = params.get(index).getAnnotation(ParserContext.class);
                    if (annotation == null)
                    {
                        print(System.err);
                        throw new GrammarException("extra parameter "+params.get(index)+" for "+rule.getDescription()+" which are not @ParserContext");
                    }
                }
            }
            else
            {
                if (!rule.getLeft().isStart())
                {
                    for (Symbol symbol : rule.getRight())
                    {
                        if (symbol.getReducerType().getKind() != TypeKind.VOID)
                        {
                            throw new GrammarException(rule.getDescription()+" doesn't have reducer but "+symbol+" has");
                        }
                    }
                }
            }
        }
    }

    public void print(Appendable out) throws IOException
    {
        if (traceLevel > 0)
        {
            out.append("Terminals:\n");
            for (String t : terminalMap.keySet())
            {
                out.append(t);
                out.append('\n');
            }
            out.append("Rules:\n");
            for (String lhsNt : lhsMap.keySet())
            {
                for (Grammar.R rule : lhsMap.get(lhsNt))
                {
                    print(out, rule);
                }
            }
        }
    }
    /**
     * Prints @Terminals and @Rules annotations
     * @param out
     * @throws IOException 
     */
    public void printAnnotations(Appendable out) throws IOException
    {
        boolean f = true;
        out.append("@Terminals({\n");
        for (T terminal : terminalMap.values())
        {
            if (!isAnonymousTerminal(terminal.name))
            {
                if (f)
                {
                    f = false;
                }
                else
                {
                    out.append(",");
                }
                terminal.printAnnotation(out);
                out.append('\n');
            }
        }
        out.append("})\n");
        out.append("@Rules({\n");
        f = true;
        for (R rule : ruleSet)
        {
            if (!rule.synthetic)
            {
                if (f)
                {
                    f = false;
                }
                else
                {
                    out.append(",");
                }
                rule.printAnnotation(out);
                out.append('\n');
            }
        }
        out.append("})\n");
    }
    public Map<Integer,String> getRuleDescriptions() throws IOException
    {
        Map<Integer,String> map = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        for (Grammar.R rule : ruleSet)
        {
            print(sb, rule);
            map.put(rule.number, sb.toString());
            sb.delete(0, sb.length());
        }
        return map;
    }
    private void print(Appendable out, Grammar.R rule) throws IOException
    {
        out.append(rule.number+" ");
        out.append(rule.lhs);
        out.append(" ::=");
        for (String symbol : rule.rhs)
        {
            out.append(' ');
            out.append(symbol);
        }
        out.append('\n');
    }
    public static boolean isAnonymousTerminal(String name)
    {
        if (name.isEmpty())
        {
            throw new IllegalArgumentException("empty terminal");
        }
        return name.charAt(0) == '\'' && name.charAt(name.length()-1) == '\'';
    }

    private boolean isTerminal(String symbol)
    {
        return isAnonymousTerminal(symbol) || terminalMap.containsKey(symbol);
    }

    public String getSymbol(int number)
    {
        return numberMap.get(number).name;
    }

    public int getNumber(String symbol)
    {
        return symbolMap.get(symbol).number;
    }

    public Collection<String> getSymbols()
    {
        return symbolMap.keySet();
    }
    
    public int getLrkLevel()
    {
        return lrkLevel;
    }

    public void setLrkLevel(int lrkLevel)
    {
        this.lrkLevel = lrkLevel;
    }

    public int getMaxStack()
    {
        return maxStack;
    }

    public void setMaxStack(int maxStack)
    {
        this.maxStack = maxStack;
    }

    public static String literal(String text)
    {
        return text.replace("'", "\\x27");
    }
    
    public TypeMirror getTypeForNonterminal(String symbol)
    {
        Set<R> set = lhsMap.get(symbol);
        if (set != null)
        {
            R r = set.iterator().next();
            if (r.reducer != null)
            {
                return r.reducer.getReturnType();
            }
            else
            {
                return Typ.Void;
            }
        }
        else
        {
            T t = terminalMap.get(symbol);
            if (t == null)
            {
                throw new IllegalArgumentException(symbol+" not found");
            }
            if (t.reducer != null)
            {
                return t.reducer.getReturnType();
            }
            else
            {
                return Typ.Void;
            }
        }
    }
    public class R
    {
        protected int number;
        protected String lhs;
        protected List<String> rhs;
        protected ExecutableElement reducer;
        protected String document;
        protected boolean synthetic;

        public R(String lhs, List<String> rhs, ExecutableElement reducer, String document, boolean synthetic)
        {
            this.lhs = lhs;
            this.rhs = rhs;
            this.reducer = reducer;
            this.document = document;
            this.synthetic = synthetic;
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
            final Grammar.R other = (Grammar.R) obj;
            if (!Objects.equals(this.lhs, other.lhs))
            {
                return false;
            }
            if (!Objects.equals(this.rhs, other.rhs))
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 71 * hash + Objects.hashCode(this.lhs);
            hash = 71 * hash + Objects.hashCode(this.rhs);
            return hash;
        }
        /**
         * Prints @Rule annotation
         * @param p
         * @throws IOException 
         */
        private void printAnnotation(Appendable p) throws IOException
        {
            p.append("@Rule");
            p.append('(');
            p.append("left=\""+lhs+"\"");
            if (!document.isEmpty())
            {
                p.append(", doc=\""+document+"\"");
            }
            if (reducer != null)
            {
                p.append(", reducer=\""+El.getExecutableString(reducer) +"\"");
            }
            p.append(", value={");
            for (int ii=0;ii<rhs.size();ii++)
            {
                if (ii > 0)
                {
                    p.append(", ");
                }
                p.append("\"");
                p.append(Nonterminal.toString(rhs.get(ii)));
                p.append("\"");
            }
            p.append("})");
        }
    }
    public class S
    {
        protected int number;
        protected String name;

        public S(String name)
        {
            this.name = name;
            number = symbolNumber++;
        }

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
            final Grammar.S other = (Grammar.S) obj;
            if (!Objects.equals(this.name, other.name))
            {
                return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 5;
            hash = 79 * hash + Objects.hashCode(this.name);
            return hash;
        }

    }
    public class T extends S
    {
        protected String expression;
        protected String document;
        protected int priority;
        protected int base;
        protected Option[] options;
        protected ExecutableElement reducer;

        public T(String name, String expression, String document, int priority, int base, Option[] options, ExecutableElement reducer)
        {
            super(name);
            if (expression != null)
            {
                this.expression = expression;
            }
            else
            {
                this.expression = name;
            }
            this.document = document;
            this.priority = priority;
            this.base = base;
            if (options != null)
            {
                this.options = options;
            }
            else
            {
                this.options = new Option[] {};
            }
            this.reducer = reducer;
        }
        /**
         * Prints @Terminal annotation
         * @param p
         * @throws IOException 
         */
        private void printAnnotation(Appendable p) throws IOException
        {
            p.append("@Terminal");
            p.append('(');
            p.append("left=\""+name.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") +"\"");
            p.append(", expression=\""+GTerminal.getUnescapedExpression(expression)+"\"");
            if (!document.isEmpty())
            {
                p.append(", doc=\""+document+"\"");
            }
            if (reducer != null)
            {
                p.append(", reducer=\""+El.getExecutableString(reducer) +"\"");
            }
            if (priority != 0)
            {
                p.append(", priority="+priority);
            }
            if (base != 10)
            {
                p.append(", radix="+base);
            }
            if (options.length > 0)
            {
                p.append(", options={");
                for (int ii=0;ii<options.length;ii++)
                {
                    if (ii > 0)
                    {
                        p.append(", ");
                    }
                    p.append(options[ii].name());
                }
                p.append("}");
            }
            p.append(")");
        }

    }
    public class NT extends Grammar.S
    {
        public NT(String name)
        {
            super(name);
        }
    }
}
