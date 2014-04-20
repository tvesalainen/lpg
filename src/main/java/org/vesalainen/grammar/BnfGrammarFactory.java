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
package org.vesalainen.grammar;

import java.util.ArrayList;
import java.util.List;
import org.vesalainen.parser.GenClassFactory;

/**
 * BnfGrammarFactory is a ... */
public class BnfGrammarFactory implements BnfGrammarIntf
{

    public static final String BnfGrammarClass = "org.vesalainen.grammar.impl.BnfParser";
    public static BnfGrammarIntf newInstance()
    {
        BnfGrammarIntf parser = (BnfGrammarIntf) GenClassFactory.loadGenInstance(BnfGrammarClass);
        if (parser == null)
        {
            return new BnfGrammarFactory();
        }
        else
        {
            return parser;
        }
    }
    @Override
    public void parseBnf(
            CharSequence text, 
            Grammar g
            )
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<String> parseRhs(String text, Grammar g)
    {
        return parseRhsString(text, g);
    }
    protected List<String> parseRhsString(String text, Grammar g)
    {
        if (!Grammar.isAnonymousTerminal(text))
        {
            checkSymbol(text);
        }
        List<String> list = new ArrayList<>();
        list.add(text);
        return list;
    }
    private void checkSymbol(CharSequence text)
    {
        for (int ii=0;ii<text.length();ii++)
        {
            switch (text.charAt(ii))
            {
                case ' ':
                case '\t':
                case '?':
                case '*':
                case '+':
                    throw new IllegalArgumentException(text+" Illegal symbol in bootstrap mode");
            }
        }
    }
}
