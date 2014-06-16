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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayDeque;

/**
 * Reader that stores input in ring buffer. Ring buffer size must be big enough
 * to enable rewind and access to captured data
 * 
 * <p>CharSequence implementation is for current input. Current input can be read
 * as CharSequence without creating String object.
 * @author tkv
 */
public final class InputReader extends Input<Reader>
{
    private char[] array;       // backing array
    public InputReader(File file, int size) throws FileNotFoundException
    {
        this(new FileInputStream(file), size);
    }
    public InputReader(File file, int size, String cs) throws FileNotFoundException
    {
        this(new FileInputStream(file), size, cs);
    }
    public InputReader(File file, int size, String cs, boolean upper) throws FileNotFoundException
    {
        this(new FileInputStream(file), size, cs, upper);
    }
    public InputReader(File file, int size, Charset cs) throws FileNotFoundException
    {
        this(new FileInputStream(file), size, cs);
    }
    public InputReader(File file, int size, Charset cs, boolean upper) throws FileNotFoundException
    {
        this(new FileInputStream(file), size, cs, upper);
    }
    /**
     * Constructs an InputReader with default charset
     * @param is
     * @param size size of inner ring buffer
     */
    public InputReader(InputStream is, int size)
    {
        this(new StreamReader(is), size);
    }
    public InputReader(InputStream is, int size, boolean upper)
    {
        this(new StreamReader(is), size, upper);
    }
    /**
     * Constructs an InputReader
     * @param is
     * @param size size of inner ring buffer
     * @param cs Character set
     */
    public InputReader(InputStream is, int size, String cs)
    {
        this(new StreamReader(is, cs), size);
    }
    public InputReader(InputStream is, int size, String cs, boolean upper)
    {
        this(new StreamReader(is, cs), size, upper);
    }
    /**
     * Constructs an InputReader
     * @param is
     * @param size
     * @param cs 
     */
    public InputReader(InputStream is, int size, Charset cs)
    {
        this(new StreamReader(is, cs), size);
    }
    public InputReader(InputStream is, int size, Charset cs, boolean upper)
    {
        this(new StreamReader(is, cs), size, upper);
    }
    /**
     * Constructs an InputReader
     * @param sr
     * @param size 
     */
    private InputReader(StreamReader sr, int size)
    {
        this((Reader)sr, size);
        includeLevel.setStreamReader(sr);
    }
    /**
     * Constructs an InputReader
     * @param in
     * @param size
     * @param upper If true input is converted upper-case, if false input is converted lower-case
     */
    public InputReader(Reader in, int size, boolean upper)
    {
        this(new CaseChangeReader(in, upper), size);
    }
    /**
     * Constructs an InputReader
     * @param in
     * @param size 
     */
    public InputReader(Reader in, int size)
    {
        this.size = size;
        includeLevel.setIn(in);
        array = new char[size];
    }
    /**
     * Constructs an InputReader
     * @param in
     * @param size 
     */
    public InputReader(PushbackReader in, int size)
    {
        this(in, new char[size]);
    }
    /**
     * Constructs an InputReader
     * @param in
     * @param shared Shared ringbuffer.
     */
    public InputReader(PushbackReader in, char[] shared)
    {
        size = shared.length;
        includeLevel.setIn(in);
        array = shared;
    }
    /**
     * Constructs an InputReader
     * @param text
     */
    public InputReader(CharSequence text)
    {
        size = text.length();
        end = size;
        array = text.toString().toCharArray();
        setSource(text.toString());
    }
    /**
     * Constructs an InputReader
     * @param text
     * @param size 
     */
    public InputReader(CharSequence text, int size)
    {
        if (size < text.length())
        {
            throw new IllegalArgumentException("buffer size "+size+" < text length "+text.length());
        }
        this.size = size;
        array = new char[size];
        for (int ii=0;ii<text.length();ii++)
        {
            array[ii] = text.charAt(ii);
        }
        end = text.length();
        setSource(text.toString());
    }
    /**
     * Constructs an InputReader
     * @param array
     */
    public InputReader(char[] array)
    {
        size = array.length;
        this.array = array;
        end = size;
    }
    /**
     * Inserts text at cursor position
     * @param text 
     */
    public void insert(char[] text) throws IOException
    {
        int ln = text.length;
        if (ln == 0)
        {
            return;
        }
        if (ln >= size - (end-cursor))
        {
            throw new IOException(text+" doesn't fit in the buffer");
        }
        if (cursor != end)
        {
            makeRoom(ln);
        }
        int cms = cursor % size;
        if (size - cms < text.length)
        {
            System.arraycopy(text, 0, array, cms, size - cms);
            System.arraycopy(text, size - cms, array, 0, text.length - (size - cms));
        }
        else
        {
            System.arraycopy(text, 0, array, cms, text.length);
        }
        end += ln;
    }
    /**
     * Inserts text at cursor position
     * @param text 
     */
    public void insert(CharSequence text) throws IOException
    {
        int ln = text.length();
        if (ln == 0)
        {
            return;
        }
        if (ln >= size - (end-cursor))
        {
            throw new IOException(text+" doesn't fit in the buffer");
        }
        if (cursor != end)
        {
            makeRoom(ln);
        }
        for (int ii=0;ii<ln;ii++)
        {
            set((cursor+ii), text.charAt(ii));
        }
        end += ln;
    }
    private void makeRoom(int ln)
    {
        int src = 0;
        int dst = 0;
        int len = 0;
        int ems = end % size;
        int cms = cursor % size;
        if (ems < cms)
        {
            src = 0;
            dst = ln;
            len = ems;
            System.arraycopy(array, src, array, dst, len);
        }
        int spaceAtEndOfBuffer = 0;
        if (ems >= cms)
        {
            spaceAtEndOfBuffer = size - ems;
        }
        int needToWrap = Math.min(ln - spaceAtEndOfBuffer, size - cms);
        if (needToWrap > 0)
        {
            src = size - spaceAtEndOfBuffer - needToWrap;
            dst = ln-needToWrap - spaceAtEndOfBuffer;
            len = needToWrap;
            System.arraycopy(array, src, array, dst, len);
        }
        src = cms;
        if (ems < cms)
        {
            len = (size - cms) - needToWrap;
        }
        else
        {
            len = (ems - cms) - needToWrap;
        }
        dst = Math.min(cms + ln, size-1);
        System.arraycopy(array, src, array, dst, len);
    }
    public void write(int s, int l, Writer writer) throws IOException
    {
        if (s < end-size)
        {
            throw new IllegalArgumentException("buffer too small");
        }
        int ps = s % size;
        int es = (s+l) % size;
        if (ps <= es)
        {
            writer.write(array, ps, l);
        }
        else
        {
            writer.write(array, ps, size-ps);
            writer.write(array, 0, es);
        }
    }

