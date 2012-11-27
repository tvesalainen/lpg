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

package org.vesalainen.regex.ant;

import java.util.HashMap;
import java.util.Map;
import org.vesalainen.bcc.SubClass;
import org.vesalainen.grammar.state.DFA;
import org.vesalainen.grammar.state.DFAState;
import org.vesalainen.grammar.state.NFA;
import org.vesalainen.grammar.state.NFAState;
import org.vesalainen.grammar.state.Scope;
import org.vesalainen.regex.RegexParser;

/**
 * @author Timo Vesalainen
 */
public abstract class AbstractDFAMap<T> extends HashMap<String,T>
{
    public abstract Class<? extends MapParser> getParserClass();
    public abstract T getErrorToken();
    public abstract T getEofToken();
    
    public DFA<T> createDFA()
    {
        RegexParser<T> regexParser = (RegexParser<T>) RegexParser.newInstance();
        Scope<NFAState<T>> nfaScope = new Scope<>("scope");
        Scope<DFAState<T>> dfaScope = new Scope<>("scope");
        NFA<T> nfa = null;
        for (Map.Entry<String, T> entry : entrySet())
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
}
