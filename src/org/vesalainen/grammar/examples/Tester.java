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
package org.vesalainen.grammar.examples;

import org.vesalainen.lpg.Act;
import org.vesalainen.lpg.Goto;
import org.vesalainen.lpg.Item;
import org.vesalainen.lpg.LALRKParserGenerator;
import org.vesalainen.lpg.LaShift;
import org.vesalainen.lpg.LaState;
import org.vesalainen.lpg.Lr0State;
import org.vesalainen.grammar.Nonterminal;
import org.vesalainen.grammar.GRule;
import org.vesalainen.lpg.Shift;
import org.vesalainen.lpg.State;
import org.vesalainen.grammar.Symbol;
import org.vesalainen.grammar.GTerminal;
import org.vesalainen.lpg.LaReduce;
import org.vesalainen.lpg.Reduce;
import org.vesalainen.parser.ParserCompiler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.vesalainen.bcc.model.El;
import org.vesalainen.grammar.Accept;
import org.vesalainen.grammar.Empty;
import org.vesalainen.grammar.Eof;
import org.vesalainen.grammar.Err;
import org.vesalainen.regex.DFACompiler;

/**
 *
 * @author tkv
 * @deprecated TODO redo tests
 */
public class Tester
{
    private static final Pattern RULE = Pattern.compile("([0-9]+)[ \t]*([^ ]+) ::= ([A-Za-z_ ]*)");
    private static final Pattern NTFIRST = Pattern.compile("([^ ]+)[ \t]+ ==>> ([A-Za-z_ %]*)");
    private static final Pattern ITEMSET = Pattern.compile("([0-9 ]+)==>> ([A-Za-z_ %]*)");
    private static final Pattern NUMBEROF = Pattern.compile("Number of ([^:]+): ([0-9]+)");
    private static final Pattern STATE = Pattern.compile("STATE ([0-9]+) [\\-]+");
    private static final Pattern ITEM = Pattern.compile("([^ ]+) ::= (.+)");
    private static final Pattern RULENUMBER = Pattern.compile("\\(([0-9]+)\\)");
    private static final Pattern SHIFT = Pattern.compile("([A-Za-z_]+)[ \t]+Shift[ \t]+([0-9]+)");
    private static final Pattern SHRD = Pattern.compile("([A-Za-z_]+)[ \t]+Sh/Rd[ \t]+([0-9]+)");
    private static final Pattern LASH = Pattern.compile("([A-Za-z_]+)[ \t]+La/Sh[ \t]+([0-9]+)");
    private static final Pattern GOTO = Pattern.compile("([A-Za-z_]+)[ \t]+Goto[ \t]+([0-9]+)");
    private static final Pattern GTRD = Pattern.compile("([A-Za-z_]+)[ \t]+Gt/Rd[ \t]+([0-9]+)");
    private static final Pattern REDUCE = Pattern.compile("([A-Za-z_]+)[ \t]+Reduce[ \t]+([0-9]+)");
    private static final Pattern ACCEPT = Pattern.compile("([A-Za-z_]+)[ \t]+Accept");
    private static final Pattern INSTAT = Pattern.compile("\\([ ]*([0-9 ]+)[ ]*\\)");
    private static final Pattern DEFRED = Pattern.compile("Default reduction to rule  ([0-9]+)");

    private LineNumberReader in;
    private LALRKParserGenerator lpg;
    private Map<String, Symbol> aliases = new HashMap<>();

    public Tester(File file, LALRKParserGenerator lpg) throws FileNotFoundException
    {
        this.lpg = lpg;
        FileReader r = new FileReader(file);
        in = new LineNumberReader(r);
    }

    public void test() throws IOException
    {
        String line = in.readLine();
        while (line != null)
        {
            if (line.startsWith("Terminals:"))
            {
                terminals();
            }
            if (line.startsWith("Rules:"))
            {
                rules();
            }
            if (line.startsWith("First map for non-terminals:"))
            {
                ntFirst();
            }
            if (line.startsWith("Produces set for non-terminals:"))
            {
                produces();
            }
            if (line.startsWith("Suffix for items:"))
            {
                suffix();
            }
            if (line.startsWith("First set for items:"))
            {
                tFirst();
            }
            if (line.startsWith("Read set for states:"))
            {
                readSet();
            }
            if (line.startsWith("Number of "))
            {
                numberOf(line);
            }
            if (line.startsWith("STATE"))
            {
                state(line);
            }
            line = in.readLine();
        }
    }

