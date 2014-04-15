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
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import org.vesalainen.bcc.model.Typ;
import org.vesalainen.grammar.state.DFA;
import org.vesalainen.grammar.state.DFAState;
import org.vesalainen.grammar.state.NFA;
import org.vesalainen.grammar.state.NFAState;
import org.vesalainen.grammar.state.Scope;
import org.vesalainen.parser.annotation.DFAMap;
import org.vesalainen.parser.annotation.DFAMapEntry;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.regex.MatchCompiler;
import org.vesalainen.regex.RegexParserFactory;
import org.vesalainen.regex.RegexParserIntf;
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
        DeclaredType superType = (DeclaredType) superClass.asType();
        if (!Typ.isAssignable(superType, Typ.getTypeFor(MapParser.class)))
        {
            throw new IllegalArgumentException(superClass+" not extending MapParser");
        }
        DFAMap mapDef = superClass.getAnnotation(DFAMap.class);
        if (mapDef == null)
        {
            throw new IllegalArgumentException("@DFAMap missing from "+superClass);
        }
        super.compile();

        Map<String,String> map = createMap(mapDef);
        DFA<String> dfa = createDFA(map);
        MatchCompiler<String> ic = new MatchCompiler<>(
                dfa, 
                mapDef.error(), 
                mapDef.eof()
                );
        subClass.overrideMethod(ic, Modifier.PUBLIC, "input", InputReader.class);
    }

    private <T> DFA<T> createDFA(Map<String,T> map)
    {
        RegexParserIntf<T> regexParser = (RegexParserIntf<T>) RegexParserFactory.newInstance();
        Scope<NFAState<T>> nfaScope = new Scope<>("scope");
        Scope<DFAState<T>> dfaScope = new Scope<>("scope");
        NFA<T> nfa = null;
        for (Map.Entry<String, T> entry : map.entrySet())
        {
            String expression = entry.getKey();
            T token = entry.getValue();
            if (nfa == null)
            {
                nfa = regexParser.createNFA(nfaScope, expression, token);
            }
            else
            {
                NFA<T> nfa2 = regexParser.createNFA(nfaScope, expression, token);
                nfa = new NFA(nfaScope, nfa, nfa2);
            }
        }
        return nfa.constructDFA(dfaScope);
    }
    private Map<String,String> createMap(DFAMap mapdef)
    {
        Map<String,String> map = new HashMap<>();
        for (DFAMapEntry e : mapdef.value())
        {
            map.put(e.key(), e.value());
        }
        return map;
    }
}
