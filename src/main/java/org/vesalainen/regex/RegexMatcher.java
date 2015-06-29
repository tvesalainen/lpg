/*
 * Copyright (C) 2015 tkv
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

import org.vesalainen.grammar.state.DFA;
import org.vesalainen.grammar.state.DFAState;
import org.vesalainen.grammar.state.NFA;
import org.vesalainen.grammar.state.NFAState;
import org.vesalainen.grammar.state.Scope;
import org.vesalainen.regex.Regex.Option;
import org.vesalainen.util.Matcher;

/**
 *
 * @author tkv
 * @param <T>
 */
public class RegexMatcher<T> implements Matcher<T>
{
    private RegexParserIntf<T> parser = RegexParserFactory.newInstance();
    private Scope<NFAState<T>> nfaScope = new Scope<>("org.vesalainen.regex.RegexMatcher");
    private NFA<T> nfa;
    private DFA<T> dfa;
    private DFAState<T> root;
    private DFAState<T> state;

    public RegexMatcher()
    {
    }
    
    public RegexMatcher(String expr, T attach, Option... options)
    {
        addExpression(expr, attach, options);
    }
    
    public void addExpression(String expr, T attach, Option... options)
    {
        if (nfa == null)
        {
            nfa = parser.createNFA(nfaScope, expr, attach, options);
        }
        else
        {
            NFA<T> nfa2 = parser.createNFA(nfaScope, expr, attach, options);
            nfa = new NFA<>(nfaScope, nfa, nfa2);
        }
    }
    public void compile()
    {
        Scope<DFAState<T>> dfaScope = new Scope<>("org.vesalainen.regex.RegexMatcher");
        dfa = nfa.constructDFA(dfaScope);
        state = root = dfa.getRoot();
        parser = null;
        nfaScope = null;
        nfa = null;
    }
    @Override
    public Status match(int cc)
    {
        state = state.transit(cc);
        if (state != null)
        {
            if (state.isAccepting())
            {
                return Status.Match;
            }
            else
            {
                return Status.Ok;
            }
        }
        else
        {
            return Status.Error;
        }
    }

    @Override
    public T getMatched()
    {
        return state.getToken();
    }

    @Override
    public void clear()
    {
        state = root;
    }
    
}
