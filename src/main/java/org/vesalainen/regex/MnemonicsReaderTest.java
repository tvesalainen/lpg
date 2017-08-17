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

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MnemonicsReaderTest
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            URL url = new URL("http://java.sun.com/docs/books/jvms/second_edition/html/Mnemonics.doc.html");
            LineNumberReader r = new LineNumberReader(new InputStreamReader(url.openStream()));

            Regex p = Regex.compile(" [0-9]{2,3} \\((0x[0-9a-fA-F]{2})\\)	 <i>([^<]*)</i><p>");
            while (true)
            {
                //System.err.println(p.find(r));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