    public void write(Writer writer) throws IOException
    {
        write(cursor-length, length, writer);
    }

    public char[] getArray()
    {
        return array;
    }
    /**
     * Returns string from buffer
     * @param s Start of input
     * @param l Length of input
     * @return 
     */
    public String getString(int s, int l)
    {
        int ps = s % size;
        int es = (s+l) % size;
        if (ps <= es)
        {
            return new String(array, ps, l);
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            sb.append(array, ps, size-ps);
            sb.append(array, 0, es);
            return sb.toString();
        }
    }
    /**
     * Include InputStream at current input. InputStream is read as part of 
     * input. When InputStream ends, input continues using current input.
     * 
     * <p>Included stream is closed at eof
     * 
     * @param is Incuded input
     * @param source Description of the source
     * @throws IOException 
     */
    public void include(InputStream is, String source) throws IOException
    {
        include(is, Charset.defaultCharset(), source);
    }
    /**
     * Include InputStream at current input. InputStream is read as part of 
     * input. When InputStream ends, input continues using current input.
     * 
     * <p>Included stream is closed at eof
     * 
     * @param is Incuded input
     * @param cs Character set
     * @param source Description of the source
     * @throws IOException 
     */
    public void include(InputStream is, String cs, String source) throws IOException
    {
        include(is, Charset.forName(cs), source);
    }
    /**
     * Include InputStream at current input. InputStream is read as part of 
     * input. When InputStream ends, input continues using current input.
     * 
     * <p>Included stream is closed at eof
     * 
     * @param is Incuded input
     * @param cs Character set
     * @param source Description of the source
     * @throws IOException 
     */
    public void include(InputStream is, Charset cs, String source) throws IOException
    {
        if (cursor != end)
        {
            throw new IOException("not allowed to include when buffer is not empty");
        }
        if (includeStack == null)
        {
            includeStack = new ArrayDeque<>();
        }
        includeStack.push(includeLevel);
        StreamReader sr = new StreamReader(is, cs);
        Reader pr = new RecoverableReader(sr);
        includeLevel = new IncludeLevel(pr, sr, source);
    }
    /**
     * Include Reader at current input. Reader is read as part of 
     * input. When Reader ends, input continues using current input.
     * 
     * <p>Included reader is closed at eof
     * 
     * @param in
     * @param source
     * @throws IOException 
     */
    public void include(Reader in, String source) throws IOException
    {
        if (cursor != end)
        {
            throw new IOException("not allowed to include when buffer is not empty");
        }
        if (includeStack == null)
        {
            includeStack = new ArrayDeque<>();
        }
        includeStack.push(includeLevel);
        includeLevel = new IncludeLevel(in, source);
    }

    @Override
    protected int get(int index)
    {
        return array[index % size];
    }

    @Override
    protected void set(int index, int value)
    {
        array[index % size] = (char) value;
    }

    @Override
    protected int fill(Reader input, int offset, int length) throws IOException
    {
        return input.read(array, offset % size, length);
    }

    @Override
    protected void close(Reader input) throws IOException
    {
        input.close();
    }

    @Override
    protected boolean ready(Reader input) throws IOException
    {
        return input.ready();
    }

    @Override
    protected void reuse(Reader input)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
