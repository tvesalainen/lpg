/*
 * Copyright (C) 2014 Timo Vesalainen
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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import org.vesalainen.io.Rewindable;

/**
 *
 * @author Timo Vesalainen
 */
public class ScatteringByteChannelReadable implements Readable, AutoCloseable, Rewindable, DynamicCharset
{
    private final ScatteringByteChannel channel;
    private CharsetDecoder decoder;
    private final ByteBuffer byteBuffer;
    private int lastRead;
    private boolean fixed;

    public ScatteringByteChannelReadable(ScatteringByteChannel channel, Charset cs)
    {
        this(channel, cs, 8192, false);
    }
    public ScatteringByteChannelReadable(ScatteringByteChannel channel, Charset cs, int sz, boolean direct)
    {
        this.channel = channel;
        this.decoder = cs.newDecoder();
        if (direct)
        {
            byteBuffer = ByteBuffer.allocateDirect(sz);
        }
        else
        {
            byteBuffer = ByteBuffer.allocate(sz);
        }
    }
    
    
    @Override
    public int read(CharBuffer charBuffer) throws IOException
    {
        byteBuffer.mark();
        int remaining = charBuffer.remaining();
        if (!fixed)
        {
            charBuffer.limit(charBuffer.position()+1);
        }
        if (!byteBuffer.hasRemaining())
        {
            byteBuffer.clear();
            int rc = channel.read(byteBuffer);
            if (rc == -1)
            {
                return -1;
            }
            byteBuffer.flip();
        }
        CoderResult res = decoder.decode(byteBuffer, charBuffer, false);
        while (res.isUnderflow())
        {
            assert !byteBuffer.hasRemaining();
            byteBuffer.clear();
            int rc = channel.read(byteBuffer);
            if (rc != -1)
            {
                byteBuffer.flip();
                res = decoder.decode(byteBuffer, charBuffer, false);
            }
            else
            {
                byteBuffer.limit(0);
                decoder.decode(byteBuffer, charBuffer, true);
                decoder.flush(charBuffer);
                break;
            }
        }
        if (res.isError())
        {
            throw new IOException(res.toString());
        }
        lastRead = remaining - charBuffer.remaining();
        return lastRead;
    }

    @Override
    public void close() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void rewind(int count) throws IOException
    {
        if (count > lastRead)
        {
            throw new IllegalArgumentException("rewinding more than last read");
        }
        byteBuffer.reset();
        skip(lastRead - count);
    }

    private void skip(int count) throws IOException
    {
        CharBuffer cb = CharBuffer.allocate(count);
        int skipped = read(cb);
        if (count != skipped)
        {
            throw new IllegalArgumentException("skipped only "+skipped);
        }
    }
    @Override
    public void setEncoding(String cs)
    {
        setEncoding(Charset.forName(cs));
    }

    @Override
    public void setEncoding(Charset cs)
    {
        decoder = cs.newDecoder();
    }

    @Override
    public void fixEncoding()
    {
        this.fixed = true;
    }
    
}
