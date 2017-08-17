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
import org.vesalainen.parser.ParserFeature;

/**
 * Marks the parse method in parser class. This method is called to parse the input.
 *
 * <p>The first parameter must be java.io.PushBackReader, java.io.Reader,
 * java.io.InputStream, java.lang.String, java.io. File or other compatible class 
 * instance.
 * @see org.vesalainen.parser.util.InputReader#getInstance
 * 
 * <p>If the first parameter is other
 * than java.lang.String the size parameter must be other than default (-1).
 *
 * <p>If this method has more than one parameter, the rest are used as parser
 * context and must be annotated with @ParserContext. If class contains more than
 * one parse method, the parser context parameters must be the same in every parse method.
 *
 * <p>Use start to define the start nonterminal
 *
 * <p>Use size to specify buffer size.
 *
 * <p>Use charSet to specify used charset.
 *
 * <p>Set upper=true to convert input to upper-case
 *
 * <p>Set lower=true to convert input to lower-case
 * 
 * <p>whiteSpace Terminals which are marked white-space are listed in whiteSpace
 * array. White-space terminals are not processed in grammar level. White-space terminals
 * can have reducer. Reducers are called when white-space input is read. If such 
 * reducer returns value, that value is inserted in input. Return type must match
 * one of InputReader.input method parameter.
 * @see org.vesalainen.parser.util.InputReader#insert(java.lang.CharSequence) 
 * @see org.vesalainen.parser.util.InputReader#insert(char[]) 
 *
 * <p>wideIndex If true the goto and jrs instructions are replaced with goto_w 
 * and jsr_w. Set this flag if compiled method is big.
 * 
 * <p>eof Set eof terminal to differ from normal. This is useful only in sub-grammar
 * 
 * <p>syntaxOnly If syntaxOnly is set, the reducer methods are not used in parsing.
 * This can be used to check syntax only.
 * 
 * <p>features is used to enlist needed features. Features is redundant for 
 * upper, lower, wideIndex, syntaxOnly and 
 * useOffsetLocatorException which are deprecated.
 * 
 * @see org.vesalainen.parser.ParserFeature
 * 
 * <p>Examples:
 * <code>
 * Usage of one @ParserContext parameter
    @ParseMethod
    protected abstract void parse(String text, @ParserContext Calendar calendar) throws IOException;

    protected void ad(@ParserContext Calendar cal)
    {
        cal.set(Calendar.ERA, 0);
    }

 * Usage of several @ParserContext parameters
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

    protected List<Symbol> rhs(List<Symbol> rhs, String literal, @ParserContext("GRAMMAR") Grammar grammar)
    {
        GTerminal t = (GTerminal) grammar.addSymbol(new GTerminal("'"+Regex.escape(literal)+"'"));
        rhs.add(t);
        return rhs;
    }
 * </code>
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface ParseMethod
{
    String start();
    int size() default -1;
    String charSet() default "";
    @Deprecated boolean upper() default false;
    @Deprecated boolean lower() default false;
    String eof() default "";
    String[] whiteSpace() default {};
    @Deprecated boolean wideIndex() default false;
    @Deprecated boolean syntaxOnly() default false;
    @Deprecated boolean useOffsetLocatorException() default false;
    ParserFeature[] features() default {};
}
