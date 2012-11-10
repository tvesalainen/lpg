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
import org.vesalainen.regex.Regex.Option;
import java.io.IOException;

/**
 *
 * @author tkv
 */
public interface RegexParserIntf
{

    /**
     * Creates a Nondeterministic finite automata from regular expression
     * @param expression
     * @param reducer Reducer marks the accepting state with unique identifier
     * @param options
     * @return
     * @throws IOException
     */
    NFA<Integer> createNFA(Scope<NFAState<Integer>> scope, String expression, int reducer, Option... options);
}
