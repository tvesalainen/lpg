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
package org.vesalainen.parser.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows use of parser context at parse call level. Parse context can of course
 * be at field level. It is easies to keep parser thread safe by not using fields.
 * You define parser context in parser method call.
 * <code>
    @ParseMethod
    public abstract List<Symbol> parse(
            String format,
            @ParserContext("GRAMMAR") Grammar grammar,
            @ParserContext("LOCALE") Locale locale,
            @ParserContext("SYMBOLS") DateFormatSymbols symbols,
            @ParserContext("ERA") Method[] era,
            @ParserContext("MONTH") Method[] month,
            @ParserContext("WEEKDAY") Method[] weekday,
            @ParserContext("AMPM") Method[] ampm
            ) throws IOException;
 * </code>
 * Use the parse context in reducer methods
 * <code>
    @Rule(left="rhs", value={"rhs", "tG"})
    protected List<Symbol> era(
            List<Symbol> rhs, String name,
            @ParserContext("GRAMMAR") Grammar grammar,
            @ParserContext("SYMBOLS") DateFormatSymbols symbols,
            @ParserContext("ERA") Method[] era)
    { ...
 *
 * </code>
 * A special parser context $ringBufferReader is always accessible. it can be
 * used for example to get location information.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.PARAMETER})
public @interface ParserContext
{
    String value() default "$context";
}
