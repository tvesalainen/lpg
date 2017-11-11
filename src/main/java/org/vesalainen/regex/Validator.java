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

import org.vesalainen.grammar.state.DFAState;
import org.vesalainen.grammar.state.DFA;
import java.io.IOException;

/**
 * @deprecated 11.11.2017
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Validator
{
    private DFAState<Integer> start;

    public Validator(String expression) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        DFA<Integer> dfa = Regex.createDFA(expression);
        start = dfa.getRoot();
    }

    public boolean match(String text)
    {
        DFAState<Integer> state = start;
        for (int ii=0;ii<text.length();ii++)
        {
            char cc = text.charAt(ii);
            state = state.transit(cc);
            if (state == null)
            {
                return false;
            }
        }
        return state.isAccepting();
    }
}
