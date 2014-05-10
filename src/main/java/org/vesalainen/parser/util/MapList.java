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

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * MapList is a convenience interface for classes handling mapped lists. List creation is automatic.
 * @author Timo Vesalainen
 * @param <M> Map key type
 * @param <L> List value type
 */
public interface MapList<M, L> extends Map<M,List<L>>
{
    /**
     * Adds value to mapped list. 
     * @param key
     * @param value 
     */
    void add(M key, L value);
    /**
     * Inserts value to mapped list at index. 
     * @param key
     * @param index
     * @param value 
     */
    void add(M key, int index, L value);
    /**
     * Returns mapped list or null if not found.
     * @param key
     * @return 
     */
    List<L> get(Object key);
    /**
     * Replaces mapped list with collection. New list is in collection iterator order.
     * Old list or null is returned.
     * @param key
     * @param value
     * @return 
     */
    List<L> set(M key, Collection<L> value);
    
}
