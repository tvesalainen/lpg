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
 * <p>Annotation GrammarDef defines parameters for created Parser class.
 *
 * <p>Field lrkLevel defines the maximum k for LALR(k) grammar. Most grammars are
 * LALR(1).
 *
 * <p>Field maxStack defines the maximum depth for parser stack. Parser compiler
 * calculates the maximum depth. It can be infinite. In that case maxStack is
 * used.
 * 
 * <p>grammar If grammar != "" it contains the grammar. In that case 
 * parser class is not searched for @Rule(s) or @Terminal(s) annotations.
 *
 * @author tkv
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface GrammarDef
{
    int lrkLevel() default 5;
    int maxStack() default 100;
    String grammar() default "";
}
