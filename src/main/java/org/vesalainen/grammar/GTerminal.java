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
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.Typ;
import org.vesalainen.grammar.state.NFA;
import org.vesalainen.grammar.state.NFAState;
import org.vesalainen.grammar.state.Scope;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.util.HtmlPrinter;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.regex.Regex;
import org.vesalainen.regex.Regex.Option;

/**
 *
 * @author tkv
 */
public class GTerminal extends Symbol implements Comparable<GTerminal>
{
    protected String name;
    protected String toString;
    private String expression;
    private ExecutableElement reducer;
    private Option[] options;
    private boolean whiteSpace;
    private int priority;
    private int base;
    private String document;

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
    GTerminal(int number, String name, String expression, int priority, int base, boolean whiteSpace, String documentation, Option... options)
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
        this.document = documentation;
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
    @Override
    public TypeMirror getReducerType()
    {
        if (reducer == null)
        {
            return Typ.Void;
        }
        else
        {
            return reducer.getReturnType();
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
            return reducer.getParameters().size() == 1;
        }
    }
    public String getExpression()
    {
        return expression;
    }

    public String getUnescapedExpression()
    {
        return getUnescapedExpression(expression);
    }
    public static String getUnescapedExpression(String expr)
    {
        if (expr != null)
        {
            return expr
                    .replace("\u001B", "\\e")
                    .replace("\u0007", "\\a")
                    .replace("\f", "\\f")
                    .replace("\b", "\\b")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
        return expr;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public ExecutableElement getReducer()
    {
        return reducer;
    }

    public void setReducer(ExecutableElement reducer)
    {
        if (reducer != null)
        {
            if (countRealParameters(reducer) > 1)
            {
                throw new IllegalArgumentException("@Terminal reducer "+reducer+" has more than one not @ParserContext parameters");
            }
            if (Typ.isSameType(Typ.getTypeFor(InputReader.class), reducer.getReturnType()))
            {
                throw new IllegalArgumentException("@Terminal reducer "+reducer+" return type is InputReader. Use getFieldRef() instead!");
            }
        }
        this.reducer = reducer;
    }

    private int countRealParameters(ExecutableElement reducer)
    {
        int count = 0;
        for (VariableElement parameter : reducer.getParameters())
        {
            ParserContext parserContext = parameter.getAnnotation(ParserContext.class);
            if (parserContext == null)
            {
                count++;
            }
        }
        return count;
    }
    public String getDocument()
    {
        return document;
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

    @Override
    public boolean isStart()
    {
        return false;
    }

    @Override
    public boolean isNil()
    {
        return false;
    }

    @Override
    public boolean isOmega()
    {
        return false;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public boolean isEof()
    {
        return false;
    }

    @Override
    public boolean isError()
    {
        return false;
    }

    @Override
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

    @Override
    public void print(Appendable p) throws IOException
    {
        p.append(toString);
    }

    public void printAnnotation(Appendable p) throws IOException
    {
        p.append("@Terminal");
        p.append('(');
        p.append("left=\""+name.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") +"\"");
        p.append(", expression=\""+getUnescapedExpression()+"\"");
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
