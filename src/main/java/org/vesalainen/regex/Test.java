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

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Test
{
    private static char[] re = new char[] {'a', 'b', 'c', '?', '+', '*'};
    private static char[] aa = new char[] {'a', 'b', 'c'};
    private static Random r = new Random();

    private static String expr()
    {
        StringBuilder sbr = new StringBuilder();
        int len = r.nextInt(10)+1;
        boolean letter = true;
        for (int ii=0;ii<len;ii++)
        {
            char cc = 0;
            if (letter)
            {
                cc = aa[r.nextInt(aa.length)];
                letter = false;
            }
            else
            {
                cc = re[r.nextInt(re.length)];
                letter = !Character.isLetter(cc);
            }
            sbr.append(cc);
        }
        return sbr.toString();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            System.err.println("'P'".substring(1, "'P'".length()-1));
            long l = 0;
            long r1 = 0;
            long r2 = 0;
            for (int c=0;c<1000;c++)
            {
                String regex = "("+expr()+")|("+expr()+")";
                Regex v = Regex.compile(regex);
                Pattern pp = Pattern.compile(regex);
                for (int ii=0;ii<100;ii++)
                {
                    StringBuilder sbe = new StringBuilder();
                    for (int jj=0;jj<1000;jj++)
                    {
                        sbe.append((char)aa[r.nextInt(aa.length)]);
                    }
                    String str = sbe.toString();
                    l = System.nanoTime();
                    boolean eb = v.isMatch(str);
                    r1 += System.nanoTime()-l;
                    l = System.nanoTime();
                    Matcher mm = pp.matcher(str);
                    boolean mb = mm.matches();
                    r2 += System.nanoTime()-l;
                    if (eb != mb)
                    {
                        System.err.println(regex+" <- "+str+" eb="+eb+" mb="+mb);
                        System.exit(-1);
                    }
                    if (eb)
                    {
                        System.err.println("ok");
                    }
                }
                System.err.println("regex="+r1+" Matcher="+r2);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
