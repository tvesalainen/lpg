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
import java.io.Reader;
import java.io.StringReader;
import org.junit.AfterClass;
import static org.junit.Assert.*;
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
            InputReader input = Input.getInstance("abcdefg", 7);
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
            
            InputReader input = Input.getInstance(temp, 100);
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
            InputReader input = Input.getInstance(sr, 4);
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
    @Test
    public void test4()
    {
        try
        {
            InputReader input = Input.getInstance("1000000");
            input.read();
            input.read();
            input.read();
            input.read();
            input.read();
            input.read();
            input.read();
            assertEquals(64, input.parseIntRadix2());
            assertEquals(-64, input.parseIntRadix2C2());
            assertEquals(64, input.parseLongRadix2());
            assertEquals(-64, input.parseLongRadix2C2());
            assertEquals(1000000, input.parseInt());
            assertEquals(1000000, input.parseLong());
            try
            {
                input.parseShort();
                fail("should fail");
            }
            catch (IllegalArgumentException ex)
            {
                assertEquals("cannot convert 1000000 to short", ex.getMessage());
            }
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void test5()
    {
        try
        {
            InputReader input = Input.getInstance("1111111");
            input.read();
            input.read();
            input.read();
            input.read();
            input.read();
            input.read();
            input.read();
            assertEquals(127, input.parseIntRadix2());
            assertEquals(-1, input.parseIntRadix2C2());
            assertEquals(127, input.parseLongRadix2());
            assertEquals(-1, input.parseLongRadix2C2());
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void test6()
    {
        try
        {
            InputReader input = Input.getInstance("1111110");
            input.read();
            input.read();
            input.read();
            input.read();
            input.read();
            input.read();
            input.read();
            assertEquals(126, input.parseIntRadix2());
            assertEquals(-2, input.parseIntRadix2C2());
            assertEquals(126, input.parseLongRadix2());
            assertEquals(-2, input.parseLongRadix2C2());
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void testCharSequence()
    {
        try
        {
            InputReader input = Input.getInstance("abcdefg");
            input.read();
            input.read();
            input.read();
            input.read();
            input.read();
            input.read();
            CharSequence cs = input;
            assertEquals(6, cs.length());
            assertEquals("abcdef", cs.toString());
            assertEquals("cde", cs.subSequence(2, 5).toString());
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void testClose()
    {
        TestReader r1 = new TestReader(10);
        try (InputReader input = Input.getInstance(r1, 10))
        {
            input.read();
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
        assertEquals(1, r1.closeCount);
    }
    @Test
    public void testCascadingClose()
    {
        TestReader r1 = new TestReader(10);
        TestReader r2 = new TestReader(10);
        TestReader r3 = new TestReader(2);
        try (InputReader input = Input.getInstance(r1, 10))
        {
            input.include(r2, "r2");
            input.include(r3, "r3");
            input.read();
            input.read();
            input.read();
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
        assertEquals(1, r1.closeCount);
        assertEquals(1, r2.closeCount);
        assertEquals(1, r3.closeCount);
    }
    public class TestReader extends Reader
    {
        private int count;
        private int closeCount;

        public TestReader(int count)
        {
            this.count = count;
        }
        
        @Override
        public int read(char[] chars, int off, int len) throws IOException
        {
            int rc = Math.min(count, len);
            count -= rc;
            if (rc > 0)
            {
                return rc;
            }
            else
            {
                return -1;
            }
        }

        @Override
        public void close() throws IOException
        {
            closeCount++;
        }
        
    }
}
