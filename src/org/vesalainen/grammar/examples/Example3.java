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
import org.vesalainen.parser.annotation.Terminals;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;

/**
 *
 * @author tkv
 */
@GenClassname("org.vesalainen.lpg.examples.Example3Impl")
@GrammarDef
@Terminals({
    @Terminal(left="a", expression="a"),
    @Terminal(left="b", expression="b"),
    @Terminal(left="c", expression="c"),
    @Terminal(left="d", expression="d"),
    @Terminal(left="f", expression="f")
})
@Rules({
    @Rule(left="S", value={"A"}),
    @Rule(left="A", value={"b", "B"}),
    @Rule(left="B", value={"c", "C"}),
    @Rule(left="B", value={"c", "C", "f"}),
    @Rule(left="C", value={"d", "A"}),
    @Rule(left="A", value={"a"}),
    @Rule(left="A", value={"b"})
})
public abstract class Example3
{
    public static void main(String[] args)
    {
        try
        {
            ParserCompiler pc = new ParserCompiler(El.getTypeElement(Example3.class.getCanonicalName()));
            pc.compile();
            pc.loadDynamic();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
