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

/**
 *
 * @author Timo Vesalainen
 */
public abstract class CharSequenceInput extends Input<CharSequence>
{
    private int size;
    private char[] array;
    @Override
    public void reuse(CharSequence text)
    {
        size = text.length();
        end = size;
        array = text.toString().toCharArray();
        cursor = 0;
        includeLevel.reset();
        includeStack = null;
        length = 0;
        findSkip = 0;
        findMark = -1;  // position where find could have last accessed the string
        waterMark = 0;
        setSource(text.toString());
    }
}
