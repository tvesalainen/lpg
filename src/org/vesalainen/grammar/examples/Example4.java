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

import org.vesalainen.parser.ParserCompiler;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.annotation.Terminals;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;

/**
 *
 * @author tkv
 */
@GenClassname("org.vesalainen.lpg.examples.Example4Impl")
@GrammarDef
@Terminals({
    @Terminal(left="a", expression="a")
})
@Rules({
    @Rule(left="S", value={"A"}),
    @Rule(left="C"),
    @Rule(left="D"),
    @Rule(left="B"),
    @Rule(left="A", value={"B", "C", "D", "A"}),
    @Rule(left="A", value={"a"})
})
public abstract class Example4
{

    public static void main(String[] args)
    {
        try
        {
            ParserCompiler pc = new ParserCompiler(Example4.class);
            pc.compile();
            pc.loadDynamic();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
