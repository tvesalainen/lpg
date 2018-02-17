/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.Map;
import org.vesalainen.regex.Regex.Option;

/**
 * PrefixMap is a special map-type class that matches mappings with unique
 * prefixes. E.g. for mapping: foo -&gt; 1, bar -&gt; 2 returns 1 for strings
 * f, fo, foo, fooo, ...
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PrefixMap<T>
{
    private RegexMatcher<T> matcher;

    public PrefixMap(Map<String,T> map, Option... options)
    {
        matcher = new RegexMatcher<>();
        map.forEach((s,t)->matcher.addExpression(Regex.escape(s), t, options));
        matcher.compile();
    }
    
    public T get(CharSequence text)
    {
        return matcher.match(text, true);
    }
}
