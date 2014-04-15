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
package org.vesalainen.regex;

import org.vesalainen.grammar.state.NFA;
import org.vesalainen.grammar.state.NFAState;
import org.vesalainen.grammar.state.Scope;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.regex.Regex.Option;
import java.lang.Character.UnicodeBlock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.annotation.GenClassname;

public abstract class RegexParserFactory
{
    public static final String RegexParserClass = "org.vesalainen.regex.impl.RegexParserImpl";
    
    public static final RegexParserIntf newInstance()
    {
        Object instance = GenClassFactory.loadGenInstance(RegexParserClass);
        if (instance != null)
        {
            return (RegexParserIntf) instance;
        }
        System.err.println("Regex parser as TinyExpressionParser!");
        return new TinyExpressionParser();
    }
}
