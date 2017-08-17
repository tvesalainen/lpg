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
 * Used to tag recover method in parser class. This method will be called when
 * syntax or other error occurs during parsing.
 * 
 * <p>Recover method can have parameters annotated with @ParserContext and using
 * local variable names defined in ParserConstants. When recover method is called
 * the parameters have their current values.
 * 
 * <p>If one of recover method parameters is derived of Exception and annotated
 * with @ParserContext with name ParserConstants.Exception, then exceptions
 * thrown during parsing are passed to parser using recover method.
 * 
 * <code>
    @RecoverMethod
    public void recover(
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext(ParserConstants.InputReader) InputReader reader,
            @ParserContext(ParserConstants.ExpectedDescription) String expected,
            @ParserContext(ParserConstants.LastToken) String got,
            @ParserContext(ParserConstants.Exception) Exception thr
            ) throws IOException
    {
        System.err.println("Expected "+expected);
        System.err.println("Got      "+got);
        String input = reader.getInput();
        ...
 * </code>
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see org.vesalainen.parser.ParserConstants
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface RecoverMethod
{
}
