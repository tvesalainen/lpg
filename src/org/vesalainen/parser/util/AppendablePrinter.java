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

import java.io.IOException;
import java.util.Locale;

/**
 *
 * @author tkv
 */
public class AppendablePrinter implements Appendable
{
    protected Appendable out;

    public AppendablePrinter(Appendable out)
    {
        this.out = out;
    }
    @Override
    public Appendable append(CharSequence csq) throws IOException
    {
        return out.append(csq);
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException
    {
        return out.append(csq, start, end);
    }

    @Override
    public Appendable append(char c) throws IOException
    {
        return out.append(c);
    }

    public void format(String format, Object... args) throws IOException
    {
        out.append(String.format(format, args));
    }

    public void format(Locale l, String format, Object... args) throws IOException
    {
        out.append(String.format(l, format, args));
    }

    public void print(boolean b) throws IOException
    {
        out.append(Boolean.toString(b));
    }

    public void print(char c) throws IOException
    {
        out.append(c);
    }

    public void print(int i) throws IOException
    {
        out.append(Integer.toString(i));
    }

    public void print(long l) throws IOException
    {
        out.append(Long.toString(l));
    }

    public void print(float f) throws IOException
    {
        out.append(Float.toString(f));
    }

    public void print(double d) throws IOException
    {
        out.append(Double.toString(d));
    }

    public void print(char[] s) throws IOException
    {
        for (char cc : s)
        {
            out.append(cc);
        }
    }

    public void print(String s) throws IOException
    {
        out.append(s);
    }

    public void print(Object obj) throws IOException
    {
        out.append(obj.toString());
    }

    public void printf(String format, Object... args) throws IOException
    {
        out.append(String.format(format, args));
    }

    public void printf(Locale l, String format, Object... args) throws IOException
    {
        out.append(String.format(l, format, args));
    }

    public void println() throws IOException
    {
        out.append('\n');
    }

    public void println(boolean b) throws IOException
    {
        out.append(Boolean.toString(b)).append('\n');
    }

    public void println(char c) throws IOException
    {
        out.append(c).append('\n');
    }

    public void println(int i) throws IOException
    {
        out.append(Integer.toString(i)).append('\n');
    }

    public void println(long l) throws IOException
    {
        out.append(Long.toString(l)).append('\n');
    }

    public void println(float f) throws IOException
    {
        out.append(Float.toString(f)).append('\n');
    }

    public void println(double d) throws IOException
    {
        out.append(Double.toString(d)).append('\n');
    }

    public void println(char[] s) throws IOException
    {
        for (char cc : s)
        {
            out.append(cc);
        }
        out.append('\n');
    }

    public void println(String s) throws IOException
    {
        out.append(s).append('\n');
    }

    public void println(Object obj) throws IOException
    {
        out.append(obj.toString()).append('\n');
    }

}
