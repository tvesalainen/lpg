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

import java.io.File;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Test2
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            File sample = new File("C:\\Users\\tkv\\Documents\\NetBeansProjects\\JVM\\build\\classes\\fi\\sw_nets\\jvm\\RegEx.class");
            String exp = "[^a]|\\z";
            //RegEx.setDebug(true);
            //RegEx.saveAs(exp, sample);
            String m = "";
            //Validator v = new Validator(exp);
            //boolean b1 = v.match(m);
            Regex r = Regex.compile(exp);
            System.err.println(r.match(m));
            System.err.println(r.find(m));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
