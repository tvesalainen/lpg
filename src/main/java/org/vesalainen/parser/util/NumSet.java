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
package org.vesalainen.parser.util;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Timo Vesalainen
 */
public class NumSet<N extends Numerable> implements Set<N>
{
    private List<N> list = new ArrayList<>();
    private BitSet set = new BitSet();
    
    @Override
    public int size()
    {
        return list.size();
    }

    @Override
    public boolean isEmpty()
    {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        if (o instanceof Numerable)
        {
            Numerable n = (Numerable) o;
            return set.get(n.getNumber());
        }
        return false;
    }

    @Override
    public Iterator<N> iterator()
    {
        return (Iterator<N>) list.iterator();
    }

    @Override
    public Object[] toArray()
    {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return list.toArray(a);
    }

    @Override
    public boolean add(N e)
    {
        int number = e.getNumber();
        if (!set.get(number))
        {
            set.set(number);
            list.add(e);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o)
    {
        if (o instanceof Numerable)
        {
            Numerable n = (Numerable) o;
            int number = n.getNumber();
            if (set.get(number))
            {
                set.clear(number);
                list.remove(o);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        for (Object o : c)
        {
            if (!contains(o))
            {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends N> c)
    {
        boolean changed = false;
        for (N n : c)
        {
            boolean b = add(n);
            if (b)
            {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        boolean changed = false;
        Iterator<N> it = list.iterator();
        while (it.hasNext())
        {
            Numerable n = it.next();
            if (!c.contains(n))
            {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        boolean changed = false;
        for (Object n : c)
        {
            boolean b = remove(n);
            if (b)
            {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void clear()
    {
        list.clear();
        set.clear();
    }

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
        final NumSet<N> other = (NumSet<N>) obj;
        if (!Objects.equals(this.set, other.set))
        {
            return false;
        }
        return true;
    }

    public int hashCode()
    {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.set);
        return hash;
    }

    @Override
    public String toString()
    {
        return set.toString();
    }

}
