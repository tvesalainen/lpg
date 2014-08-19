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

import org.vesalainen.util.OrderedList;
import org.vesalainen.util.MapList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * NumMapList is a convenience class for handling Numerable keyed mapped lists. 
 * List creation is automatic.
 * @author tkv
 */
public class NumMapList<M extends Numerable,L> extends NumMap<M,List<L>> implements MapList<M, L>
{
    private Comparator<L> comparator;
    private List<L> emptyList = new ArrayList<>();

    public NumMapList()
    {
    }

    public NumMapList(Comparator<L> comparator)
    {
        this.comparator = comparator;
    }

    private List<L> createList()
    {
        if (comparator != null)
        {
            return new OrderedList<>(comparator);
        }
        else
        {
            return new ArrayList<>();
        }
    }
    @Override
    public void add(M key, L value)
    {
        add(key, -1, value);
    }
    
    @Override
    public void add(M key, int index, L value)
    {
        List<L> list = super.get(key);
        if (list == null)
        {
            list = createList();
            put(key, list);
        }
        if (index != -1)
        {
            list.add(index, value);
        }
        else
        {
            list.add(value);
        }
    }
    @Override
    public List<L> set(M key, Collection<L> value)
    {
        List<L> list = super.get(key);
        if (list == null)
        {
            list = createList();
            put(key, list);
        }
        list.clear();
        list.addAll(value);
        return list;
    }

    @Override
    public List<L> get(Object key)
    {
        List<L> list = super.get(key);
        if (list == null)
        {
            return emptyList;
        }
        else
        {
            return list;
        }
    }

    @Override
    public void addAll(Map<M, L> map)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
