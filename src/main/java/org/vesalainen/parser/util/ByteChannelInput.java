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
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.EnumSet;
import org.vesalainen.parser.ParserFeature;

/**
 *
 * @author Timo Vesalainen
 */
public class ByteChannelInput extends ByteInput<ReadableByteChannel>
{

    public ByteChannelInput(ReadableByteChannel in, int size, EnumSet<ParserFeature> features)
    {
        super(size, features);
        includeLevel.in = in;
    }

    public ByteChannelInput(byte[] array, EnumSet<ParserFeature> features)
    {
        super(array, features);
        this.end = array.length;
    }

    @Override
    protected int fill(ReadableByteChannel input) throws IOException
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
    protected void unread(ReadableByteChannel input) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void close(ReadableByteChannel input) throws IOException
    {
        input.close();
    }

    @Override
    public void include(InputStream is, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void include(InputStream is, String cs, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void include(InputStream is, Charset cs, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void include(Readable in, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
