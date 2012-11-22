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
package org.vesalainen.parser;

/**
 * Whenever a class returned from one of reduce methods implements this interface.
 * The parser text location is reported by using setLocation method
 * @author Timo Vesalainen
 */
public interface ParserOffsetLocator
{
    /**
     * Sets the location of parsed item. 
     * <p>
     * Note! End location is the location of current input and might be past the
     * parsed item.
     * @param source Source of input. Eg. filename. Can be null.
     * @param start Start offset of parsed item.
     * @param end End offset of parsed item. Might be past the parsed item.
     */
    void setLocation(String source, int start, int end);
}
