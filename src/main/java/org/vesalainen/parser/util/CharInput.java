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
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.EnumSet;
import org.vesalainen.parser.ParserFeature;

/**
 *
 * @author Timo Vesalainen
 * @param <I> A class providing input.
 */
public abstract class CharInput<I> extends Input<I, CharBuffer>
{
    protected char[] array;
    
    protected CharInput(int size, EnumSet<ParserFeature> features)
    {
        super(features);
        this.size = size;
        this.buffer1 = CharBuffer.allocate(size);
        this.buffer2 = buffer1.duplicate();
        this.array1 = new CharBuffer[] {buffer1};
        this.array2 = new CharBuffer[] {buffer1, buffer2};
        if (buffer1.hasArray())
        {
            this.array = buffer1.array();
        }
    }
    protected CharInput(char[] array, EnumSet<ParserFeature> features)
    {
        super(features);
        this.size = array.length;
        this.buffer1 = CharBuffer.wrap(array);
        this.buffer2 = buffer1.duplicate();
        this.array1 = new CharBuffer[] {buffer1};
        this.array2 = new CharBuffer[] {buffer1, buffer2};
        if (buffer1.hasArray())
        {
            this.array = buffer1.array();
        }
    }
    protected CharInput(CharSequence text, EnumSet<ParserFeature> features)
    {
        super(features);
        this.size = text.length();
        this.buffer1 = CharBuffer.wrap(text);
        this.buffer2 = buffer1.duplicate();
        this.array1 = new CharBuffer[] {buffer1};
        this.array2 = new CharBuffer[] {buffer1, buffer2};
        if (buffer1.hasArray())
        {
            this.array = buffer1.array();
        }
    }
    @Override
    public int get(int index)
    {
        return buffer1.get(index % size);
    }

    @Override
    public void reuse(CharSequence text)
    {
        this.size = text.length();
        this.buffer1 = CharBuffer.wrap(text);
        this.buffer2 = buffer1.duplicate();
        this.array1 = new CharBuffer[] {buffer1};
        this.array2 = new CharBuffer[] {buffer1, buffer2};
        if (buffer1.hasArray())
        {
            this.array = buffer1.array();
        }
        end = size;
        cursor = 0;
        length = 0;
        findSkip = 0;
        findMark = -1;
        waterMark = 0;
    }

    @Override
    protected void set(int index, int value)
    {
        buffer1.put(index % size, (char)value);
    }

    @Override
    public void write(int start, int length, Writer writer) throws IOException
    {
        if (array != null)
        {
            if (start < end-size)
            {
                throw new IllegalArgumentException("buffer too small");
            }
            int ps = start % size;
            int es = (start+length) % size;
            if (ps <= es)
            {
                writer.write(array, ps, length);
            }
            else
            {
                writer.write(array, ps, size-ps);
                writer.write(array, 0, es);
            }
        }
        else
        {
            for (int ii=0;ii<length;ii++)
            {
                writer.append((char)get(start+ii));
            }
        }
    }

    @Override
    public void write(Writer writer) throws IOException
    {
        write(cursor-length, length, writer);
    }

    @Override
    public char[] getArray()
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
        if (length == 0)
        {
            return "";
        }
        if (array != null)
        {
            int ps = start % size;
            int es = (start+length) % size;
            if (ps < es)
            {
                return new String(array, ps, length);
            }
            else
            {
                StringBuilder sb = new StringBuilder();
                sb.append(array, ps, size-ps);
                sb.append(array, 0, es);
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
