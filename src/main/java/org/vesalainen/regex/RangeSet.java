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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class groups Ranges to form a set
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RangeSet implements Iterable<Range>
{
    private SortedSet<Range> set;
    /**
     * Construct a empty RangeSet
     */
    public RangeSet()
    {
        set = new TreeSet<Range>();
    }
    /**
     * Constructs a RangeSet containg one single character Range
     * @param cc
     */
    public RangeSet(int cc)
    {
        set = new TreeSet<Range>();
        add(cc);
    }
    /**
     * Copy constructor
     * @param rs
     */
    public RangeSet(RangeSet rs)
    {
        set = new TreeSet<Range>();
        for (Range r : rs)
        {
            add(r);
        }
    }

    /**
     * Constructs a RangeSet from SortedSet
     * @param set
     */
    protected RangeSet(SortedSet<Range> set)
    {
        assert set != null;
        this.set = set;
    }
    /**
     * Adds all characters from array
     * @param ac
     */
    public void add(char[] ac)
    {
        for (char cc : ac)
        {
            add(cc);
        }
    }
    /**
     * Add a single character
     * @param cc
     */
    public void add(int cc)
    {
        add(new Range(cc));
    }
    /**
     * Adds a range of characters
     * @param from
     * @param to
     */
    public void add(int from, int to)
    {
        assert from < to;
        add(new Range(from, to));
    }
    /**
     * Add all Ranges from another RangeSet
     * @param set
     */
    public void add(RangeSet set)
    {
        for (Range r : set)
        {
            add(r);
        }
    }
    /**
     * Adds a Range
     * @param cond
     */
    public void add(Range cond)
    {
        if (set.isEmpty())
        {
            set.add(cond);
        }
        else
        {
            List<Range> remlist = new ArrayList<Range>();
            List<Range> addlist = new ArrayList<Range>();
            boolean is = false;
            for (Range r : set)
            {
                if (r.intersect(cond))
                {
                    remlist.add(r);
                    addlist.addAll(Range.removeOverlap(cond, r));
                    is = true;
                }
            }
            if (!is)
            {
                set.add(cond);
            }
            set.removeAll(remlist);
            set.addAll(addlist);
        }
    }
    /**
     * Removes all range's from a rangeset.
     * @param rs
     */
    public void remove(RangeSet rs)
    {
        for (Range r : rs)
        {
            remove(r);
        }
    }
    /**
     * Removes a Range from RangeSet. Example: Removing c-d from a-z results a-be-z
     * @param cond
     */
    public void remove(Range cond)
    {
        if (!set.isEmpty())
        {
            List<Range> remlist = new ArrayList<Range>();
            List<Range> addlist = new ArrayList<Range>();
            for (Range r : set)
            {
                if (r.intersect(cond))
                {
                    remlist.add(r);
                    int from = r.getFrom();
                    int to = cond.getFrom();
                    if (from != to)
                    {
                        addlist.add(new Range(from, to));
                    }
                    from = cond.getTo();
                    to = r.getTo();
                    if (from != to)
                    {
                        addlist.add(new Range(from, to));
                    }
                }
            }
            set.removeAll(remlist);
            set.addAll(addlist);
        }
    }
    /**
     *
     * @return
     */
    public Iterator<Range> iterator()
    {
        return set.iterator();
    }

    public boolean isEmpty()
    {
        return set.isEmpty();
    }
    /**
     * Converts a possibly overlapping collection of RangesSet's into a non overlapping
     * RangeSet that accepts the same characters.
     * @param rangeSets
     * @return
     */
    public static RangeSet split(RangeSet... rangeSets)
    {
        return split(Arrays.asList(rangeSets));
    }
    /**
     * Converts a possibly overlapping collection of RangesSet's into a non overlapping
     * RangeSet that accepts the same characters.
     * @param rangeSets
     * @return
     */
    public static RangeSet split(Collection<RangeSet> rangeSets)
    {
        RangeSet result = new RangeSet();
        SortedSet<Integer> ss = new TreeSet<Integer>();
        for (RangeSet rs : rangeSets)
        {
            for (Range r : rs)
            {
                ss.add(r.getFrom());
                ss.add(r.getTo());
            }
        }
        Iterator<Integer> i = ss.iterator();
        if (i.hasNext())
        {
            int from = i.next();
            while (i.hasNext())
            {
                int to = i.next();
                if (from != to)
                {
                    for (RangeSet rs : rangeSets)
                    {
                        if (rs.contains(from, to))
                        {
                            result.add(from, to);
                            break;
                        }
                    }
                }
                from = to;
            }
        }
        return result;
    }
    /**
     * Returns true only if any two of RangeSet's is intersecting with each other.
     * @param rangeSets
     * @return
     */
    public boolean isIntersecting(RangeSet... rangeSets)
    {
        return isIntersecting(Arrays.asList(rangeSets));
    }
    /**
     * Returns true only if any two of RangeSet's is intersecting with each other.
     * @param rangeSets
     * @return
     */
    public boolean isIntersecting(Collection<RangeSet>  rangeSets)
    {
        for (RangeSet rs : rangeSets)
        {
            for (Range r1 : rs)
            {
                for (Range r2 : this)
                {
                    if (r1.intersect(r2))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    /**
     * Returns a RangeSet containg only the ranges that all of the argument
     * ranges accept
     * @param rangeSets
     * @return
     */
    public static RangeSet intersect(RangeSet... rangeSets)
    {
        return intersect(Arrays.asList(rangeSets));
    }
    /**
     * Returns a RangeSet containg only the ranges that all of the argument
     * ranges accept
     * @param rangeSets
     * @return
     */
    public static RangeSet intersect(Collection<RangeSet> rangeSets)
    {
        RangeSet result = new RangeSet();
        SortedSet<Integer> ss = new TreeSet<Integer>();
        for (RangeSet rs : rangeSets)
        {
            for (Range r : rs)
            {
                ss.add(r.getFrom());
                ss.add(r.getTo());
            }
        }
        Iterator<Integer> i = ss.iterator();
        if (i.hasNext())
        {
            int from = i.next();
            while (i.hasNext())
            {
                int to = i.next();
                if (from != to)
                {
                    boolean included = true;
                    for (RangeSet rs : rangeSets)
                    {
                        if (!rs.contains(from, to))
                        {
                            included = false;
                            break;
                        }
                    }
                    if (included)
                    {
                        result.add(from, to);
                    }
                }
                from = to;
            }
        }
        return result;
    }
    /**
     * Returns a new RangeSet that accepts the same characters as argument.
     * Ranges that are followinf each other are concatenated.
     * @param rs
     * @return
     */
    public static RangeSet merge(RangeSet rs)
    {
        RangeSet result = new RangeSet();
        int from = -1;
        int to = -1;
        for (Range r : rs)
        {
            if (from == -1) // first
            {
                from = r.getFrom();
                to = r.getTo();
            }
            else
            {
                if (r.getFrom() == to)
                {
                    to = r.getTo();
                }
                else
                {
                    result.add(from, to);
                    from = r.getFrom();
                    to = r.getTo();
                }
            }
        }
        result.add(from, to);
        return result;
    }
    private boolean contains(int from, int to)
    {
        for (Range r : set)
        {
            if (r.contains(from, to))
            {
                return true;
            }
            if (r.getFrom() > to)
            {
                return false;
            }
        }
        return false;
    }
    /**
     * Return a complement RangeSet. In other words a RangeSet doesn't accept
     * any of this rangesets characters and accepts all other characters.
     * @return
     */
    public RangeSet complement()
    {
        SortedSet<Range> nset = new TreeSet<Range>();
        int from = 0;
        for (Range r : set)
        {
            int to = r.getFrom();
            if (from < to)
            {
                nset.add(new Range(from, to));
            }
            from = r.getTo();
        }
        if (from < Integer.MAX_VALUE)
        {
            nset.add(new Range(from, Integer.MAX_VALUE));
        }
        return new RangeSet(nset);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (Range r : set)
        {
            sb.append(r.toString());
        }
        return sb.toString();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            RangeSet rs1 = new RangeSet();
            rs1.add('d', 'e'+1);
            rs1.add(0, 'a');
            rs1.add('b');
            rs1.add('g');
            RangeSet rs3 = RangeSet.merge(rs1);
            System.err.println(rs3);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
