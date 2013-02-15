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

import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.annotation.Terminals;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import java.io.File;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.annotation.GenClassname;

/**
 *
 * @author tkv
 */
@GenClassname("org.vesalainen.grammar.examples.LegExampleImpl")
@GrammarDef()
@Terminals({
    @Terminal(left="SEMICOLON", expression=";"),
    @Terminal(left="ASSIGN", expression="="),
    @Terminal(left="LEFT_BRACKET", expression="\\["),
    @Terminal(left="RIGHT_BRACKET", expression="\\]"),
    @Terminal(left="PLUS", expression="\\+"),
    @Terminal(left="MINUS", expression="\\-"),
    @Terminal(left="DIVIDE", expression="/"),
    @Terminal(left="STAR", expression="\\*"),
    @Terminal(left="LEFT_PARENTHESIS", expression="\\("),
    @Terminal(left="RIGHT_PARENTHESIS", expression="\\)"),
    @Terminal(left="IDENTIFIER", expression="[ ]*[a-z][a-z0-9_]*"),
    @Terminal(left="CONSTANT", expression="[ ]*[0-9]+[ ]*"),
    @Terminal(left="IF", expression="[ ]*IF"),
    @Terminal(left="THEN", expression="THEN"),
    @Terminal(left="ELSE", expression="ELSE"),
    @Terminal(left="END", expression="END"),
    @Terminal(left="WHILE", expression="WHILE"),
    @Terminal(left="DO", expression="DO"),
    @Terminal(left="BREAK", expression="BREAK")
})
    @Rules({
    @Rule(left="block"),
    @Rule(left="block", value={"block", "statement"}),
    @Rule(left="statement", value={"variable", "ASSIGN", "expression", "SEMICOLON"}),
    @Rule(left="statement", value={"IF", "expression", "THEN", "block", "ELSE", "block", "END", "IF", "SEMICOLON"}),
    @Rule(left="statement", value={"WHILE", "expression", "DO", "block", "END", "WHILE", "SEMICOLON"}),
    @Rule(left="statement", value={"BREAK", "SEMICOLON"}),
    @Rule(left="statement", value={"array_declaration", "SEMICOLON"}),
    @Rule(left="array_declaration", value={"IDENTIFIER", "LEFT_BRACKET", "RIGHT_BRACKET"}),
    @Rule(left="array_declaration", value={"array_declaration", "LEFT_BRACKET", "RIGHT_BRACKET"}),
    @Rule(left="expression", value={"term"}),
    @Rule(left="expression", value={"expression", "PLUS", "term"}),
    @Rule(left="expression", value={"expression", "MINUS", "term"}),
    @Rule(left="term", value={"factor"}),
    @Rule(left="term", value={"term", "DIVIDE", "factor"}),
    @Rule(left="term", value={"term", "STAR", "factor"}),
    @Rule(left="factor", value={"variable"}),
    @Rule(left="factor", value={"CONSTANT"}),
    @Rule(left="factor", value={"LEFT_PARENTHESIS", "expression", "RIGHT_PARENTHESIS"}),
    @Rule(left="variable", value={"IDENTIFIER"}),
    @Rule(left="variable", value={"variable", "LEFT_BRACKET", "expression", "RIGHT_BRACKET"})
            })
public abstract class LegExample
{
    @ParseMethod(start="block")
    public abstract void parse(String txt);

    public static void main(String[] args)
    {
        try
        {
            LegExample rp = (LegExample) GenClassFactory.getGenInstance(LegExample.class);
            File log = new File("C:\\Users\\tkv\\Documents\\Visual Studio 2008\\Projects\\jikespg\\examples\\leg\\leg.l");
            //Tester t = new Tester(log, pc.getLrk());
            //t.test();
            rp.parse("IF 1 THEN a=2;ELSE b=3;END IF;");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
