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
public final class ReaderInput extends Input<Reader>
{
    private char[] array;       // backing array
    /**
     * Constructs an InputReader
     * @param sr
     * @param size 
     */
    ReaderInput(StreamReader sr, int size)
    {
        this((Reader)sr, size);
        includeLevel.setStreamReader(sr);
    }
    /**
     * Constructs an InputReader
     * @param in
     * @param size 
     */
    ReaderInput(Reader in, int size)
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
    ReaderInput(PushbackReader in, int size)
    {
        this(in, new char[size]);
    }
    /**
     * Constructs an InputReader
     * @param in
     * @param shared Shared ringbuffer.
     */
    ReaderInput(PushbackReader in, char[] shared)
    {
        size = shared.length;
        includeLevel.setIn(in);
        array = shared;
    }
    /**
     * Constructs an InputReader
     * @param text
     * @param size 
     */
    ReaderInput(CharSequence text, int size)
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
    ReaderInput(char[] array)
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
            release();
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
            release();
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
    public void reuse(CharSequence text)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    protected void unread(Reader input, int offset, int length) throws IOException
    {
            if (input instanceof PushbackReader)
            {
                PushbackReader pr = (PushbackReader) input;
                int right = size - offset;
                if (length > right)
                {
                    pr.unread(array, 0, length-right);
                }
                pr.unread(array, offset, right);
            }
            else
            {
                throw new UnsupportedOperationException("release() only supported for java.io.PushbackReader. Not for "+input.getClass().getName());
            }
    }
    
}
