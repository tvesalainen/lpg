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
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import org.vesalainen.parser.ParserFeature;

/**
 *
 * @author Timo Vesalainen
 * @param <I> A class providing input.
 */
public abstract class ByteInput<I> extends Input<I, ByteBuffer>
{
    protected byte[] array;
    
    protected ByteInput(int size, boolean direct, EnumSet<ParserFeature> features)
    {
        super(features);
        this.size = size;
        if (direct)
        {
            this.buffer1 = ByteBuffer.allocateDirect(size);
        }
        else
        {
            this.buffer1 = ByteBuffer.allocate(size);
        }
        this.buffer2 = buffer1.duplicate();
        this.array1 = new ByteBuffer[] {buffer1};
        this.array2 = new ByteBuffer[] {buffer1, buffer2};
        if (buffer1.hasArray())
        {
            this.array = buffer1.array();
        }
    }
    protected ByteInput(byte[] array, EnumSet<ParserFeature> features)
    {
        super(features);
        this.size = array.length;
        this.buffer1 = ByteBuffer.wrap(array);
        this.buffer2 = buffer1.duplicate();
        this.array1 = new ByteBuffer[] {buffer1};
        this.array2 = new ByteBuffer[] {buffer1, buffer2};
        this.array = buffer1.array();
        this.end = array.length;
    }
    protected ByteInput(ByteBuffer buffer, EnumSet<ParserFeature> features)
    {
        super(features);
        this.size = buffer.limit();
        this.buffer1 = buffer.duplicate();
        this.buffer2 = buffer1.duplicate();
        this.array1 = new ByteBuffer[] {buffer1};
        this.array2 = new ByteBuffer[] {buffer1, buffer2};
        if (buffer1.hasArray())
        {
            this.array = buffer1.array();
        }
        this.cursor = buffer.position();
        this.end = buffer.limit();
    }
    @Override
    protected int get(int index)
    {
        return buffer1.get(index % size) & 0xff;
    }

    @Override
    protected void set(int index, int value)
    {
        buffer1.put(index % size, (byte) value);
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
    public void reuse(CharSequence text)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getArray()
    {
        return array;
    }
    /**
     * Returns string from buffer
     * @param start Start of input
     * @param length Length of input
     * @return 
     */
    @Override
    public String getString(int start, int length)
    {
        if (array != null)
        {
            int ps = start % size;
            int es = (start+length) % size;
            if (ps < es)
            {
                return new String(array, ps, length, StandardCharsets.US_ASCII);
            }
            else
            {
                StringBuilder sb = new StringBuilder();
                sb.append(new String(array, ps, size-ps, StandardCharsets.US_ASCII));
                sb.append(new String(array, 0, es, StandardCharsets.US_ASCII));
                return sb.toString();
            }
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            for (int ii=0;ii<length;ii++)
            {
                sb.append((char)get(start+ii));
            }
            return sb.toString();
        }
    }
    /**
     * Inserts text at cursor position
     * @param text 
     * @throws java.io.IOException 
     */
    @Override
    public void insert(char[] text) throws IOException
    {
        int ln = text.length;
        if (ln == 0)
        {
            return;
        }
        if (ln >= size - (end-cursor))
        {
            throw new IOException(Arrays.toString(text)+" doesn't fit in the buffer");
        }
        if (cursor != end)
        {
            makeRoom(ln);
        }
        if (array != null)
        {
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
        }
        else
        {
            for (int ii=0;ii<ln;ii++)
            {
                set(cursor+ii, text[ii]);
            }
        }
        end += ln;
    }
    /**
     * Inserts text at cursor position
     * @param text 
     * @throws java.io.IOException 
     */
    @Override
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
    @Override
    protected void makeRoom(int ln)
    {
        if (array != null)
        {
            int src;
            int dst;
            int len;
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
        else
        {
            int len = end-cursor;
            for (int ii=0;ii<len;ii++)
            {
                set(end+ln-ii, get(end-ln));
            }
        }
    }
    
}
