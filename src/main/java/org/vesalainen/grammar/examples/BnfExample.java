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

import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.annotation.Terminals;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.ParserInfo;
import org.vesalainen.parser.Trace;
import org.vesalainen.parser.TraceHelper;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.TraceMethod;
import org.vesalainen.parser.util.InputReader;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@GenClassname("org.vesalainen.grammar.examples.BnfExampleImpl")
@GrammarDef()
@Terminals({
    @Terminal(left="PRODUCES", expression="::="),
    @Terminal(left="OR", expression="\\|")
})
@Rules({
    @Rule(left="bnf"),
    @Rule(left="bnf", value={"bnf", "rules"}),
    @Rule(left="rules", value={"rule"})
})
public abstract class BnfExample implements ParserInfo
{
    @ParseMethod(start="bnf", wideIndex=true)
    public abstract void parse(String txt);
    @Rule(left="rules", value={"rules", "OR", "symbol_list"})
    public void rules(List<String> list)
    {
        System.err.println("rule 4 "+list);
    }
    @Rule(left="rule", value={"SYMBOL", "SYMBOL", "PRODUCES", "symbol_list"})
    public void rule(String s1, String s2, List<String> list)
    {
        System.err.println("rule 5 "+s1+" "+s2+" ::= "+list);
    }
    @Rule(left="symbol_list")
    public List<String> symbolList()
    {
        System.err.println("rule 6 ");
        return new ArrayList<String>();
    }
    @Rule(left="symbol_list", value={"symbol_list", "SYMBOL"})
    public List<String> symbolList(List<String> list, String symbol)
    {
        System.err.println("rule 7 "+list+" "+symbol);
        list.add(symbol);
        return list;
    }
    @Terminal(left="SYMBOL", expression="[ ]*[a-z]+[ ]*")
    public String symbol(String symbol)
    {
        return symbol;
    }
    @TraceMethod
    protected void trace(
            int ord,
            int ctx,
            @ParserContext("$inputReader") InputReader reader,
            @ParserContext("$token") int token,
            @ParserContext("$laToken") int laToken,
            @ParserContext("$curTok") int curtok,
            @ParserContext("$stateStack") int[] stack,
            @ParserContext("$sp") int sp,
            @ParserContext("$typeStack") int[] typeStack,
            @ParserContext("$valueStack") Object[] valueStack
            )
    {
        Trace trace = Trace.values()[ord];
        switch (trace)
        {
            case STATE:
                System.err.println("state "+stack[sp]);
                break;
            case INPUT:
                if (ctx >= 0)
                {
                    System.err.println("input"+ctx+"='"+reader.getString()+"' token="+getToken(token));
                }
                else
                {
                    System.err.println("re input='"+reader.getString()+"' token="+getToken(token));
                }
                break;
            case LAINPUT:
                if (ctx >= 0)
                {
                    System.err.println("lainput"+ctx+"='"+reader.getString()+"' token="+getToken(laToken));
                }
                else
                {
                    System.err.println("re lainput='"+reader.getString()+"' token="+getToken(laToken));
                }
                break;
            case PUSHVALUE:
                System.err.println("push value");
                break;
            case EXITLA:
                System.err.println("exit La");
                TraceHelper.printStacks(System.err, stack, typeStack, valueStack, sp);
                break;
            case BEFOREREDUCE:
                System.err.println("Before reducing rule "+getRule(ctx));
                TraceHelper.printStacks(System.err, stack, typeStack, valueStack, sp);
                break;
            case AFTERREDUCE:
                System.err.println("After reducing rule "+getRule(ctx));
                TraceHelper.printStacks(System.err, stack, typeStack, valueStack, sp);
                break;
            case GOTO:
                System.err.println("Goto "+ctx);
                break;
            case SHIFT:
                System.err.println("Shift "+ctx);
                break;
            case SHRD:
                System.err.println("Shift/Reduce");
                break;
            case LASHRD:
                System.err.println("La Shift/Reduce");
                break;
            case GTRD:
                System.err.println("Goto/Reduce");
                break;
            case LASHIFT:
                System.err.println("LaShift State "+ctx);
                TraceHelper.printStacks(System.err, stack, typeStack, valueStack, sp);
                break;
            default:
                System.err.println("unknown action "+trace);
                break;
        }
    }

    public static void main(String[] args)
    {
        try
        {
            BnfExample rp = (BnfExample) GenClassFactory.getGenInstance(BnfExample.class);
            File log = new File("C:\\Users\\tkv\\Documents\\Visual Studio 2008\\Projects\\jikespg\\examples\\bnf\\bnf.l");
            //Tester t = new Tester(log, pc.getLrk());
            //t.test();
            //rp.traceLevel(Trace.PUSHVALUE);
            rp.parse("a b ::= c d e ::= f g");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
