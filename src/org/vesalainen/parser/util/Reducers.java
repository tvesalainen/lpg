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

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timo Vesalainen
 */
public class Reducers
{
    private static Method get1;
    private static Method get2;
    static
    {
        try
        {
            get1 = Reducers.class.getMethod("get");
            get2 = Reducers.class.getMethod("get", Object.class);
        }
        catch (NoSuchMethodException | SecurityException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public static <T> List<T> listStart()
    {
        return new ArrayList<>();
    }
    
    public static <T> List<T> listStart(T t)
    {
        List<T> list = new ArrayList<>();
        list.add(t);
        return list;
    }
    
    public static <T> List<T> listNext(List<T> list, T t)
    {
        list.add(t);
        return list;
    }
    
    public static <T> T get()
    {
        return null;
    }
    
    public static <T> T get(T t)
    {
        return t;
    }

    public static boolean isGet(Member member)
    {
        return get1.equals(member) || get2.equals(member);
    }
}
