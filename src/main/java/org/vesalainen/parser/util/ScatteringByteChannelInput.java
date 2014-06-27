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
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.util.EnumSet;
import org.vesalainen.parser.ParserFeature;

/**
 *
 * @author Timo Vesalainen
 */
public final class ScatteringByteChannelInput extends Input<ScatteringByteChannel>
{
    private final ByteBuffer buffer1;
    private final ByteBuffer buffer2;
    private final ByteBuffer[] buffers;

    /**
     *
     * @param input
     * @param size
     * @param features
     */
    public ScatteringByteChannelInput(ScatteringByteChannel input, int size, EnumSet<ParserFeature> features)
    {
        super(features);
        includeLevel.in = input;
        this.size = size;
        buffer1 = ByteBuffer.allocateDirect(size);
        buffer2 = buffer1.duplicate();
        buffers = new ByteBuffer[] {buffer1, buffer2};
    }

    @Override
    protected int get(int index)
    {
        return buffer1.get(index % size);
    }

    @Override
    protected void set(int index, int value)
    {
        buffer1.put(index % size, (byte) value);
    }

    @Override
    protected int fill(ScatteringByteChannel input, int offset, int length) throws IOException
    {
        int op = offset % size;
        int rightSpace = size - op;
        if (length > rightSpace)
        {
            buffer1.position(op);
            buffer1.limit(size);
            buffer2.position(0);
            buffer2.limit(length - rightSpace);
            int rc = (int) input.read(buffers);
            buffer1.clear();
            buffer2.clear();
            return rc;
        }
        else
        {
            buffer1.position(op);
            buffer1.limit(op+length);
            int rc = (int) input.read(buffer1);
            buffer1.clear();
            return rc;
        }
    }

    @Override
    protected void unread(ScatteringByteChannel input, int offset, int length) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void close(ScatteringByteChannel input) throws IOException
    {
        input.close();
    }

    @Override
    public void insert(char[] text) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void insert(CharSequence text) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write(int start, int length, Writer writer) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write(Writer writer) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getString(int start, int length)
    {
        StringBuilder sb = new StringBuilder();
        for (int ii=0;ii<length;ii++)
        {
            sb.append((char)get(start+ii));
        }
        return sb.toString();
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
    public void include(Reader in, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reuse(CharSequence text)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