    private void terminals() throws IOException
    {
        System.err.println("Check Terminals");
        String line = in.readLine();
        while (line.isEmpty())
        {
            line = in.readLine();
        }
        while (!line.isEmpty())
        {
            String[] t = line.split("[ ]+");
            for (String s : t)
            {
                if (!s.isEmpty() &&!"\f".equals(s))
                {
                    aliases.put(s, createTerminal(s, s));
                }
            }
            line = in.readLine();
        }
        aliases.put("%acc", new Accept());
        aliases.put("%empty", new Empty());
    }

    private GTerminal createTerminal(String name, String expr)
    {
        if ("%eof".equals(name) || "EOF".equals(name))
        {
            return new Eof();
        }
        if ("%error".equals(name) || "ERROR".equals(name))
        {
            return new Err();
        }
        return null;//new GTerminal(name, expr);
    }

    private void rules() throws IOException
    {
        System.err.println("Check Rules");
        String line = in.readLine();
        while (line.isEmpty())
        {
            line = in.readLine();
        }
        while (!line.startsWith("\f"))
        {
            Matcher m = RULE.matcher(line);
            if (m.lookingAt())
            {
                int number = Integer.parseInt(m.group(1));
                String lhs = m.group(2);
                String[] rhs = m.group(3).split("[ ]+");
                GRule rule = lpg.getRules().get(number);
                assertEquals(rule.getLeft(), getSymbol(lhs));
                
                if (rhs.length == 1 && rhs[0].isEmpty())
                {
                    assertTrue(rule.getRight().size() == 0, "RHS length differs "+line);
                }
                else
                {
                    assertTrue(rule.getRight().size() == rhs.length, "RHS length differs "+line);
                    int index = 0;
                    for (String r : rhs)
                    {
                        assertEquals(rule.getRight().get(index++), getSymbol(r));
                    }
                }
            }
            line = in.readLine();
        }
    }

    private Symbol getSymbol(String s)
    {
        if (aliases.containsKey(s))
        {
            return aliases.get(s);
        }
        return null; //new Nonterminal(s);
    }

    private void assertTrue(boolean b, String message)
    {
        if (!b)
        {
            throw new IllegalArgumentException(message);
        }
    }

    private void assertEquals(Object o1, Object o2)
    {
        if (o1 == null || o2 == null)
        {
            throw new IllegalArgumentException("null values");
        }
        if (!o1.equals(o2))
        {
            throw new IllegalArgumentException(o1 + " != " + o2);
        }
    }
    private void produces() throws IOException
    {
        System.err.println("Check Produces");
        String line = in.readLine();
        while (line.isEmpty())
        {
            line = in.readLine();
        }
        while (!line.startsWith("\f"))
        {
            Matcher m = NTFIRST.matcher(line);
            if (m.lookingAt())
            {
                Nonterminal nt = (Nonterminal) getSymbol(m.group(1));
                String[] dp = m.group(2).split("[ ]+");
                List<Nonterminal> ntList = lpg.getNonterminals();
                int idx = ntList.indexOf(nt);
                assertTrue(idx != -1, nt+" not found");
                Nonterminal nt2 = ntList.get(idx);
                Set<Nonterminal> fSet = nt2.getProduces();

                if (dp.length == 1 && dp[0].isEmpty())
                {
                    assertTrue(fSet.size() == 0, "Produces length differs "+line);
                }
                else
                {
                    assertTrue(fSet.size() == dp.length, "Produces length differs "+line);
                    int index = 0;
                    for (String r : dp)
                    {
                        assertTrue(fSet.contains(getSymbol(r)), r+" not found");
                    }
                }
            }
            line = in.readLine();
        }
    }


    private void ntFirst() throws IOException
    {
        System.err.println("Check ntFirst");
        String line = in.readLine();
        while (line.isEmpty())
        {
            line = in.readLine();
        }
        while (!line.startsWith("\f"))
        {
            Matcher m = NTFIRST.matcher(line);
            if (m.lookingAt())
            {
                Nonterminal nt = (Nonterminal) getSymbol(m.group(1));
                String[] first = m.group(2).split("[ ]+");
                List<Nonterminal> ntList = lpg.getNonterminals();
                int idx = ntList.indexOf(nt);
                assertTrue(idx != -1, nt+" not found");
                Nonterminal nt2 = ntList.get(idx);
                Set<GTerminal> fSet = nt2.getFirstSet();

                if (first.length == 1 && first[0].isEmpty())
                {
                    assertTrue(fSet.size() == 0, "Nt first length differs "+line);
                }
                else
                {
                    assertTrue(fSet.size() == first.length, "Nt first length differs "+line);
                    int index = 0;
                    for (String r : first)
                    {
                        assertTrue(fSet.contains(getSymbol(r)), r+" not found");
                    }
                }
            }
            line = in.readLine();
        }
    }

