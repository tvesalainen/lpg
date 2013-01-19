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
 * Annotation Terminal is used to add terminals to a grammar. Terminals are regular
 * expression with a limitation, that the expression is not allowed to accept
 * empty string.
 *
 * <p>Left is the name of terminal. If omitted the method name is used.
 *
 * <p>Expression is the regular expression.
 * 
 * <p>priority set priority for terminal.
 * 
 * @author tkv
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Terminal
{
    String left() default "";
    String expression();
    Regex.Option[] options() default {};
    /**
     * Set priority > 0 for reserved words when E.g identifier matches reserved words
     * @return 
     */
    int priority() default 0;
    /**
     * If type is int, the base tells the integer base.
     * @return 
     */
    int base() default 10;
}
