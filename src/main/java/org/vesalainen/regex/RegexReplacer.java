/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RegexReplacer extends RegexMatcher<Replacer>
{

    public RegexReplacer()
    {
    }

    public RegexReplacer(String expr, Replacer attach, Regex.Option... options)
    {
        super(expr, attach, options);
    }
    /**
     * Compiles if not compiled. Replaces found string by using attached replacers.
     * 
     * <p>This method is thread safe:
     * @param text
     * @return 
     */
    public String replace(CharSequence text)
    {
        StringBuilder sb = new StringBuilder();
        replace(sb, text);
        return sb.toString();
    }
    /**
     * Compiles if not compiled. Replaces found string by using attached replacers.
     * 
     * <p>This method is thread safe:
     * @param sb
     * @param text 
     */
    public void replace(StringBuilder sb, CharSequence text)
    {
        if (!isCompiled())
        {
            compile();
        }
        DFAState<Replacer> st = root;
        Replacer replacer = null;
        int start = -1;
        int end = -1;
        int len = text.length();
        for (int ii=0;ii<len;ii++)
        {
            char cc = text.charAt(ii);
            st = st.transit(cc);
            if (st != null)
            {
                if (start == -1)
                {
                    start = ii; // start of match
                }
                if (st.isAccepting())
                {
                    end = ii;   // last accpting point
                    replacer = st.getToken();
                }
            }
            else
            {
                // error
                if (start != -1)
                {
                    // we have some matching
                    if (end != -1)
                    {
                        // accepted string
                        replacer.replace(sb, text, start, end+1);
                        ii = end;   // reparse after last match
                        end = -1;
                    }
                    else
                    {
                        // not accepted partial match
                        sb.append(text, start, ii+1);
                    }
                    start = -1;
                }
                else
                {
                    sb.append(cc);
                }
                st = root;
            }
        }
        if (start != -1)
        {
            // matching at end
            if (end != -1)
            {
                replacer.replace(sb, text, start, end+1);
            }
            else
            {
                sb.append(text, start, len);
            }
        }
    }

}