    private void suffix() throws IOException
    {
        System.err.println("Check suffix");
        String line = in.readLine();
        while (line.isEmpty())
        {
            line = in.readLine();
        }
        while (!line.startsWith("\f"))
        {
            Matcher m = ITEMSET.matcher(line);
            if (m.lookingAt())
            {
                String[] items = m.group(1).split("[ ]+");
                String[] suf = m.group(2).split("[ ]+");
                List<Item> itemList = lpg.getItemList();
                for (String si : items)
                {
                    int index = Integer.parseInt(si);
                    Item item = itemList.get(index);
                    if (suf.length == 1 && suf[0].isEmpty())
                    {
                        assertTrue(0 == item.getSuffix().size(), line+" suffix length differs");
                    }
                    else
                    {
                        assertTrue(suf.length == item.getSuffix().size(), line+" suffix length differs");
                        int idx = 0;
                        for (String st : suf)
                        {
                            Symbol symbol = getSymbol(st);
                            assertTrue(symbol.equals(item.getSuffix().get(idx)), line+" suffix differs "+symbol);
                            idx++;
                        }
                    }
                }
            }
            line = in.readLine();
        }
    }
    private void tFirst() throws IOException
    {
        System.err.println("Check first set");
        String line = in.readLine();
        while (line.isEmpty())
        {
            line = in.readLine();
        }
        while (!line.startsWith("\f"))
        {
            Matcher m = ITEMSET.matcher(line);
            if (m.lookingAt())
            {
                String[] items = m.group(1).split("[ ]+");
                String[] suf = m.group(2).split("[ ]+");
                List<Item> itemList = lpg.getItemList();
                for (String si : items)
                {
                    int index = Integer.parseInt(si);
                    Item item = itemList.get(index);
                    if (suf.length == 1 && suf[0].isEmpty())
                    {
                        assertTrue(0 == item.getFirstSet().size(), line+" first set length differs");
                    }
                    else
                    {
                        assertTrue(suf.length == item.getFirstSet().size(), line+" first set length differs");
                        int idx = 0;
                        for (String st : suf)
                        {
                            Symbol symbol = getSymbol(st);
                            assertTrue(item.getFirstSet().contains(symbol), line+" first set missing "+symbol);
                            idx++;
                        }
                    }
                }
            }
            line = in.readLine();
        }
    }

    private void readSet() throws IOException
    {
        System.err.println("Check read set");
        String line = in.readLine();
        while (line.isEmpty())
        {
            line = in.readLine();
        }
        while (!line.startsWith("\f"))
        {
            Matcher m = ITEMSET.matcher(line);
            if (m.lookingAt())
            {
                String sts = m.group(1).trim();
                String[] suf = m.group(2).split("[ ]+");
                Lr0State state = (Lr0State) lpg.getStateList().get(Integer.parseInt(sts)-1);
                if (suf.length == 1 && suf[0].isEmpty())
                {
                    assertTrue(0 == state.getReadSet().size(), line+" read set length differs");
                }
                else
                {
                    assertTrue(suf.length == state.getReadSet().size(), line+" read set length differs");
                    int idx = 0;
                    for (String st : suf)
                    {
                        Symbol symbol = getSymbol(st);
                        assertTrue(state.getReadSet().contains(symbol), line+" read set missing "+symbol);
                        idx++;
                    }
                }
            }
            line = in.readLine();
        }
    }


