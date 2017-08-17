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
package org.vesalainen.regex;

/**
 * This is part of the original hand written part of regex parser. It is replaced by RegexParser class
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class EscapeResolver
{
    private String text;
    private boolean escaped;
    private int index;

    public EscapeResolver(String text)
    {
        this.text = text;
    }

    public boolean hasNext()
    {
        return index < text.length();
    }

    public int getIndex()
    {
        return index;
    }

    public int peek()
    {
        int save = index;
        int cc = next();
        index = save;
        return cc;
    }

    public int next()
    {
        char cc = text.charAt(index++);
        if (cc != '\\')
        {
            escaped = false;
            return cc;
        }
        else
        {
            escaped = true;
            cc = text.charAt(index++);
            switch (cc)
            {
                case 't':
                    return '\t';
                case 'n':
                    return '\n';
                case 'r':
                    return '\r';
                case 'f':
                    return '\f';
                case 'a':
                    return '\u0007';
                case 'e':
                    return '\u001B';
                case 'x':
                    return shortHex();
                case 'u':
                    return longHex();
                case '0':
                    throw new UnsupportedOperationException("Unsupported escape sequence '"+cc+"' at "+index+" in "+text);
                case 'z':
                    throw new UnsupportedOperationException("Unsupported escape sequence '"+cc+"' at "+index+" in "+text);
                default:
                    return cc;
            }
        }
    }

    private Character shortHex()
    {
        String s = text.substring(index, index+2);
        index += 2;
        return (char)Integer.parseInt(s, 16);
    }

    public boolean isEscaped()
    {
        return escaped;
    }

    private Character longHex()
    {
        String s = text.substring(index, index+4);
        index += 4;
        return (char)Integer.parseInt(s, 16);
    }

}
