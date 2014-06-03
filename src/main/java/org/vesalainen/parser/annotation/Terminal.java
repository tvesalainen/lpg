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
 * <p>doc is the terminal documentation.
 *
 * <p>left is the name of terminal. If omitted the method name is used.
 *
 * <p>expression is the regular expression.
 * 
 * <p>priority set priority for terminal.
 * 
 * <p>reducer method in text form. Used only with class annotation. Format is
 * &lt;canonical name of method class&gt; ' ' &lt;method name&gt; '(' arguments ')'
 * 
 * Arguments is a comma separated list of argument type names. Argument type name
 * is canonical name of argument class. Arrays however are printed with leading '['.
 * Example T0.class.getDeclaredMethod("m1", String.class, int.class, long[].class) 
 * produces "org.vesalainen.bcc.T0 m1(java.lang.String,int,[long)"
 * 
 * @author tkv
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Terminal
{
    String doc() default "";
    String left() default "";
    String expression();
    Regex.Option[] options() default {};
    /**
     * Set priority > 0 for reserved words when E.g identifier matches reserved words
     * @return 
     */
    int priority() default 0;
    /**
     * If type is int, the radix tells the integer radix.
     * 
     * <p>If radix = 2 means 1-complement while radix=-2 means 2-complement
     * @return 
     */
    int radix() default 10;
    String reducer() default "";
}
