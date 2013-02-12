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
package org.vesalainen.parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.vesalainen.grammar.Grammar;
import org.vesalainen.parser.annotation.GenClassname;

/**
 * Helper class for getting instances of parser classes. Uses @GenClassname annotation
 * to detect implementation class name.
 * @version $Id$
 * @author tkv
 */
public class ParserFactory
{
    private static Map<Class<?>,Class<?>> map = new HashMap<>();
    
    /**
     * Creates parser class instance either by using ClassLoader or by compiling it dynamically
     * @param cls Annotated class acting also as superclass for created parser
     * @return
     * @throws ParserException 
     */
    public static Object getParserInstance(Class<?> cls) throws ParserException
    {
        Class<?> parserClass = getParserClass(cls);
        try
        {
            return parserClass.newInstance();
        }

        catch (InstantiationException | IllegalAccessException ex)
        {
            throw new ParserException(ex);
        }
    }
    /**
     * Creates parser class either by using ClassLoader or by compiling it dynamically
     * @param cls Annotated class acting also as superclass for created parser
     * @return
     * @throws ParserException 
     */
    public static Class<?> getParserClass(Class<?> cls) throws ParserException
    {
        try
        {
            return loadParserClass(cls);
        }
        catch (ClassNotFoundException ex)
        {
            try
            {
                ParserCompiler pc = null;
                pc = new ParserCompiler(cls);
                pc.compile();
                return pc.loadDynamic();
            }
            catch (ReflectiveOperationException | IOException ex1)
            {
                throw new ParserException(ex1);
            }
        }
    }
    /**
     * Creates parser class instance by using ClassLoader. Return null if unable to
     * load class
     * @param cls Annotated class acting also as superclass for created parser
     * @return
     * @throws ParserException 
     */
    public static Object loadParserInstance(Class<?> cls) throws ParserException
    {
        try
        {
            Class<?> parserClass;
            try
            {
                parserClass = loadParserClass(cls);
            }
            catch (ClassNotFoundException ex)
            {
                return null;
            }
            return parserClass.newInstance();
        }
        catch (InstantiationException | IllegalAccessException ex)
        {
            throw new ParserException(ex);
        }
    }
    /**
     * Creates parser class by using ClassLoader. Return null if unable to
     * load class
     * @param cls Annotated class acting also as superclass for created parser
     * @return
     * @throws ClassNotFoundException 
     */
    public static Class<?> loadParserClass(Class<?> cls) throws ClassNotFoundException
    {
        Class<?> parserClass = map.get(cls);
        if (parserClass == null)
        {
            GenClassname genClassname = cls.getAnnotation(GenClassname.class);
            if (genClassname == null)
            {
                throw new IllegalArgumentException("@GenClassname not set in "+cls);
            }
            parserClass = Class.forName(genClassname.value());
            map.put(cls, parserClass);
        }
        return parserClass;
    }
}
