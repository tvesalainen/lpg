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

import org.vesalainen.regex.Regex;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @GenRegex is used to generate Regex instances in annotated parser classes. The 
 * fields type must be Regex. In generated parser class this field is initialized
 * to Regex. Example:
 * <code>
    @Terminal(expression="xmlns(:["+NCNameStartChar+"]["+NCNameChar+"]*)?", priority=1)
    protected String nsAttName(String s) throws IOException
    {
        String[] ss = colonRegex.split(s);
        if (ss.length == 2)
        {
            return ss[1];
        }
        else
        {
            return "";
        }
    }

    @GenRegex(":")
    protected Regex colonRegex;
    
 * </code>
 * @author tkv
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenRegex
{
    String value();
    Regex.Option[] options() default {};
}
