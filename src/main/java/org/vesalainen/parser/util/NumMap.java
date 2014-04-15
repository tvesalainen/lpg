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

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Timo Vesalainen
 */
public class NumMap<K extends Numerable,V> implements Map<K,V>
{
    private Object[] entries;
    private BitSet set = new BitSet();
    private static final int INITIALSIZE = 16;

    public NumMap()
    {
        entries = new Object[INITIALSIZE];
    }

    public NumMap(int initialCapacity)
    {
        entries = new Object[initialCapacity];
    }
    
    @Override
    public int size()
    {
        return set.cardinality();
    }

    @Override
    public boolean isEmpty()
    {
        return set.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        if (key instanceof Numerable)
        {
            Numerable n = (Numerable) key;
            return set.get(n.getNumber());
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value)
    {
        for (Object obj : entries)
        {
            Entry<K,V> entry = (Entry<K,V>) obj;
            if (entry != null && value.equals(entry.getValue()))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key)
    {
        if (key instanceof Numerable)
        {
            Numerable n = (Numerable) key;
            int number = n.getNumber();
            if (number < entries.length)
            {
                Entry<K,V> entry = (Entry<K,V>) entries[number];
                if (entry != null)
                {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value)
    {
        V res = get(key);
        int index = key.getNumber();
        if (index >= entries.length)
        {
            entries = Arrays.copyOf(entries, index+INITIALSIZE);
        }
        entries[index] = new SimpleEntry<>(key, value);
        set.set(index);
        return res;
    }

    @Override
    public V remove(Object key)
    {
        if (key instanceof Numerable)
        {
            Numerable n = (Numerable) key;
            int index = n.getNumber();
            if (index < entries.length)
            {
                Entry<K,V> entry = (Entry<K,V>) entries[index];
                if (entry != null)
                {
                    entries[index] = null;
                    set.clear(index);
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        for (Entry<? extends K,? extends V> entry : m.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear()
    {
        Arrays.fill(entries, null);
        set.clear();
    }

    @Override
    public Set<K> keySet()
    {
        Set<K> res = new NumSet<>();
        for (int index = set.nextSetBit(0); index >= 0; index = set.nextSetBit(index+1))
        {
            Entry<K,V> entry = (Entry<K,V>) entries[index];
            res.add(entry.getKey());
        }
        return res;
    }

    @Override
    public Collection<V> values()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
