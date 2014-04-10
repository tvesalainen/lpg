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
package org.vesalainen.regex;

import org.vesalainen.grammar.state.NFA;
import org.vesalainen.grammar.state.NFAState;
import org.vesalainen.grammar.state.Scope;
import org.vesalainen.regex.Regex.Option;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * This is the original hand written regex parser. It is replaced by RegexParser class
 * @author tkv
 */
public class TinyExpressionParser<T> implements RegexParserIntf<T>
{

    protected enum Op {RANGE, LEFT, RIGHT, ERROR, UNION, CONCAT, STAR, QUESS};

    private Deque<NFA<T>> operandStack = new ArrayDeque<>();
    private Deque<Op> operatorStack = new ArrayDeque<>();
    private Scope<NFAState<T>> nfaScope;

    public TinyExpressionParser()
    {
    }

    public NFA<T> createNFA(Scope<NFAState<T>> scope, String expression, T reducer, Option... options)
    {
        nfaScope = scope;
        if (Option.supports(options, Option.CASE_INSENSITIVE))
        {
            throw new UnsupportedOperationException("ignoreCase not supported");
        }
        TinyTokenizer tok = new TinyTokenizer(expression);
        for (Op op : tok)
        {
            if (op.equals(Op.RANGE))
            {
                RangeSet rs = tok.getRangeSet();
                NFA<T> nfa = new NFA<>(scope, rs);
                operandStack.push(nfa);
            }
            else
            {
                if (operatorStack.isEmpty())
                {
                    operatorStack.push(op);
                }
                else
                {
                    if (op.equals(Op.LEFT))
                    {
                        operatorStack.push(op);
                    }
                    else
                    {
                        if (op.equals(Op.RIGHT))
                        {
                            while (!operatorStack.peek().equals(Op.LEFT))
                            {
                                evaluate(operatorStack.pop());
                            }
                            operatorStack.pop();
                        }
                        else
                        {
                            while (!operatorStack.isEmpty() && op.ordinal() <= operatorStack.peek().ordinal())
                            {
                                evaluate(operatorStack.pop());
                            }
                            operatorStack.push(op);
                        }
                    }
                }
            }
        }
        while (!operatorStack.isEmpty())
        {
            evaluate(operatorStack.pop());
        }
        if (operandStack.size() != 1)
        {
            System.err.println("");
        }
        assert operandStack.size() == 1;
        assert operatorStack.isEmpty();
        operandStack.peek().getLast().setToken(reducer);
        return operandStack.pop();
    }

    private void evaluate(Op op)
    {
        switch (op)
        {
            case CONCAT:
                operandStack.push(concat(operandStack.pop(), operandStack.pop()));
                break;
            case STAR:
                operandStack.push(star(operandStack.pop()));
                break;
            case UNION:
                operandStack.push(union(operandStack.pop(), operandStack.pop()));
                break;
            case QUESS:
                operandStack.push(quess(operandStack.pop()));
                break;
            case ERROR:
                error();
                break;
        }
    }

    private NFA<T> concat(NFA<T> nfa2, NFA<T> nfa1)
    {
        nfa1.concat(nfa2);
        return nfa1;
    }

    private NFA<T> star(NFA<T> nfa)
    {
        nfa.star();
        return nfa;
    }

    private NFA<T> union(NFA<T> nfa2, NFA<T> nfa1)
    {
        return new NFA<>(nfaScope, nfa1, nfa2);
    }

    private NFA<T> quess(NFA<T> nfa)
    {
        nfa.opt();
        return nfa;
    }

    private void error()
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            /*
            TinyExpressionParser expr = new TinyExpressionParser();
            NFA<T> nfa = expr.createNFA(new Scope<NFAState<T>>("(b*a)|(a)"), "(b*a)|(a)", 1);
            Validator v = new Validator("a+b*");
            System.err.println(v.match("aabb"));
             * 
             */
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
