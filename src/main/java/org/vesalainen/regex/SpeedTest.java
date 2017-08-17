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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SpeedTest
{

    public static String expr(int n)
    {
        StringBuilder sb = new StringBuilder();
        for (int ii=0;ii<n;ii++)
        {
            sb.append("a?");
        }
        return sb.toString();
    }
    public static String expr2(int n)
    {
        StringBuilder sb = new StringBuilder();
        for (int ii=0;ii<n;ii++)
        {
            sb.append("a");
        }
        return sb.toString();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            boolean regex = false;
            for (int n=1;n<30;n++)
            {
                String expr = expr(n);
                String a = expr2(n);
                if (regex)
                {
                    Regex p = Regex.compile(expr+a);
                    long l = System.nanoTime();
                    p.isMatch(a);
                    long ll = System.nanoTime()-l;
                    System.err.println(ll);
                }
                else
                {
                    Pattern p = Pattern.compile(expr+a);
                    Matcher m = p.matcher(a);
                    long l = System.nanoTime();
                    m.matches();
                    long ll = System.nanoTime()-l;
                    System.err.println(ll);
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
