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

package org.vesalainen.grammar.math;

/**
 * @author Timo Vesalainen
 */
public class MoreMath 
{
    public static int pow(int k, int e)
    {
        return (int) Math.pow(k, e);
    }
    public static long pow(long k, long e)
    {
        return (long) Math.pow(k, e);
    }
    public static int factorial(int k)
    {
        int result = 1;
        for (int ii=1;ii<=k;ii++)
        {
            result *= ii;
        }
        return result;
    }
    public static long factorial(long k)
    {
        long result = 1;
        for (long ii=1;ii<=k;ii++)
        {
            result *= ii;
        }
        return result;
    }
}
