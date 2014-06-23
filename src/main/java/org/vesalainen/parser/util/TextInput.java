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
import java.nio.charset.Charset;

/**
 *
 * @author Timo Vesalainen
 */
public class TextInput extends Input<CharSequence>
{
    private CharSequence text;

    public TextInput(CharSequence text)
    {
        this.text = text;
        this.size = text.length();
        this.end = size;
        setSource(text.toString());
    }
    
    @Override
    public void reuse(CharSequence text)
    {
        this.size = text.length();
        this.end = size;
        this.text = text;
        this.cursor = 0;
        this.includeLevel.reset();
        this.includeStack = null;
        this.length = 0;
        this.findSkip = 0;
        this.findMark = -1;  // position where find could have last accessed the string
        this.waterMark = 0;
        setSource(text.toString());
    }

    @Override
    protected int get(int index)
    {
        return text.charAt(index);
    }

    @Override
    protected void set(int index, int value)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    protected int fill(CharSequence input, int offset, int length) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    protected void close(CharSequence input) throws IOException
    {
    }

    @Override
    public void insert(char[] text) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void insert(CharSequence text) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void write(Writer writer) throws IOException
    {
        write(cursor-length, length, writer);
    }

    @Override
    public void write(int start, int len, Writer writer) throws IOException
    {
        writer.append(text, start, start+len);
    }

    @Override
    public String getString(int s, int l)
    {
        return text.subSequence(s, s+l).toString();
    }

    @Override
    public void include(Reader in, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void include(InputStream is, Charset cs, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void include(InputStream is, String cs, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void include(InputStream is, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

}
