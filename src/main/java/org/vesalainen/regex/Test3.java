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

import java.util.Enumeration;

/**
 *
 * @author tkv
 */
public class Test3
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            Enumeration e1 = new Enumeration()
            {
                public boolean hasMoreElements()
                {
                    return true;
                }

                public Object nextElement()
                {
                    return null;
                }
            };
            Class<?> c = e1.getClass();
            Enumeration e2 = new Enumeration()
            {
                public boolean hasMoreElements()
                {
                    return true;
                }

                public Object nextElement()
                {
                    return null;
                }
            };
            Class<?> c2 = e2.getClass();
            System.err.println(c.getName());
            System.err.println(c.getPackage());
            System.err.println(c.isAnonymousClass());
            System.err.println(c.isLocalClass());
            System.err.println(c2.getName());
            System.err.println(c2.getPackage());
            System.err.println(c2.isAnonymousClass());
            System.err.println(c2.isLocalClass());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
