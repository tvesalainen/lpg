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

import org.vesalainen.regex.SyntaxErrorException;

/**
 * @author Timo Vesalainen
 */
public class OffsetLocatorException extends SyntaxErrorException
{
    private String source;
    private long start;
    private long end;
    
    public OffsetLocatorException(String message, String source, long start, long end)
    {
        super(message);
        this.source = source;
        this.start = start;
        this.end = end;
    }

    public OffsetLocatorException(String message, String source, long start, long end, int lastChar, Throwable thr)
    {
        super(message, thr, lastChar);
        this.source = source;
        this.start = start;
        this.end = end;
    }

    public long getEnd()
    {
        return end;
    }

    public String getSource()
    {
        return source;
    }

    public long getStart()
    {
        return start;
    }

}
