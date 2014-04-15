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

package org.vesalainen.grammar;

import org.vesalainen.parser.GenClassFactory;

/**
 * SyntheticBnfParserFactory 
 * @author Timo Vesalainen
 */
public class SyntheticBnfParserFactory implements SyntheticBnfParserIntf
{
    public static final String SyntheticBnfParserClass = "org.vesalainen.grammar.impl.SyntheticBnfParserImpl";
    public static SyntheticBnfParserIntf newInstance()
    {
        SyntheticBnfParserIntf parser = (SyntheticBnfParserIntf) GenClassFactory.loadGenInstance(SyntheticBnfParserClass);
        if (parser == null)
        {
            parser = new SyntheticBnfParserFactory();
        }
        return parser;
    }
    @Override
    public String parse(String text)
    {
        return text;
    }
    
}
