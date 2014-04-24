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

import org.vesalainen.parser.util.InputReader;
import java.io.IOException;
import java.io.Writer;

/**
 * Replacer interface provides more control on Regex replace method.
 * @author tkv
 */
public interface Replacer
{
    /**
     * When this method is called, the Regex expression has been found in input.
     * Matched text is available by a call to reader.toString(). There are two
     * choises. Write replacement text with writer or inserting it with reader.insert
     * method. Inserted text will be read as input and might match with regex.
     * @param reader
     * @param writer
     * @throws IOException 
     */
    void replace(InputReader reader, Writer writer) throws IOException;
}