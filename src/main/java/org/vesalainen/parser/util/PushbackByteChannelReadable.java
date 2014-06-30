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
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import org.vesalainen.io.PushbackReadable;
import org.vesalainen.io.Pushbackable;

/**
 *
 * @author Timo Vesalainen
 */
public class PushbackByteChannelReadable extends ByteChannelReadable implements Pushbackable<CharBuffer>
{
    private PushbackReadable pushback;

    public PushbackByteChannelReadable(ReadableByteChannel channel, Charset cs)
    {
        super(channel, cs);
        pushback = new PushbackReadable(this);
    }

    public PushbackByteChannelReadable(ReadableByteChannel channel, Charset cs, int sz, boolean direct, boolean fixedCharset)
    {
        super(channel, cs, sz, direct, fixedCharset);
        pushback = new PushbackReadable(this);
    }
    

    @Override
    public int read(CharBuffer cb) throws IOException
    {
        return pushback.read(cb);
    }

    @Override
    public void pushback(CharBuffer... buffers) throws IOException
    {
        pushback.pushback(buffers);
    }
    
    
}
