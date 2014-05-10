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

import org.vesalainen.util.MapSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author tkv
 */
public class NumMapSet<M extends Numerable,S> extends NumMap<M,Set<S>> implements MapSet<M, S>
{

    protected Set<S> createSet()
    {
        return new HashSet<>();
    }
    @Override
    public boolean contains(M key, S value)
    {
        Set<S> set = get(key);
        if (set == null)
        {
            return false;
        }
        return set.contains(value);
    }

    @Override
    public void add(M key, S value)
    {
        Set<S> set = get(key);
        if (set == null)
        {
            set = createSet();
            put(key, set);
        }
        set.add(value);
    }

    @Override
    public void addAll(M key, Collection<S> value)
    {
        Set<S> set = get(key);
        if (set == null)
        {
            set = createSet();
            put(key, set);
        }
        if (value != null)
        {
            set.addAll(value);
        }
    }

    @Override
    public Set<S> set(M key, Collection<S> value)
    {
        Set<S> set = get(key);
        if (set == null)
        {
            set = createSet();
            put(key, set);
        }
        set.clear();
        if (value != null)
        {
            set.addAll(value);
        }
        return set;
    }

}
