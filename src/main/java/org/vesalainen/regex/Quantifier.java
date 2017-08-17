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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Quantifier
{
    private int min;
    private int max;

    public Quantifier(int c)
    {
        min = c;
        max = c;
    }

    public Quantifier(int min, int max)
    {
        this.min = min;
        this.max = max;
    }

    public int getMax()
    {
        return max;
    }

    public int getMin()
    {
        return min;
    }

    @Override
    public String toString()
    {
        return "{"+min+", "+max+'}';
    }

}
