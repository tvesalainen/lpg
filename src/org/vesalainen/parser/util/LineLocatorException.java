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
public class LineLocatorException extends SyntaxErrorException
{
    private String source;
    private int startLine;
    private int startColumn;
    private int endLine;
    private int endColumn;
    
    public LineLocatorException(String message, String source, int startLine, int startColumn)
    {
        super(message);
        this.source = source;
        this.startLine = startLine;
        this.startColumn = startColumn;
    }

    public LineLocatorException(String message, String source, int startLine, int startColumn, int endLine, int endColumn)
    {
        super(message);
        this.source = source;
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    public int getEndColumn()
    {
        return endColumn;
    }

    public int getEndLine()
    {
        return endLine;
    }

    public int getStartColumn()
    {
        return startColumn;
    }

    public int getStartLine()
    {
        return startLine;
    }

    public String getSource()
    {
        return source;
    }

}
