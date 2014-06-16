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
public class InputText extends Input<CharSequence>
{
    private CharSequence text;

    public InputText(CharSequence text)
    {
        this.text = text;
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
    protected boolean ready(CharSequence input) throws IOException
    {
        return true;
    }

    @Override
    protected void insert(char[] text) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    protected void insert(CharSequence text) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    protected void write(Writer writer) throws IOException
    {
        writer.append(text);
    }

    @Override
    protected void write(int s, int l, Writer writer) throws IOException
    {
        writer.append(text, s, l);
    }

    @Override
    protected String getString(int s, int l)
    {
        return text.subSequence(s, s+l).toString();
    }

    @Override
    protected void include(Reader in, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    protected void include(InputStream is, Charset cs, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    protected void include(InputStream is, String cs, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    protected void include(InputStream is, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }
}
