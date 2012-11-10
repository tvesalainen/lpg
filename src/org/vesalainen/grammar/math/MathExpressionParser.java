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
package org.vesalainen.grammar.math;

import java.io.IOException;
import org.vesalainen.parser.ParserFactory;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.annotation.Terminals;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;

/**
 * @author tkv
 */
@GenClassname("org.vesalainen.grammar.math.MathExpressionParserImpl")
@GrammarDef()
@Terminals({
    @Terminal(left="PLUS", expression="\\+"),
    @Terminal(left="MINUS", expression="\\-"),
    @Terminal(left="STAR", expression="\\*"),
    @Terminal(left="SLASH", expression="/"),
    @Terminal(left="PERCENT", expression="%"),
    @Terminal(left="EXP", expression="\\^"),
    @Terminal(left="COMMA", expression="\\,"),
    @Terminal(left="PIPE", expression="\\|"),
    @Terminal(left="EXCL", expression="!"),
    @Terminal(left="LBRACKET", expression="\\["),
    @Terminal(left="RBRACKET", expression="\\]"),
    @Terminal(left="LPAREN", expression="\\("),
    @Terminal(left="RPAREN", expression="\\)")
})
@Rules({
    @Rule(left="expression", value="term"),
    @Rule(left="term", value="factor"),
    @Rule(left="factor", value="atom"),
    @Rule(left="atom", value={"LPAREN", "expression", "RPAREN"}),
    @Rule(left="expressionList"),
    @Rule(left="expressionList", value={"expression"}),
    @Rule(left="expressionList", value={"expressionList", "COMMA", "expression"}),
    @Rule(left="funcArgs", value={"expressionList", "RPAREN"}),
    @Rule(left="indexes")
})
public abstract class MathExpressionParser
{
    @ParseMethod(start="expression",  size=1024, whiteSpace={"whiteSpace"})
    protected abstract void parse(String expression, @ParserContext ExpressionHandler handler);
    
    @Rule(left="expression", value={"expression", "PLUS", "term"})
    protected void add(@ParserContext ExpressionHandler handler) throws IOException
    {
        handler.add();
        handler.stack(-1);
    }
    @Rule(left="expression", value={"expression", "MINUS", "term"})
    protected void subtract(@ParserContext ExpressionHandler handler) throws IOException
    {
        handler.subtract();
        handler.stack(-1);
    }
    @Rule(left="term", value={"term", "STAR", "factor"})
    protected void mul(@ParserContext ExpressionHandler handler) throws IOException
    {
        handler.mul();
        handler.stack(-1);
    }
    @Rule(left="term", value={"term", "SLASH", "factor"})
    protected void div(@ParserContext ExpressionHandler handler) throws IOException
    {
        handler.div();
        handler.stack(-1);
    }
    @Rule(left="term", value={"term", "PERCENT", "factor"})
    protected void mod(@ParserContext ExpressionHandler handler) throws IOException
    {
        handler.mod();
        handler.stack(-1);
    }
    /*
    @Rule(left="factor", value={"MINUS", "atom"})
    protected void neg(@ParserContext ExpressionHandler handler) throws IOException
    {
        handler.neg();
    }
    * 
    */
    @Rule(left="factor", value={"atom", "EXP", "factor"})
    protected void power(@ParserContext ExpressionHandler handler) throws IOException
    {
        handler.invoke("pow", 2);
        handler.stack(-1);
    }
    @Rule(left="atom", value={"number"})
    protected void num(String number, @ParserContext ExpressionHandler handler) throws IOException
    {
        handler.number(number);
        handler.stack(1);
    }
    @Rule(left="atom", value={"variable", "indexes"})
    protected void id(String identifier, @ParserContext ExpressionHandler handler)
    {
        handler.arrayIndexMode(false);
        handler.stack(1);
    }
    @Rule(left="atom", value={"MINUS", "variable", "indexes"})
    protected void negId(String identifier, @ParserContext ExpressionHandler handler) throws IOException
    {
        handler.arrayIndexMode(false);
        handler.neg();
        handler.stack(1);
    }
    @Rule("identifier")
    protected String variable(String identifier, @ParserContext ExpressionHandler handler) throws IOException
    {
        handler.loadVariable(identifier);
        handler.arrayIndexMode(true);
        return identifier;
    }
    @Rule(left="indexes", value={"indexes", "LBRACKET", "expression", "RBRACKET"})
    protected void index(@ParserContext ExpressionHandler handler) throws IOException
    {
        handler.arrayIndex();
        handler.stack(-1);
    }
    @Rule(left="atom", value={"PIPE", "expression", "PIPE"})
    protected void abs(@ParserContext ExpressionHandler handler) throws IOException
    {
        handler.abs();
    }
    @Rule(left="atom", value={"atom", "EXCL"})
    protected void factorial(@ParserContext ExpressionHandler handler) throws IOException
    {
        handler.invoke("factorial", 1);
    }
    @Rule(left="atom", value={"funcName", "funcArgs"})
    protected void func(String funcName, @ParserContext ExpressionHandler handler) throws IOException
    {
        int stack = handler.pop();
        handler.invoke(funcName, stack);
        handler.stack(-stack+1);
    }
    @Rule(value={"identifier", "LPAREN", })
    protected String funcName(String identifier, @ParserContext ExpressionHandler handler)
    {
        handler.push();
        return identifier;
    }
    @Terminal(expression="[a-zA-Z][a-zA-Z0-9_]*")
    protected abstract String identifier(String value);

    @Terminal(expression="[\\+\\-]?[0-9]+")
    protected abstract String integer(String value);

    @Terminal(expression="[\\+\\-]?[0-9]+(\\.[0-9]+)?([eE][\\+\\-]?[0-9]+)?")
    protected abstract String number(String value);

    @Terminal(expression="[ \t\r\n]+")
    protected abstract void whiteSpace();
    
    public static void main(String[] args)
    {
        try
        {
            MathExpressionParser rp = (MathExpressionParser) ParserFactory.getParserInstance(MathExpressionParser.class);
            //rp.parse("1 + (1-2)^n+max(1, 2)+|x| - 1.23e-12 + n! + min(1+2,2,3,4,5)", new PrintingHandler());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
