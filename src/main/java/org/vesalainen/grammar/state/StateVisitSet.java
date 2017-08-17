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

import java.util.BitSet;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class StateVisitSet<S extends State>
{
    private BitSet set;
    private Scope scope;

    public boolean contains(S state)
    {
        if (set != null)
        {
            if (scope != state.getScope())
            {
                throw new IllegalArgumentException("compairing states of different scope "+scope+" != "+state.getScope());
            }
            return set.get(state.getNumber());
        }
        else
        {
            return false;
        }
    }

    public void add(S state)
    {
        if (set == null)
        {
            set = new BitSet(state.getScope().count());
            scope = state.getScope();
        }
        set.set(state.getNumber());
    }

    public void clear()
    {
        if (set != null)
        {
            set.clear();
        }
    }
}
