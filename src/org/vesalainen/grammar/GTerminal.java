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
import java.lang.reflect.Member;
import org.vesalainen.regex.Regex.Option;
import java.lang.reflect.Type;
import org.vesalainen.bcc.type.Generics;
import org.vesalainen.grammar.state.NFA;
import org.vesalainen.grammar.state.NFAState;
import org.vesalainen.grammar.state.Scope;
import org.vesalainen.parser.util.HtmlPrinter;
import org.vesalainen.regex.Regex;

/**
 *
 * @author tkv
 */
public class GTerminal extends Symbol implements Comparable<GTerminal>
{
    protected String name;
    protected String toString;
    private String expression;
    private Member reducer;
    private Option[] options;
    private boolean whiteSpace;
    private int priority;
    private int base;

    protected GTerminal(int number, String name)
    {
        super(number);
        this.name = name;
    }

    /**
     * Named terminal
     * @param name
     * @param expression
     */
    GTerminal(int number, String name, String expression, int priority, int base, boolean whiteSpace, Option... options)
    {
        super(number);
        if (expression.isEmpty())
        {
            throw new IllegalArgumentException("empty string not allowed as terminal");
        }
        this.name = name;
        this.toString = name.replace("\\", "").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
        this.expression = expression;
        this.priority = priority;
        this.base = base;
        this.options = options;
        this.whiteSpace = whiteSpace;
    }

    public NFA<Integer> createNFA(Scope<NFAState<Integer>> scope) throws IOException
    {
        NFA<Integer> nfa = Regex.createNFA(scope, expression, getNumber(), options);
        NFAState<Integer> last = nfa.getLast();
        last.setToken(getNumber());
        last.setPriority(priority);
        if (Option.supports(options, Option.ACCEPT_IMMEDIATELY))
        {
            last.setAcceptImmediately(true);
        }
        return nfa;
    }

    public int getBase()
    {
        return base;
    }
    
    public int getPriority()
    {
        return priority;
    }

    public boolean isWhiteSpace()
    {
        return whiteSpace;
    }

    public boolean isAnonymous()
    {
        return  expression != null &&
                name.length() ==  expression.length()+2 &&
                expression.equals(name.substring(1, name.length()-1));
    }
    
    public Option[] getOptions()
    {
        return options;
    }
    public Type getReducerType()
    {
        if (reducer == null)
        {
            return void.class;
        }
        else
        {
            return Generics.getReturnType(reducer);
        }
    }

    public boolean isStoring()
    {
        if (reducer == null)
        {
            return false;
        }
        else
        {
            return Generics.getParameterTypes(reducer).length == 1;
        }
    }
    public String getExpression()
    {
        return expression;
    }

    public String getUnescapedExpression()
    {
        return expression
                .replace("\u001B", "\\e")
                .replace("\u0007", "\\a")
                .replace("\f", "\\f")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public String getName()
    {
        return name;
    }

    public Member getReducer()
    {
        return reducer;
    }

    public void setReducer(Member reducer)
    {
        if (reducer != null && Generics.getParameterTypes(reducer).length > 2)
        {
            throw new IllegalArgumentException("Terminal reducer "+reducer+" has more than two parameters");
        }
        this.reducer = reducer;
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
        final GTerminal other = (GTerminal) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return toString;    // + "("+number+")";
    }

    public boolean isStart()
    {
        return false;
    }

    public boolean isNil()
    {
        return false;
    }

    public boolean isOmega()
    {
        return false;
    }

    public boolean isEmpty()
    {
        return false;
    }

    public boolean isEof()
    {
        return false;
    }

    public boolean isError()
    {
        return false;
    }

    public int compareTo(GTerminal o)
    {
        return getNumber() - o.getNumber();
    }

    @Override
    public void print(HtmlPrinter p) throws IOException
    {
        if (isAnonymous())
        {
            p.print(toString);
        }
        else
        {
            p.linkSource("#"+toString, toString);
        }
    }
    public static void main(String... args)
    {
        try
        {
            GTerminal t = new GTerminal(1, "nl", "\n", 1, 10, true);
            System.err.println(t.getUnescapedExpression());
        }
        catch (Exception ex)
        {
            
        }
    }
}
