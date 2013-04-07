/*
 * Copyright (C) 2013 Timo Vesalainen
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

package org.vesalainen.parser;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.vesalainen.bcc.model.Typ;
import org.vesalainen.grammar.state.DFA;
import org.vesalainen.parser.annotation.MapDef;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.regex.MatchCompiler;
import org.vesalainen.regex.ant.AbstractDFAMap;
import org.vesalainen.regex.ant.MapParser;

/**
 * @author Timo Vesalainen
 */
public class MapCompiler extends GenClassCompiler
{

    public MapCompiler(TypeElement superClass) throws IOException
    {
        super(superClass);
    }

    @Override
    public void compile() throws IOException
    {
        if (!Typ.isAssignable(superClass.asType(), Typ.getTypeFor(MapParser.class)))
        {
            throw new IllegalArgumentException(superClass+" not extending MapParser");
        }
        MapDef mapDef = superClass.getAnnotation(MapDef.class);
        if (mapDef == null)
        {
            throw new IllegalArgumentException("@MapDef missing from "+superClass);
        }
        super.compile();
        
        Class<? extends AbstractDFAMap> mapClass = mapDef.mapClass();
        AbstractDFAMap map;
        try
        {
            map = mapClass.newInstance();
        }
        catch (InstantiationException | IllegalAccessException ex)
        {
            throw new IOException(ex);
        }
        DFA dfa = map.createDFA();
        MatchCompiler<?> ic = new MatchCompiler<>(dfa, map.getErrorToken(), map.getEofToken());
        subClass.overrideMethod(ic, Modifier.PUBLIC, "input", InputReader.class);
    }

}
