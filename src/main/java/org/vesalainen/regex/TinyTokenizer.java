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

import org.vesalainen.regex.TinyExpressionParser.Op;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
/**
 * This is part of the original hand written part of regex parser. It is replaced by RegexParser class
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
class TinyTokenizer implements Iterator<Op>, Iterable<Op>
{
    private EscapeResolver resolver;
    private RangeSet current;
    private boolean concat;
    private Deque<Op> queue = new ArrayDeque<Op>();

    public TinyTokenizer(String expression)
    {
        resolver = new EscapeResolver(expression);
    }

    private static RangeSet getInstance(int cc, boolean escaped)
    {
        RangeSet rs = null;
        if (escaped)
        {
            switch (cc)
            {
                case 'd':
                    rs = new RangeSet();
                    rs.add(new CharRange('0', '9'+1));
                    return rs;
                case 'D':
                    rs = new RangeSet();
                    rs.add(new CharRange('0', '9'+1));
                    return rs.complement();
                case 's':
                    rs = new RangeSet();
                    rs.add(new CharRange(' '));
                    rs.add(new CharRange('\t'));
                    rs.add(new CharRange('\n'));
                    rs.add(new CharRange(0x0B));
                    rs.add(new CharRange('\f'));
                    rs.add(new CharRange('\r'));
                    return rs;
                case 'S':
                    rs = new RangeSet();
                    rs.add(new CharRange(' '));
                    rs.add(new CharRange('\t'));
                    rs.add(new CharRange('\n'));
                    rs.add(new CharRange(0x0B));
                    rs.add(new CharRange('\f'));
                    rs.add(new CharRange('\r'));
                    return rs.complement();
                case 'w':
                    rs = new RangeSet();
                    rs.add(new CharRange('a', 'z'+1));
                    rs.add(new CharRange('A', 'Z'+1));
                    rs.add(new CharRange('0', '9'+1));
                    rs.add(new CharRange('_'));
                    return rs;
                case 'W':
                    rs = new RangeSet();
                    rs.add(new CharRange('a', 'z'+1));
                    rs.add(new CharRange('A', 'Z'+1));
                    rs.add(new CharRange('0', '9'+1));
                    rs.add(new CharRange('_'));
                    return rs.complement();
                case 'p':
                    throw new UnsupportedOperationException("Posix escapes not supported");
                default:
                    rs = new RangeSet();
                    rs.add(new CharRange(cc));
                    return rs;
            }
        }
        else
        {
            switch (cc)
            {
                case '.':
                    rs = new RangeSet();
                    rs.add(new CharRange(0, Integer.MAX_VALUE));
                    return rs;
                default:
                    rs = new RangeSet();
                    rs.add(new CharRange(cc));
                    return rs;
            }
        }
    }

    public Op next()
    {
        if (!queue.isEmpty())
        {
            return queue.pollFirst();
        }
        int cc = resolver.next();
        if (!resolver.isEscaped())
        {
            switch (cc)
            {
                case '(':
                    if (concat)
                    {
                        queue.add(Op.LEFT);
                        concat = false;
                        return Op.CONCAT;
                    }
                    else
                    {
                        return Op.LEFT;
                    }
                case ')':
                    concat = true;
                    return Op.RIGHT;
                case '*':
                    return Op.STAR;
                case '?':
                    return Op.QUESS;
                case '+':
                    queue.add(Op.RANGE);
                    queue.add(Op.STAR);
                    return Op.CONCAT;
                case '|':
                    concat = false;
                    return Op.UNION;
                case '[':
                    current = parseRangeSet();
                    if (concat)
                    {
                        queue.add(Op.RANGE);
                        return Op.CONCAT;
                    }
                    else
                    {
                        concat = true;
                        return Op.RANGE;
                    }
                case '{':
                    try
                    {
                        fillQueue();
                    }
                    catch (SyntaxErrorException ex)
                    {
                        return Op.ERROR;
                    }
                    return queue.pollFirst();
            }
        }
        current = getInstance(cc, resolver.isEscaped());
        if (concat)
        {
            queue.add(Op.RANGE);
            return Op.CONCAT;
        }
        else
        {
            concat = true;
            return Op.RANGE;
        }
    }

    public boolean hasNext()
    {
        return resolver.hasNext() || !queue.isEmpty();
    }

    public void remove()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public RangeSet getRangeSet()
    {
        return current;
    }

    public Iterator<Op> iterator()
    {
        return this;
    }

    private RangeSet parseRangeSet()
    {
        RangeSet set = new RangeSet();
        boolean neg = false;
        int cc = resolver.next();
        if (!resolver.isEscaped() && cc == '^')
        {
            neg = true;
            cc = resolver.next();
        }
        while (
                !resolver.isEscaped() && cc != ']' ||
                resolver.isEscaped()
                )
        {
            int nn = resolver.peek();
            if (!resolver.isEscaped() && nn == '-')
            {
                resolver.next();
                nn = resolver.next();
                set.add(cc, nn+1);
            }
            else
            {
                set.add(cc);
            }
            cc = resolver.next();
        }
        if (neg)
        {
            set = set.complement();
        }
        return set;
    }

    private void fillQueue() throws SyntaxErrorException
    {
        int min = 0;
        int cc = resolver.next();
        while (Character.isDigit(cc))
        {
            min = 10*min+Character.digit(cc, 10);
            cc = resolver.next();
        }
        for (int ii=1;ii<min;ii++)
        {
            queue.add(Op.CONCAT);
            queue.add(Op.RANGE);
        }
        if (cc == ',')
        {
            int max = 0;
            cc = resolver.next();
            while (Character.isDigit(cc))
            {
                max = 10*max+Character.digit(cc, 10);
                cc = resolver.next();
            }
            if (cc != '}')
            {
                throw new SyntaxErrorException("Unexpected char '"+cc+"'", resolver.getIndex());
            }
            if (max >= min)
            {
                int gap = max-min;
                for (int ii=0;ii<gap;ii++)
                {
                    queue.add(Op.CONCAT);
                    queue.add(Op.RANGE);
                    queue.add(Op.QUESS);
                }
            }
            else
            {
                if (max != 0)
                {
                    throw new SyntaxErrorException("Illegal quantifier", resolver.getIndex());
                }
                queue.add(Op.CONCAT);
                queue.add(Op.RANGE);
                queue.add(Op.STAR);
            }
        }
        else
        {
            if (cc != '}')
            {
                throw new SyntaxErrorException("Unexpected char '"+cc+"'", resolver.getIndex());
            }
        }
    }
}
