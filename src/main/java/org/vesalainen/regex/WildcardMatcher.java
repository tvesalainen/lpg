/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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

/**
 * A simplified RegexMatcher which accepts literal expression with simple ? and *
 * wildcards.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 * @see org.vesalainen.regex.Regex#wildcard(java.lang.String) 
 */
public class WildcardMatcher<T> extends RegexMatcher<T>
{

    public WildcardMatcher()
    {
    }
    /**
     * @deprecated Use WildcardMatcher() and addExpression(...
     * @param expr
     * @param attach
     * @param options 
     */
    public WildcardMatcher(String expr, T attach, Regex.Option... options)
    {
        addExpression(expr, attach, options);
    }

    @Override
    public void addExpression(String expr, T attach, Regex.Option... options)
    {
        super.addExpression(Regex.wildcard(expr), attach, options);
    }

}