    private void numberOf(String line)
    {
        Matcher m = NUMBEROF.matcher(line);
        if (m.matches())
        {
            if ("Terminals".equals(m.group(1)))
            {
                System.err.println("Check "+line);
                int n = Integer.parseInt(m.group(2))+1;
                if (lpg.getTerminals().size() != n)
                {
                    throw new IllegalArgumentException("Terminal count differs");
                }
            }
            if ("Nonterminals".equals(m.group(1)))
            {
                System.err.println("Check "+line);
                int n = Integer.parseInt(m.group(2))+1;
                if (lpg.getNonterminals().size() != n)
                {
                    throw new IllegalArgumentException("Nonterminals count differs");
                }
            }
            if ("Items".equals(m.group(1)))
            {
                System.err.println("Check "+line);
                int n = Integer.parseInt(m.group(2))+1;
                if (lpg.getItemList().size() != n)
                {
                    throw new IllegalArgumentException("Items count differs");
                }
            }
            if ("States".equals(m.group(1)))
            {
                System.err.println("Check "+line);
                int n = Integer.parseInt(m.group(2));
                if (lpg.getLr0StateList().size() != n)
                {
                    throw new IllegalArgumentException("Lr0States count differs");
                }
            }
            if ("look-ahead states:".equals(m.group(1)))
            {
                System.err.println("Check "+line);
                int n = Integer.parseInt(m.group(2));
                if (lpg.getLaStateList().size() != n)
                {
                    throw new IllegalArgumentException("LaStates count differs");
                }
            }
        }
    }

