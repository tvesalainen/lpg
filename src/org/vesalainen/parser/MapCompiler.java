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
import org.vesalainen.bcc.type.ClassWrapper;
import org.vesalainen.bcc.type.MethodWrapper;
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

    public MapCompiler(Class<?> superClass) throws IOException, ReflectiveOperationException
    {
        super(superClass);
    }

    public MapCompiler(ClassWrapper thisClass) throws IOException, ReflectiveOperationException
    {
        super(thisClass);
    }

    @Override
    public void compile() throws IOException, ReflectiveOperationException
    {
        if (!MapParser.class.isAssignableFrom(superClass))
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
        AbstractDFAMap map = mapClass.newInstance();
        DFA dfa = map.createDFA();
        MethodWrapper mw = MethodWrapper.wrap(MapParser.class.getDeclaredMethod("input", InputReader.class));
        MatchCompiler<?> ic = new MatchCompiler<>(dfa, map.getErrorToken(), map.getEofToken());
        mw.setImplementor(ic);
        subClass.implement(mw);
    }

}
