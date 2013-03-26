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
package org.vesalainen.grammar.examples;

import org.vesalainen.bcc.model.El;
import org.vesalainen.parser.ParserCompiler;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;

/**
 * @version $Id$
 * @author Timo Vesalainen
 */
@GenClassname("org.vesalainen.lpg.examples.Example5Impl")
@GrammarDef
@Rules({
    @Rule(left="S", value="'fdflsdkfösdlöfkasdlgkldfgjdkjgklajflalöklasdkglajgkdkghahgkaljklaj'"),
    @Rule(left="S", value="'adflsdkfösdlöfkasdlgkldfgjdkjgklajflalöklasdkglajgkdkghahgkaljklaj'"),
    @Rule(left="S", value="'bdflsdkfösdlöfkasdlgkldfgjdkjgklajflalöklasdkglajgkdkghahgkaljklaj'"),
    @Rule(left="S", value="'ddflsdkfösdlöfkasdlgkldfgjdkjgklajflalöklasdkglajgkdkghahgkaljklaj'"),
    @Rule(left="S", value="'edflsdkfösdlöfkasdlgkldfgjdkjgklajflalöklasdkglajgkdkghahgkaljklaj'"),
    @Rule(left="S", value="'fdflsdkfösdlöfkasdlgkldfgjdkjgklajflalöklasdkglajgkdkghahgkaljklaj'"),
    @Rule(left="S", value="'gdflsdkfösdlöfkasdlgkldfgjdkjgklajflalöklasdkglajgkdkghahgkaljklaj'"),
    @Rule(left="S", value="'hdflsdkfösdlöfkasdlgkldfgjdkjgklajflalöklasdkglajgkdkghahgkaljklaj'"),
    @Rule(left="S", value="'idflsdkfösdlöfkasdlgkldfgjdkjgklajflalöklasdkglajgkdkghahgkaljklaj'"),
    @Rule(left="S", value="'jdflsdkfösdlöfkasdlgkldfgjdkjgklajflalöklasdkglajgkdkghahgkaljklaj'"),
    @Rule(left="S", value="'kdflsdkfösdlöfkasdlgkldfgjdkjgklajflalöklasdkglajgkdkghahgkaljklaj'"),
    @Rule(left="S", value="'ldflsdkfösdlöfkasdlgkldfgjdkjgklajflalöklasdkglajgkdkghahgkaljklaj'"),
    @Rule(left="S", value="'mdflsdkfösdlöfkasdlgkldfgjdkjgklajflalöklasdkglajgkdkghahgkaljklaj'"),
    @Rule(left="S", value="'ndflsdkfösdlöfkasdlgkldfgjdkjgklajflalöklasdkglajgkdkghahgkaljklaj'"),
    @Rule(left="A", value={"a"}),
    @Rule(left="B", value={"b"})
})
public abstract class Example5
{
    @ParseMethod(start="S")
    public abstract void parse(String text);
    
    public static void main(String[] args)
    {
        try
        {
            ParserCompiler pc = new ParserCompiler(El.getTypeElement(Example5.class.getCanonicalName()));
            pc.compile();
            Example5 e =(Example5) pc.newInstance();
            e.parse("edflsdkfösdlöfkasdlgkldfgjdkjgklajflalöklasdkglajgkdkghahgkaljklaj");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
