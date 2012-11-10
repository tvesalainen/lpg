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

/**
 *
 * @author tkv
 */
public class Scope<S extends State>
{
    private String name;
    private int next;

    public Scope(String name)
    {
        this.name = name;
    }

    int next()
    {
        return next++;
    }

    int count()
    {
        return next;
    }

    @Override
    public String toString()
    {
        return "StateScope{" + "name=" + name + '}';
    }
}
