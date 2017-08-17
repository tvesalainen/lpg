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
 * @MathExpression is used to generate mathematical functions
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * 
 * <p>value is the expression. Following terms are allowed
 * 
 * <p>Method arguments as variables.
 * 
 * <p>Methods as functions. Methods are searched in following order. Defining class
 * and its super classes. java.lang.Math methods. org.vesalainen.grammar.math.MoreMath 
 * methods.
 * 
 * <p>Defining class enums. Ordinal values.
 * 
 * <p>If degrees = true the java.lang.Math method arguments are converted from 
 * degrees before invocation of: sin, cos, tan 
 * 
 * <p>If degrees = true the java.lang.Math method return values are converted to 
 * degrees after invocation of: acos, asin, atan, atan2, 
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface MathExpression
{
    String value();
    boolean degrees() default false;
}
