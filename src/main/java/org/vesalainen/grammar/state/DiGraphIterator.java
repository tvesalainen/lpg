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
package org.vesalainen.grammar.state;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import org.vesalainen.regex.Regex;

/**
 * DiGraphIterator implements iterator over all vertices a given vertex has a connection.
 * This is an implementation of DiGraph algorithm.
 * @author Timo Vesalainen
 * @see DiGraph
 */
public final class DiGraphIterator<X extends Vertex> implements Iterator<X>
{
    private static final int INFINITY = 9999999;

    private Deque<X> stack = new ArrayDeque<>();
    private Map<X,Integer> indexMap = new HashMap<>();
    private Deque<Ctx> context = new ArrayDeque<>();
    private X next;

    public DiGraphIterator(X root)
    {
        next = enter(root);
    }

    @Override
    public boolean hasNext()
    {
        return next != null;
    }

    @Override
    public X next()
    {
        if (next == null)
        {
            throw new NoSuchElementException();
        }
        X n = next;
        next = traverse();
        return n;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private X enter(X x)
    {
        stack.push(x);

        int d = stack.size();
        setIndexOf(x, d);

        Iterator<X> i = x.edges().iterator();
        context.push(new Ctx(x, i, d));
        return x;
    }
    private X traverse()
    {
        while (!context.isEmpty())
        {
            Ctx ctx = context.peek();
            X x = ctx.x;
            while (ctx.i.hasNext())
            {
                X y = ctx.i.next();
                if (indexOf(y) == 0)
                {
                    return enter(y);
                }
                setIndexOf(x, Math.min(indexOf(x), indexOf(y)));
            }
            pop();
        }
        return null;
    }
    
    private void pop()
    {
        Ctx ctx = context.peek();
        X x = ctx.x;
        if (indexOf(x) == ctx.d)
        {
            X s = stack.peek();
            while (!s.equals(x))
            {
                stack.pop();
                setIndexOf(s, INFINITY);
                s = stack.peek();
            }
            setIndexOf(x, INFINITY);
            stack.pop();
        }
        ctx = context.pop();
    }
    
    private void setIndexOf(X state, int index)
    {
        indexMap.put(state, index);
    }
    
    private int indexOf(X state)
    {
        Integer i = indexMap.get(state);
        if (i == null)
        {
            return 0;
        }
        else
        {
            return i;
        }
    }
    private class Ctx
    {
        X x;
        Iterator<X> i;
        int d;

        public Ctx(X x, Iterator<X> i, int d)
        {
            this.x = x;
            this.i = i;
            this.d = d;
        }

    }
    /**
     * @param args the command line arguments
     */ 
    public static void main(String[] args)    
    {
        try
        {
            DFA<Integer> dfa = Regex.createDFA("ashjkahsj(ha|kjkajdkasdj)+h|yrquiyqiwdioas|(kdajksdfh){2,3}ajkshdjkah|ajhdajsdjkahsdjkah");
            DiGraphIterator<DFAState<Integer>> i = new DiGraphIterator<>(dfa.getRoot());
            while (i.hasNext())
            {
                System.err.println(i.next());
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
