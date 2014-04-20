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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.model.Typ;
import static org.vesalainen.grammar.GrammarConstants.*;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;

/**
 * SyntheticParserFactory 
 * @author Timo Vesalainen
 */
public abstract class SyntheticParserFactory implements SyntheticParserIntf
{
    public static final String SyntheticParserClass = "org.vesalainen.grammar.impl.SyntheticParserImpl";
    
    public static SyntheticParserIntf newInstance()
    {
        SyntheticParserIntf parser = (SyntheticParserIntf) GenClassFactory.loadGenInstance(SyntheticParserClass);
        return parser;
    }
    @Override
    public TypeMirror parse(String text, Grammar g)
    {
        throw new UnsupportedOperationException("not supported");
    }
}
