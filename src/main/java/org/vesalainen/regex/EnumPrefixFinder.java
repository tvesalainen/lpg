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

import org.vesalainen.regex.Regex.Option;

/**
 * EnumPrefixFinder can resolve Enum value from unique prefix 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class EnumPrefixFinder<T extends Enum<T>>
{
    protected RegexMatcher<T> matcher;
    /**
     * Creates EnumPrefixFinder for only given enums
     * @param ignoreCase
     * @param ens 
     */
    public EnumPrefixFinder(boolean ignoreCase, T... ens)
    {
        Option[] options = null;
        if (ignoreCase)
        {
            options = new Option[]{Option.CASE_INSENSITIVE};
        }
        else
        {
            options = new Option[]{};
        }
        matcher = new RegexMatcher<>();
        for (T en : ens)
        {
            matcher.addExpression(en.name(), en, options);
        }
        matcher.compile();
    }
    /**
     * Creates EnumPrefixFinder for all enums
     * @param cls
     * @param options 
     */
    public EnumPrefixFinder(Class<T> cls, Regex.Option... options)
    {
        matcher = new RegexMatcher<>();
        for (T en : cls.getEnumConstants())
        {
            matcher.addExpression(en.name(), en, options);
        }
        matcher.compile();
    }
    /**
     * Returns enum for text if it is unique prefix.
     * @param text
     * @return 
     */
    public T find(String text)
    {
        T match = matcher.match(text, true);
        if (match != null && match.name().startsWith(text))
        {
            return match;
        }
        return null;
    }
}
