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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * <p>CharRange represents a 32 bit unicode character range. Regular expression [a-z]
 is transformed to 'a' - 'z'+1 range. Single character a is transformed to
 'a' - 'a'+1

 <p>Boundary matchers are implemented as negative ranges
 *
 * <p>Ranges are constant; their values cannot be changed after they are created.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CharRange implements Comparable<CharRange>
{
    private int from;
    private int to;

    public enum BoundaryType {
        /**
         * Beginning of line
         */
        BOL,
        /**
         * End of line
         */
        EOL,
        /**
         * Word boundary
         */
        WB,
        /**
         * Non word boundary
         */
        NWB,
        /**
         * Beginning of input
         */
        BOI,
        /**
         * End of previous match
         */
        EOPM,
        /**
         * Not implemented
         */
        EOIL,
        /**
         * End of input or end of line
         */
        EOI
    }
    /**
     * Constructs a boundary matcher
     * @param type
     */
    public CharRange(BoundaryType type)
    {
        from = -1 - type.ordinal();
        to = from+1;
    }

    protected CharRange()
    {
    }
    /**
     * Constructs a single character range
     * @param a
     */
    public CharRange(int a)
    {
        this.from = a;
        this.to = a+1;
    }
    /**
     * Constructs a character range
     * @param from inclusive
     * @param to exclusive
     */
    public CharRange(int from, int to)
    {
        assert from < to;
        this.from = from;
        this.to = to;
    }
    /**
     * Returns the length of accepted character. Returns 1 for normal and 0 for boundary matchers.
     * @return
     */
    public int getLength()
    {
        if (from >= 0)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }
    /**
     * Return true if range is character range. (not boundary)
     * @return
     */
    public boolean isNormal()
    {
        return from >= 0;
    }
    /**
     * Returns true range is boundary matcher
     * @return
     */
    public boolean isBoundaryMatcher()
    {
        return from < 0 && from > -2 - BoundaryType.values().length;
    }
    /**
     * Returns BoundaryMatchers ordinal
     * @return
     */
    public int getBoundaryMatcher()
    {
        assert isBoundaryMatcher();
        return -from-1;
    }
    /**
     * Returns true if argument is inside this range
     * @param r
     * @return
     */
    public boolean contains(CharRange r)
    {
        if (r != null)
        {
            return (from <= r.from) && (to >= r.to);
        }
        else
        {
            return false;
        }
    }
    /**
     * Returns true if from - to is inside this range
     * @param from
     * @param to
     * @return
     */
    public boolean contains(int from, int to)
    {
        return (this.from <= from) && (this.to >= to);
    }
    /**
     * Returns true is n is inside this range
     * @param n
     * @return
     */
    public boolean contains(int n)
    {
        return accept(n);
    }
    /**
     * Returns true is n is inside this range
     * @param n
     * @return
     */
    public boolean accept(int n)
    {
        return (from <= n) && (to > n);
    }
    /**
     * Returns true if this and other have common characters
     * @param other
     * @return
     */
    public boolean intersect(CharRange other)
    {
        return other.accept(from) || other.accept(to-1) || accept(other.from) || accept(other.to-1);
    }
    /**
     * Returns a list of ranges that together gather the same characters as r1 and r2.
     * None of the resulting ranges doesn't intersect each other.
     * @param r1
     * @param r2
     * @return
     */
    public static List<CharRange> removeOverlap(CharRange r1, CharRange r2)
    {
        assert r1.intersect(r2);
        List<CharRange> list = new ArrayList<CharRange>();
        Set<Integer> set = new TreeSet<Integer>();
        set.add(r1.getFrom());
        set.add(r1.getTo());
        set.add(r2.getFrom());
        set.add(r2.getTo());
        int p = 0;
        for (int r : set)
        {
            if (p != 0)
            {
                list.add(new CharRange(p, r));
            }
            p = r;
        }
        return list;
    }
    /**
     * Returns the lowest character value
     * @return
     */
    public int getFrom()
    {
        return from;
    }
    /**
     * Returns the greatest character value +1
     * @return
     */
    public int getTo()
    {
        return to;
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
        final CharRange other = (CharRange) obj;
        if (this.from != other.from)
        {
            return false;
        }
        if (this.to != other.to)
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 47 * hash + this.from;
        hash = 47 * hash + this.to;
        return hash;
    }

    @Override
    public String toString()
    {
        if ((to - from) == 1)
        {
            return toString(from);
        }
        else
        {
            if (to < Integer.MAX_VALUE)
            {
                int t = to -1;
                return toString(from) + "-" + toString(t);
            }
            else
            {
                return toString(from) + "-";
            }
        }
    }

    private String toString(int cc)
    {
        if (cc >= ' ' && cc <= 255)
        {
            return String.valueOf((char)cc);
        }
        else
        {
            return "0x"+Integer.toHexString(cc);
        }
    }
    @Override
    public int compareTo(CharRange o)
    {
        if (from != o.from)
        {
            return from - o.from;
        }
        else
        {
            return o.to - to;
        }
    }

}
