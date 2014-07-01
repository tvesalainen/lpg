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
import java.io.InputStream;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.util.EnumSet;
import org.vesalainen.parser.ParserFeature;

/**
 *
 * @author Timo Vesalainen
 */
public class ScatteringByteChannelInput extends ByteInput<ScatteringByteChannel>
{

    public ScatteringByteChannelInput(ScatteringByteChannel in, int size, EnumSet<ParserFeature> features)
    {
        super(size, features);
        includeLevel.in = in;
    }

    public ScatteringByteChannelInput(byte[] array, EnumSet<ParserFeature> features)
    {
        super(array, features);
        this.end = array.length;
    }

    @Override
    protected int fill(ScatteringByteChannel input) throws IOException
    {
        return (int) input.read(buffers);
    }

    @Override
    protected void unread(ScatteringByteChannel input) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    protected void close(ScatteringByteChannel input) throws IOException
    {
        input.close();
    }

    @Override
    public void include(InputStream is, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void include(InputStream is, String cs, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void include(InputStream is, Charset cs, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void include(Readable in, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }
    
}
