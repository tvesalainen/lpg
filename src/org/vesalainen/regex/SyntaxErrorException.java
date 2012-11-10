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
 *
 * @author tkv
 */
public class SyntaxErrorException extends RuntimeException
{
    public SyntaxErrorException()
    {
    }

    public SyntaxErrorException(String message)
    {
        super(message);
    }

    public SyntaxErrorException(Throwable cause, int at)
    {
        super(msg(at), cause);
    }

    public SyntaxErrorException(String message, Throwable cause, int at)
    {
        super(message+msg(at), cause);
    }

    public SyntaxErrorException(String message, int at)
    {
        super(message+msg(at));
    }

    public SyntaxErrorException(int at)
    {
        super(msg(at));
    }

    private static String msg(int at)
    {
        return " unaccepted char='"+(char)at+"' (0x"+Integer.toHexString(at)+")";
    }
}
