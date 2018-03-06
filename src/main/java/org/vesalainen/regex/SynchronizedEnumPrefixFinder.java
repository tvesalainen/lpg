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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SynchronizedEnumPrefixFinder<T extends Enum<T>> extends EnumPrefixFinder<T>
{

    public SynchronizedEnumPrefixFinder(boolean ignoreCase, T... ens)
    {
        super(ignoreCase, ens);
    }

    public SynchronizedEnumPrefixFinder(Class<T> cls, Regex.Option... options)
    {
        super(cls, options);
    }

    @Override
    public synchronized T find(String text)
    {
        return super.find(text);
    }
    
}
