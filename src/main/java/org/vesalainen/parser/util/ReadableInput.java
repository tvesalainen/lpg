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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackReader;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
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
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class ReadableInput extends CharInput<Readable>
{
    /**
     * Constructs an InputReader
     * @param in
     * @param size 
     */
    ReadableInput(Readable in, int size, EnumSet<ParserFeature> features)
    {
        super(size, features);
        includeLevel.in = in;
    }
    /**
     * Constructs an InputReader
     * @param in
     * @param shared Shared ringbuffer.
     */
    ReadableInput(Readable in, char[] shared, EnumSet<ParserFeature> features)
    {
        super(shared, features);
        includeLevel.in = in;
        end = shared.length;
    }
    /**
     * Constructs an InputReader
     * @param text
     */
    ReadableInput(CharSequence text, EnumSet<ParserFeature> features)
    {
        super(text, features);
        end = text.length();
        setSource(text.toString());
    }
    /**
     * Constructs an InputReader
     * @param text
     * @param size 
     */
    ReadableInput(CharSequence text, int size, EnumSet<ParserFeature> features)
    {
        super(size, features);
        if (size < text.length())
        {
            throw new IllegalArgumentException("buffer size "+size+" < text length "+text.length());
        }
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
    ReadableInput(char[] array, EnumSet<ParserFeature> features)
    {
        super(array, features);
        end = size;
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
    @Override
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
    @Override
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
        includeLevel = new IncludeLevel(getFeaturedReadable(Channels.newChannel(is), cs, features), source);
    }
    /**
     * Include Readable at current input. Readable is read as part of 
     * input. When Readable ends, input continues using current input.
     * 
     * <p>Included reader is closed at eof
     * 
     * @param in
     * @param source
     * @throws IOException 
     */
    @Override
    public void include(Readable in, String source) throws IOException
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
    protected int fill(Readable input, CharBuffer[] array) throws IOException
    {
        int rc1 = input.read(buffer1);
        if (rc1 == -1)
        {
            return -1;
        }
        if (rc1 == buffer1.remaining() && buffer2.hasRemaining())
        {
            int rc2 = input.read(buffer2);
            if (rc2 == -1)
            {
                return rc1;
            }
            else
            {
                return rc1+rc2;
            }
        }
        else
        {
            return rc1;
        }
    }

    @Override
    protected void close(Readable input) throws IOException
    {
        if (input != null && (input instanceof Closeable))
        {
            Closeable closeable = (Closeable) input;
            closeable.close();
        }
    }

    @Override
    protected void unread(Readable input) throws IOException
    {
        if ((input instanceof PushbackReader) && array != null)
        {
            PushbackReader pr = (PushbackReader) input;
            if (array != null)
            {
                pr.unread(array, buffer2.position(), buffer2.remaining());
                pr.unread(array, buffer1.position(), buffer1.remaining());
            }
            else
            {
                int count = buffer2.remaining();
                int limit = buffer2.limit();
                for (int ii=0;ii<count;ii++)
                {
                    pr.unread(buffer2.get(limit-ii));
                }
                count = buffer1.remaining();
                limit = buffer1.limit();
                for (int ii=0;ii<count;ii++)
                {
                    pr.unread(buffer1.get(limit-ii));
                }
            }
        }
        else
        {
            throw new UnsupportedOperationException("unread not supported for "+input);
        }
    }
    
}
