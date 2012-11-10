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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Timo Vesalainen
 */
public class ClassBrowser
{
    /**
     * Return effective methods for a class. All methods accessible at class 
     * are returned. That includes superclass methods which are not overriden.
     * @param cls
     * @return 
     */
    public static Collection<Method> getMethods(Class<?> cls)
    {
        List<Method> list = new ArrayList<>();
        while (cls != null)
        {
            for (Method method : cls.getDeclaredMethods())
            {
                if (!contains(list, method))
                {
                    list.add(method);
                }
            }
            cls = cls.getSuperclass();
        }
        return list;
    }
    private static boolean contains(Collection<Method> methods, Method method)
    {
        for (Method m : methods)
        {
            if (isSame(m, method))
            {
                return true;
            }
        }
        return false;
    }
    private static boolean isSame(Method m1, Method m2)
    {
        return m1.getName().equals(m2.getName()) && Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes());
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)    
    {
        try
        {
            // TODO code application logic here
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
