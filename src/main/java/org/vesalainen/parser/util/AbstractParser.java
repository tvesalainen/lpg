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

import java.time.Instant;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Terminal;
import static org.vesalainen.regex.Regex.Option.*;

/**
 * AbstractParser is a base class containing common reducers.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractParser
{
    /**
     * In Rule : string
     * <p>Anything else than white-space.
     * @param value
     * @return
     */
    @Terminal(expression = "[^ \t\r\n]+")
    protected abstract String string(String value);
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
     * @param seq
     * @return
     */
    @Terminal(expression = "'[^']*'|\"[^\"]*\"|`[^´]´")
    protected String quote(CharSequence seq)
    {
        return seq.subSequence(1, seq.length() - 1).toString();
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
    
    @Terminal(expression = "[0-9]{4}[\\-][0-9]{2}[\\-][0-9]{2}[T][0-9]{2}[:][0-9]{2}[:][0-9]{2}[\\.][0-9]{3}[Z]")
    protected Instant instant(CharSequence instant)
    {
        return Instant.parse(instant);
    }

    @Rule("int '\u00b0' float `'´ char")
    protected double coordinate(int deg, float min, char wens)
    {
        double d = deg + min/60.0;
        switch (wens)
        {
            case 'N':
                if (d < 0 || d > 90)
                {
                    throw new IllegalArgumentException("latitude coordinate"+d);
                }
                return d;
            case 'S':
                if (d < 0 || d > 90)
                {
                    throw new IllegalArgumentException("latitude coordinate"+d);
                }
                return -d;
            case 'E':
                if (d < 0 || d > 180)
                {
                    throw new IllegalArgumentException("longitude coordinate"+d);
                }
                return d;
            case 'W':
                if (d < 0 || d > 180)
                {
                    throw new IllegalArgumentException("longitude coordinate"+d);
                }
                return -d;
            default:
                throw new UnsupportedOperationException(wens+" not supported");
        }
    }

    @Terminal(expression = "[ \t\r\n]+")
    protected abstract void whiteSpace();

    @Terminal(expression = "\\-\\-[^\n]*\n")
    protected abstract void doubleSlashComment();

    @Terminal(expression = "#[^\n]*[\n]?")
    protected abstract void hashComment();

    @Terminal(expression = "/\\*.*\\*/", options = {FIXED_ENDER})
    protected abstract void cComment();
}
