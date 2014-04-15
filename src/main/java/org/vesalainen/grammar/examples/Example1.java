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
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.Rule;

/**
 *
 * @author tkv
 */
@GenClassname("org.vesalainen.lpg.examples.Example1Impl")
@GrammarDef
public abstract class Example1
{

    @Rule(left="E", value={"E", "star", "B"})
    public int mul(int e, int b)
    {
        return e*b;
    }
    @Rule(left="E", value={"E", "plus", "B"})
    public int add(int e, int b)
    {
        return e+b;
    }
    @Rule(left="E", value={"B"})
    public int eb(int b)
    {
        return b;
    }
    @Rule(left="B", value={"zero"})
    public int b0(String b)
    {
        return Integer.parseInt(b);
    }
    @Rule(left="B", value={"one"})
    public int b1(int b)
    {
        return b;
    }
    @Terminal(expression = "\\*")
    public void star()
    {

    }
    @Terminal(expression = "\\+")
    public void plus()
    {

    }
    @Terminal(expression = "0")
    public int zero()
    {
        return 0;
    }
    @Terminal(expression = "1")
    public int one()
    {
        return 1;
    }

    public static void main(String[] args)
    {
        try
        {
            ParserCompiler pc = new ParserCompiler(El.getTypeElement(Example1.class.getCanonicalName()));
            pc.compile();
            Example1 ex1 = (Example1) pc.newInstance();
            System.err.println(ex1);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
