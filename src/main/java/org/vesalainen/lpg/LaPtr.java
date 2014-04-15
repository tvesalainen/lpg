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
package org.vesalainen.lpg;

import org.vesalainen.grammar.GTerminal;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author tkv
 */
public class LaPtr
{
    private Set<GTerminal> laSet = new LinkedHashSet<GTerminal>();

    public Set<GTerminal> getLaSet()
    {
        return laSet;
    }

    public void add(GTerminal symbol)
    {
        laSet.add(symbol);
    }
    public void add(Collection<GTerminal> symbols)
    {
        laSet.addAll(symbols);
    }
    public void set(Collection<GTerminal> symbols)
    {
        laSet.clear();
        laSet.addAll(symbols);
    }

    @Override
    public String toString()
    {
        return "LaPtr{" + laSet + '}';
    }

}