    private void state(String line) throws IOException
    {
        System.err.println("Check States");
        State state = null;
        int number = 0;
        int kernelCount = 0;
        int completeCount = 0;
        boolean kernel = true;
        for (;line != null;line = in.readLine())
        {
            if (line.isEmpty())
            {
                if (state instanceof Lr0State)
                {
                    Lr0State lr0State = (Lr0State) state;
                    if (kernel && kernelCount > 0)
                    {
                        if (kernelCount != lr0State.getKernelItems().size())
                        {
                            throw new IllegalArgumentException("Kernel size differs in STATE "+number);
                        }
                        kernel = false;
                        kernelCount = 0;
                    }
                    if (!kernel && completeCount > 0)
                    {
                        List<Item> complete = new ArrayList<Item>();
                        complete.addAll(lr0State.getCompleteItems());
                        complete.removeAll(lr0State.getKernelItems());
                        if (completeCount != complete.size())
                        {
                            throw new IllegalArgumentException("Complete item count differs in STATE "+number);
                        }
                        completeCount = 0;
                    }
                }
                continue;
            }
            Matcher m = STATE.matcher(line);
            if (m.matches())
            {
                System.err.println(line);
                number = Integer.parseInt(m.group(1));
                line = in.readLine();
                state = lpg.getStateList().get(number-1);
                kernel = true;
                kernelCount = 0;
                completeCount = 0;
                Matcher mm = INSTAT.matcher(line);
                if (mm.matches())
                {
                    String[] ins = mm.group(1).split("[ ]+");
                    if (state.getInStateCount() != ins.length)
                    {
                        throw new IllegalArgumentException("InStates count differs");
                    }
                    for (String s : ins)
                    {
                        int n = Integer.parseInt(s);
                        State st = lpg.getStateList().get(n-1);
                        if (!state.hasInState(st))
                        {
                            throw new IllegalArgumentException("InStates content differs");
                        }
                    }
                }
                else
                {
                    if (state.getInStateCount() != 0)
                    {
                        throw new IllegalArgumentException("InStates count differs");
                    }
                }
                continue;
            }
            m = ITEM.matcher(line);
            if (m.matches())
            {
                if (state instanceof Lr0State)
                {
                    Lr0State lr0State = (Lr0State) state;
                    if (kernel)
                    {
                        kernelCount++;
                        produce(line, lr0State.getKernelItems(), m, false);
                    }
                    else
                    {
                        if (produce(line, lr0State.getCompleteItems(), m, true))
                        {
                            completeCount++;
                        }
                    }
                }
                continue;
            }
            m = SHIFT.matcher(line);
            if (m.matches())
            {
                if (state instanceof Lr0State)
                {
                    Lr0State lr0State = (Lr0State) state;
                    GTerminal symbol = (GTerminal) getSymbol(m.group(1));
                    Lr0State st = (Lr0State) lpg.getStateList().get(Integer.parseInt(m.group(2))-1);
                    boolean found = false;
                    for (Shift shift : lr0State.getShiftList())
                    {
                        if (symbol.equals(shift.getSymbol()) && st.equals(shift.getAction()))
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                    {
                        throw new IllegalArgumentException(line+" not found");
                    }
                }
                else
                {
                    LaState laState = (LaState) state;
                    GTerminal symbol = (GTerminal) getSymbol(m.group(1));
                    Lr0State st = (Lr0State) lpg.getStateList().get(Integer.parseInt(m.group(2))-1);
                    boolean found = false;
                    for (LaShift shift : laState.getShiftList())
                    {
                        if (symbol.equals(shift.getSymbol()) && st.equals(shift.getAct()))
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                    {
                        throw new IllegalArgumentException(line+" not found");
                    }
                }
                continue;
            }
            m = SHRD.matcher(line);
            if (m.matches())
            {
                if (state instanceof Lr0State)
                {
                    Lr0State lr0State = (Lr0State) state;
                    GTerminal symbol = (GTerminal) getSymbol(m.group(1));
                    GRule rl = lpg.getRules().get(Integer.parseInt(m.group(2)));
                    boolean found = false;
                    for (Shift shift : lr0State.getShiftList())
                    {
                        if (symbol.equals(shift.getSymbol()) && rl.equals(shift.getAction()))
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                    {
                        throw new IllegalArgumentException(line+" not found");
                    }
                }
                else
                {
                    LaState laState = (LaState) state;
                    GTerminal symbol = (GTerminal) getSymbol(m.group(1));
                    GRule rl = lpg.getRules().get(Integer.parseInt(m.group(2)));
                    boolean found = false;
                    for (LaShift shift : laState.getShiftList())
                    {
                        Act act = shift.getAct();
                        if (act instanceof GRule)
                        {
                            GRule rule = (GRule) shift.getAct();
                            if (symbol.equals(shift.getSymbol()) && rl.getNumber() == rule.getNumber())
                            {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found)
                    {
                        throw new IllegalArgumentException(line+" not found");
                    }
                }
                continue;
            }
            m = LASH.matcher(line);
            if (m.matches())
            {
                if (state instanceof Lr0State)
                {
                    Lr0State lr0State = (Lr0State) state;
                    GTerminal symbol = (GTerminal) getSymbol(m.group(1));
                    State st = lpg.getStateList().get(Integer.parseInt(m.group(2))-1);
                    boolean found = false;
                    for (Shift shift : lr0State.getShiftList())
                    {
                        if (symbol.equals(shift.getSymbol()) && st.equals(shift.getAction()))
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                    {
                        throw new IllegalArgumentException(line+" not found");
                    }
                }
                else
                {
                    LaState laState = (LaState) state;
                    GTerminal symbol = (GTerminal) getSymbol(m.group(1));
                    State st = lpg.getStateList().get(Integer.parseInt(m.group(2))-1);
                    boolean found = false;
                    for (LaShift shift : laState.getShiftList())
                    {
                        if (symbol.equals(shift.getSymbol()) && st.equals(shift.getAct()))
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                    {
                        throw new IllegalArgumentException(line+" not found");
                    }
                }
                continue;
            }
            m = GOTO.matcher(line);
            if (m.matches())
            {
                if (state instanceof Lr0State)
                {
                    Lr0State lr0State = (Lr0State) state;
                    Nonterminal symbol = (Nonterminal) getSymbol(m.group(1));
                    Lr0State st = (Lr0State) lpg.getStateList().get(Integer.parseInt(m.group(2))-1);
                    boolean found = false;
                    for (Goto go : lr0State.getGotoList())
                    {
                        if (symbol.equals(go.getSymbol()) && st.equals(go.getAction()))
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                    {
                        throw new IllegalArgumentException(line+" not found");
                    }
                }
                else
                {
                    throw new IllegalArgumentException(line+" no test!");
                }
                continue;
            }
            m = GTRD.matcher(line);
            if (m.matches())
            {
                if (state instanceof Lr0State)
                {
                    Lr0State lr0State = (Lr0State) state;
                    Nonterminal symbol = (Nonterminal) getSymbol(m.group(1));
                    GRule rl = lpg.getRules().get(Integer.parseInt(m.group(2)));
                    boolean found = false;
                    for (Goto go : lr0State.getGotoList())
                    {
                        if (symbol.equals(go.getSymbol()) && rl.equals(go.getAction()))
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                    {
                        throw new IllegalArgumentException(line+" not found");
                    }
                }
                else
                {
                    throw new IllegalArgumentException(line+" no test!");
                }
                continue;
            }
            m = REDUCE.matcher(line);
            if (m.matches())
            {
                if (state instanceof Lr0State)
                {
                    Lr0State lr0State = (Lr0State) state;
                    GTerminal symbol = (GTerminal) getSymbol(m.group(1));
                    GRule rl = lpg.getRules().get(Integer.parseInt(m.group(2)));
                    boolean found = false;
                    for (Reduce reduce : lr0State.getReduceList())
                    {
                        if (symbol.equals(reduce.getSymbol()) && rl.equals(reduce.getRule()))
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                    {
                        throw new IllegalArgumentException(line+" not found");
                    }
                }
                else
                {
                    LaState laState = (LaState) state;
                    GTerminal symbol = (GTerminal) getSymbol(m.group(1));
                    GRule rl = lpg.getRules().get(Integer.parseInt(m.group(2)));
                    boolean found = false;
                    for (LaReduce reduce : laState.getReduceList())
                    {
                        if (symbol.equals(reduce.getSymbol()) && rl.getNumber() == reduce.getAct().getNumber())
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                    {
                        throw new IllegalArgumentException(line+" not found");
                    }
                }
                continue;
            }
            m = ACCEPT.matcher(line);
            if (m.matches())
            {
                continue;
            }
            m = DEFRED.matcher(line);
            if (m.matches())
            {
                if (state instanceof Lr0State)
                {
                    Lr0State lr0State = (Lr0State) state;
                    GRule rl = lpg.getRules().get(Integer.parseInt(m.group(1)));
                    if (!rl.equals(lr0State.getDefaultReduce()))
                    {
                        throw new IllegalArgumentException(line+" not found");
                    }
                }
                else
                {
                    LaState laState = (LaState) state;
                    GRule rl = lpg.getRules().get(Integer.parseInt(m.group(1)));
                    if (rl.getNumber() != laState.getDefaultRule().getNumber())
                    {
                        throw new IllegalArgumentException(line+" not found");
                    }
                }
                continue;
            }
            
        }
    }

    private boolean produce(String line, Set<Item> items, Matcher m, boolean complete)
    {
        Nonterminal lhs = (Nonterminal) getSymbol(m.group(1));
        Symbol symbol = null;
        GRule rule = null;
        String[] rhs = m.group(2).split("[ ]+");
        int dot = 0;
        int i = 0;
        for (String si : rhs)
        {
            if (".".equals(si))
            {
                symbol = new Empty();
                dot = i;
            }
            else
            {
                if (si.startsWith("."))
                {
                    symbol = getSymbol(si.substring(1));
                    dot = i;
                }
                else
                {
                    Matcher rm = RULENUMBER.matcher(si);
                    if (rm.matches())
                    {
                        rule = lpg.getRules().get(Integer.parseInt(rm.group(1)));
                    }
                }
            }
            i++;
        }
        if (!complete || rule != null)
        {
            boolean found = false;
            for (Item item : items)
            {
                if (
                        symbol.equals(item.getSymbol()) &&
                        dot == item.getDot() &&
                        (rule != null == item.isFinal())
                        )
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                throw new IllegalArgumentException(line+" not found in items");
            }
        }
        return rule != null;
    }

    public static void main(String[] args)
    {
        try
        {
            Object[] oa = new Object[9];
            oa[0] = new int[9];
            test1();
            test2();
            test3();
            float bytesToDFAState = DFACompiler.byteCount/DFACompiler.dfaCount;
            float maxDFAStates = 0x10000/bytesToDFAState;
            System.err.println("bytes/DFAState="+bytesToDFAState+" maxDFAStates/method="+maxDFAStates);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    public static void test1() throws IOException, ReflectiveOperationException
    {
        ParserCompiler pc = new ParserCompiler(El.getTypeElement(ExprExample.class.getCanonicalName()));
        pc.compile();
        ExprExample rp = (ExprExample) pc.newInstance();
        Long rc = (Long) rp.parse("123+2*(3+4)/2+-200");
        System.err.println(rc);
        assert rc == -70;
        File log = new File("C:\\Users\\tkv\\Documents\\Visual Studio 2008\\Projects\\jikespg\\examples\\expr\\expr.l");
        //Tester t = new Tester(log, pc.getLrk());
        //t.test();
    }
    public static void test2() throws IOException, ReflectiveOperationException
    {
        ParserCompiler pc = new ParserCompiler(El.getTypeElement(BnfExample.class.getCanonicalName()));
        pc.compile();
        BnfExample rp = (BnfExample) pc.newInstance();
        rp.parse("a b ::= c d e ::= f g");
    }
    public static void test3() throws IOException, ReflectiveOperationException
    {
        ParserCompiler pc = new ParserCompiler(El.getTypeElement(LegExample.class.getCanonicalName()));
        pc.compile();
        LegExample rp = (LegExample) pc.newInstance();
        rp.parse("ifa=1;");
    }
}
