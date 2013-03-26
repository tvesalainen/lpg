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

import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.ParserInfo;
import org.vesalainen.parser.Trace;
import org.vesalainen.parser.TraceHelper;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.annotation.Terminals;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.TraceMethod;
import org.vesalainen.parser.util.InputReader;

/**
 * goal::=
 * goal::=expression
 * expression::=expression '+' term
 * expression::=expression '-' term
 * expression::=term
 * term::=term '*' factor
 * term::=term '/' factor
 * term::=factor
 * factor::=number
 * factor::='-' number
 * factor::='(' expression ')'
 * @author tkv
 */
@GenClassname("org.vesalainen.grammar.examples.ExprExampleImpl")
@GrammarDef(maxStack=9)
@Terminals({
    @Terminal(left="PLUS", expression="\\+"),
    @Terminal(left="MINUS", expression="\\-"),
    @Terminal(left="STAR", expression="\\*"),
    @Terminal(left="SLASH", expression="/"),
    @Terminal(left="LPAREN", expression="\\("),
    @Terminal(left="RPAREN", expression="\\)")
})
public abstract class ExprExample implements ParserInfo
{
    @ParseMethod(start="Goal")
    public abstract long parse(String txt);
    @Terminal(left="NUMBER", expression="[0-9]+")
    public abstract long number(long str);
    @Rule(left="Goal")
    public long goal()
    {
        return 0;
    }
    @Rule(left="Goal", value={"Expression"})
    public abstract long goal(long expr);
    @Rule(left="Expression", value={"Expression", "PLUS", "Term"})
    public long plusExpression(long expr, long term)
    {
        System.err.println(expr+" + "+term);
        return expr + term;
    }
    @Rule(left="Expression", value={"Expression", "MINUS", "Term"})
    public long minusExpression(long expr, long term)
    {
        System.err.println(expr+" - "+term);
        return expr - term;
    }
    @Rule(left="Expression", value={"Term"})
    public long termExpression(long term)
    {
        System.err.println("termExpression("+term);
        return term;
    }
    @Rule(left="Term", value={"Term", "STAR", "Factor"})
    public long starTerm(long term, long factor)
    {
        System.err.println(term+" * "+factor);
        return term * factor;
    }
    @Rule(left="Term", value={"Term", "SLASH", "Factor"})
    public long slashTerm(long term, long factor)
    {
        System.err.println(term+" / "+factor);
        return term / factor;
    }
    @Rule(left="Term", value={"Factor"})
    public long factorTerm(long term)
    {
        System.err.println("factorTerm("+term);
        return term;
    }
    @Rule(left="Factor", value={"NUMBER"})
    public long numberFactor(long term)
    {
        System.err.println("numberFactor("+term);
        return term;
    }
    @Rule(left="Factor", value={"MINUS", "NUMBER"})
    public long minusFactor(long term)
    {
        return -term;
    }
    @Rule(left="Factor", value={"LPAREN", "Expression", "RPAREN"})
    public long expressionFactor(long term)
    {
        return term;
    }
    @TraceMethod
    protected void trace(
            int ord,
            int ctx,
            @ParserContext("$inputReader") InputReader reader,
            @ParserContext("$token") int token,
            //@ParserContext("$laToken") int laToken,
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
                /*
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
                * 
                */
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
            ExprExample rp = (ExprExample) GenClassFactory.getGenInstance(ExprExample.class);
            System.err.println(rp.parse("1+a-2"));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
