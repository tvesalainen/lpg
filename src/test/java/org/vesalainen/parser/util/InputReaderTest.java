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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author tkv
 */
public class InputReaderTest
{

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }
    
    @Test
    public void test1()
    {
        try
        {
            InputReader input = new InputReader("abcdefg");
            input.read();
            for (int count=0;count < 1000;count++)
            {
                input.read();
                input.read();
                input.read();
                input.clear();
                assertEquals("efg", input.buffered());
                input.insert("1".toCharArray());
                assertEquals("1efg", input.buffered());
                input.insert("23".toCharArray());
                assertEquals("231efg", input.buffered());
                input.insert("".toCharArray());
                assertEquals("231efg", input.buffered());
            }
        }
        catch (IOException ex)
        {
            fail();
        }
    }
    @Test
    public void test2()
    {
        try
        {
            File temp = File.createTempFile("test", null);
            FileOutputStream fos = new FileOutputStream(temp);
            for (int ii=0;ii<4000;ii++)
            {
                fos.write("qwerty".getBytes());
            }
            fos.close();
            
            InputReader input = new InputReader(temp, 100);
            int index=0;
            int rc = input.read();
            while (rc != -1)
            {
                if (index % 6 == 0)
                {
                    input.clear();
                }
                rc = input.read();
                index++;
            }
            fos.close();
        }
        catch (IOException ex)
        {
            fail();
        }
    }
    @Test
    public void test3()
    {
        try
        {
            StringReader sr = new StringReader("abcdefghijklmn");
            InputReader input = new InputReader(sr, 4);
            input.read();
            input.read();
            assertEquals(0, input.getStart());
            assertEquals(2, input.getEnd());
            assertEquals(2, input.getLength());
            assertEquals("ab", input.getString(0, 2));
            int fieldRef = input.getFieldRef();
            assertEquals("ab", input.getString(fieldRef));
            CharSequence cs = input.getCharSequence(fieldRef);
            assertEquals(2, cs.length());
            assertEquals('a', cs.charAt(0));
            assertEquals('b', cs.charAt(1));
            assertEquals("ab", cs.toString());
            input.clear();
            input.read();
            input.read();
            input.read();
            assertEquals(2, input.getStart());
            assertEquals(5, input.getEnd());
            assertEquals(3, input.getLength());
            assertEquals("cde", input.getString(2, 3));
            fieldRef = input.getFieldRef();
            cs = input.getCharSequence(fieldRef);
            assertEquals(3, cs.length());
            assertEquals('c', cs.charAt(0));
            assertEquals('d', cs.charAt(1));
            assertEquals('e', cs.charAt(2));
            assertEquals("cde", cs.toString());
            CharSequence ss = cs.subSequence(1, 2);
            assertEquals(1, ss.length());
            assertEquals('d', ss.charAt(0));
            assertEquals("d", ss.toString());
            assertEquals("cde", input.getString(fieldRef));
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
}
