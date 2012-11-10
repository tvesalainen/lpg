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
 * Use Rule annotation to add a rule to a grammar. Field left is the rule nonterminal.
 * Default value for left is the method name.
 * Field value is the rule right hand side. Right hand side value is either a nonterminal
 * name, a terminal name or anonymous terminal. Anonymous terminals are used for terminals
 * that doesn't need reducer methods. Separators like /,.: are examples of anonymous
 * terminals. Anonymous terminal regular expression is quoted with apostrophe (').
 *
 * <p>Example.
 * <p>EXPRESSION ::= EXPRESSION + TERM
 * <code>
     @Rule(left="Expression", value={"Expression", "PLUS", "Term"})
    public long plusExpression(long expr, long term)
    {
        return expr + term;
    }
 * </code>
 *
 * <p>Nonterminal names are like (us-ascii) method names. At right hand side it is
 * also possible to use grouping and quantifiers. Example:
 * <code>
    @Rule({"prolog", "element", "misc*"})
    protected void document(@ParserContext DefaultHandler2 handler)
    {

    }
 * </code>
 * <p>For misc* nonterminal two rules are added to the grammar.
 * <code>
 * misc* ::=
 * misc* ::= misc* misc
 * </code>
 *
 * <p>Quantifiers +, ? and * are allowed.
 *
 * Nonterminals can be grouped with or without quantifiers. Example:
 * <code>
    @Rule({"a1", "(a2 misc*)?"})
    protected void lhs()
    {

    }
 * </code>
 * <p>Note! If grouping contains anonymous terminals, use \x20 for space.
 * @author tkv
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Rule
{
    String left() default "";
    String[] value() default {};
}
