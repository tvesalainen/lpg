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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.vesalainen.parser.GenClassFactory;
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
    @Rule(left="funcArgs", value={"expressionList", "RPAREN"}),
    @Rule(left="indexes")
})
public abstract class MathExpressionParser
{
    @ParseMethod(start="expression",  size=1024, whiteSpace={"whiteSpace"})
    public abstract DEH parse(String expression, @ParserContext("handler") MethodExpressionHandler handler);
    
    @Rule("term")
    protected DEH expression(DEH term)
    {
        return term;
    }
    @Rule("factor")
    protected DEH term(DEH factor)
    {
        return factor;
    }
    @Rule("atom")
    protected DEH factor(DEH atom)
    {
        return atom;
    }
    @Rule({"LPAREN", "expression", "RPAREN"})
    protected DEH atom(DEH expression)
    {
        return expression;
    }
    @Rule
    protected List<DEH> expressionList()
    {
        return new ArrayList<>();
    }
    @Rule("expression")
    protected List<DEH> expressionList(DEH expression)
    {
        ArrayList<DEH> list = new ArrayList<>();
        list.add(expression);
        return list;
    }
    @Rule({"expressionList", "COMMA", "expression"})
    protected List<DEH> expressionList(List<DEH> list, DEH expression)
    {
        list.add(expression);
        return list;
    }
    @Rule(left="expression", value={"expression", "PLUS", "term"})
    protected DEH add(DEH expression, DEH term) throws IOException
    {
        expression.append(term);
        expression.getProxy().add();
        return expression;
    }
    @Rule(left="expression", value={"expression", "MINUS", "term"})
    protected DEH subtract(DEH expression, DEH term) throws IOException
    {
        expression.append(term);
        expression.getProxy().subtract();
        return expression;
    }
    @Rule(left="term", value={"term", "STAR", "factor"})
    protected DEH mul(DEH term, DEH factor) throws IOException
    {
        term.append(factor);
        term.getProxy().mul();
        return term;
    }
    @Rule(left="term", value={"term", "SLASH", "factor"})
    protected DEH div(DEH term, DEH factor) throws IOException
    {
        term.append(factor);
        term.getProxy().div();
        return term;
    }
    @Rule(left="term", value={"term", "PERCENT", "factor"})
    protected DEH mod(DEH term, DEH factor) throws IOException
    {
        term.append(factor);
        term.getProxy().mod();
        return term;
    }
    @Rule(left="atom", value={"number"})
    protected DEH num(String number) throws IOException
    {
        DEH atom = new DEH();
        atom.getProxy().number(number);
        return atom;
    }
    @Rule(left="atom", value={"PIPE", "expression", "PIPE"})
    protected DEH abs(DEH expression, @ParserContext("handler") MethodExpressionHandler handler) throws IOException
    {
        List<DEH> args = new ArrayList<>();
        args.add(expression);
        return func("abs", args, handler);
    }
    @Rule(left="factor", value={"atom", "EXP", "factor"})
    protected DEH power(DEH atom, DEH factor, @ParserContext("handler") MethodExpressionHandler handler) throws IOException
    {
        List<DEH> args = new ArrayList<>();
        args.add(atom);
        args.add(factor);
        return func("pow", args, handler);
    }
    @Rule(left="atom", value={"atom", "EXCL"})
    protected DEH factorial(DEH atom, @ParserContext("handler") MethodExpressionHandler handler) throws IOException
    {
        List<DEH> args = new ArrayList<>();
        args.add(atom);
        return func("factorial", args, handler);
    }
    @Rule(left="neg")
    protected boolean none()
    {
        return false;
    }
    @Rule(left="neg", value="MINUS")
    protected boolean minus()
    {
        return true;
    }
    @Rule
    protected List<DEH> indexList() throws IOException
    {
        return new ArrayList<>();
    }
    @Rule({"indexList", "LBRACKET", "expression", "RBRACKET"})
    protected List<DEH> indexList(List<DEH> list, DEH expression) throws IOException
    {
        list.add(expression);
        return list;
    }
    @Rule(left="atom", value={"neg", "identifier", "indexList"})
    protected DEH variable(boolean neg, String identifier, List<DEH> indexList) throws IOException
    {
        DEH atom = new DEH();
        ExpressionHandler proxy = atom.getProxy();
        proxy.loadVariable(identifier);
        if (indexList != null && !indexList.isEmpty())
        {
            Iterator<DEH> iterator = indexList.iterator();
            while (iterator.hasNext())
            {
                DEH expr = iterator.next();
                proxy.setIndex(true);
                atom.append(expr);
                proxy.setIndex(false);
                if (iterator.hasNext())
                {
                    proxy.loadArray();
                }
                else
                {
                    proxy.loadArrayItem();
                }
            }
        }
        if (neg)
        {
            proxy.neg();
        }
        return atom;
    }
    @Rule(left="atom", value={"identifier", "LPAREN", "expressionList", "RPAREN"})
    protected DEH func(String identifier, List<DEH> funcArgs, @ParserContext("handler") MethodExpressionHandler handler) throws IOException
    {
        DEH atom = new DEH();
        ExpressionHandler proxy = atom.getProxy();
        Method method = handler.findMethod(identifier, funcArgs.size());
        Class<?>[] parameters = method.getParameterTypes();
        assert funcArgs.size() == parameters.length;
        int index = 0;
        for (DEH expr : funcArgs)
        {
            atom.append(expr);
            proxy.convertTo(parameters[index++]);
        }
        proxy.invoke(method);
        proxy.convertFrom(method.getReturnType());
        return atom;
    }
    
    // -------------------
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
            MathExpressionParser rp = (MathExpressionParser) GenClassFactory.getGenInstance(MathExpressionParser.class);
            //rp.parse("1 + (1-2)^n+max(1, 2)+|x| - 1.23e-12 + n! + min(1+2,2,3,4,5)", new PrintingHandler());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
