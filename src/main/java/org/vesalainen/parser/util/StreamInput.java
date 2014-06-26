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
import java.io.PushbackInputStream;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.EnumSet;
import org.vesalainen.parser.ParserFeature;

/**
 * Reader that stores input in ring buffer. Ring buffer size must be big enough
 * to enable rewind and access to captured data
 * 
 * <p>CharSequence implementation is for current input. Current input can be read
 * as CharSequence without creating String object.
 * @author tkv
 */
public final class StreamInput extends Input<InputStream>
{
    private byte[] array;       // backing array
    /**
     * Constructs an InputReader
     * @param in
     * @param size 
     */
    StreamInput(InputStream in, int size, EnumSet<ParserFeature> features)
    {
        super(features);
        this.size = size;
        includeLevel.in = in;
        array = new byte[size];
    }
    /**
     * Constructs an InputReader
     * @param array
     */
    StreamInput(byte[] array, EnumSet<ParserFeature> features)
    {
        super(features);
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
        throw new UnsupportedOperationException("not supported");
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
        throw new UnsupportedOperationException("not supported");
    }

    public void write(Writer writer) throws IOException
    {
        throw new UnsupportedOperationException("not supported");
    }

    public char[] getArray()
    {
        throw new UnsupportedOperationException("not supported");
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
            sb.append(new String(array, ps, size-ps));
            sb.append(new String(array, 0, es));
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
    @Override
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
        includeLevel = new IncludeLevel(getStream(is, size, cs, features), source);
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
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    protected int get(int index)
    {
        return array[index % size];
    }

    @Override
    protected int fill(InputStream input, int offset, int length) throws IOException
    {
        int op = offset % size;
        int rightSpace = size - op;
        if (length > rightSpace)
        {
            int rc1 = input.read(array, op, rightSpace);
            if (rc1 < rightSpace)
            {
                return rc1;
            }
            int rc2 = input.read(array, 0, length - rightSpace);
            if (rc2 == -1)
            {
                return rc1;
            }
            else
            {
                return rc1 + rc2;
            }
        }
        else
        {
            return input.read(array, op, length);
        }
    }

    @Override
    protected void close(InputStream input) throws IOException
    {
        input.close();
    }

    @Override
    public void reuse(CharSequence text)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    protected void set(int index, int value)
    {
        array[index % size ] = (byte)value;
    }

    @Override
    protected void unread(InputStream input, int offset, int length) throws IOException
    {
        if (input instanceof PushbackInputStream)
        {
            PushbackInputStream pr = (PushbackInputStream) input;
            pr.unread(array, offset, length);
        }
        else
        {
            throw new UnsupportedOperationException("Release() not supported for "+input);
        }
    }

    private InputStream getStream(InputStream is, int size, Charset cs, EnumSet<ParserFeature> features)
    {
        return is;
    }
    
}
