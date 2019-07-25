/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import org.vesalainen.parser.annotation.Terminal;
import static org.vesalainen.regex.Regex.Option.*;

/**
 * AbstractParser is a base class containing common reducers.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractParser
{
    /**
     * In Rule : identifier
     * <p>Starts with letter and can contain letter, digits and underscores.
     * @param value
     * @return
     */
    @Terminal(expression = "[a-zA-z][a-zA-z0-9_]*")
    protected abstract String identifier(String value);
    /**
     * In Rule : quote
     * <p>("), (') or (`´) quoted string without quote character.
     * @param value
     * @return
     */
    @Terminal(expression = "'[^']*'|\"[^\"]*\"|`[^´]´")
    protected String quote(String value)
    {
        return value.substring(1, value.length() - 1);
    }
    /**
     * In Rule : boolean
     * @param value
     * @return
     */
    @Terminal(left="boolean", expression = "true|false", options = {CASE_INSENSITIVE})
    protected abstract boolean bool(boolean value);

    /**
     * In Rule : char
     * @param value
     * @return
     */
    @Terminal(left="char", expression = "[^ \t\r\n]")
    protected abstract char ch(char value);

    /**
     * In Rule : short
     * @param value
     * @return
     */
    @Terminal(left="short", expression = "[\\+\\-]?[0-9]+")
    protected abstract short integer(short value);

    /**
     * In Rule : long
     * @param value
     * @return
     */
    @Terminal(left="long", expression = "[\\+\\-]?[0-9]+")
    protected abstract long integer(long value);

    /**
     * In Rule : int
     * @param value
     * @return
     */
    @Terminal(left="int", expression = "[\\+\\-]?[0-9]+")
    protected abstract int integer(int value);

    /**
     * In Rule : hex
     * @param value
     * @return
     */
    @Terminal(expression="[0-9a-fA-F]+", radix=16)
    protected abstract int hex(int value);
    /**
     * In Rule : float
     * @param value
     * @return
     */
    @Terminal(left="float", expression = "[\\+\\-]?[0-9]+\\.[0-9]+")
    protected abstract float decimal(float value);

    /**
     * In Rule : double
     * @param value
     * @return
     */
    @Terminal(left="double", expression = "[\\+\\-]?[0-9]+\\.[0-9]+")
    protected abstract double decimal(double value);
    
    @Terminal(expression = "[ \t\r\n]+")
    protected abstract void whiteSpace();

    @Terminal(expression = "\\-\\-[^\n]*\n")
    protected abstract void doubleSlashComment();

    @Terminal(expression = "#[^\n]*\n")
    protected abstract void hashComment();

    @Terminal(expression = "/\\*.*\\*/", options = {FIXED_ENDER})
    protected abstract void cComment();
}
