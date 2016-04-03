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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.bcc.model.El;
import org.vesalainen.parser.annotation.GenClassname;

/**
 * Helper class for getting instances of parser classes. Uses @GenClassname annotation
 * to detect implementation class name.
 * @version $Id$
 * @author tkv
 */
public class GenClassFactory
{
    private static Map<Class<?>,Class<?>> map = new HashMap<>();
    
    /**
     * Creates generated class instance either by using ClassLoader or by compiling it dynamically
     * @param cls Annotated class acting also as superclass for created parser
     * @param args
     * @return
     * @throws ParserException 
     */
    public static Object getGenInstance(Class<?> cls, Object... args) throws ParserException
    {
        Class<?> parserClass = getGenClass(cls);
        try
        {
            if (args.length == 0)
            {
                return parserClass.newInstance();
            }
            else
            {
                Class<?>[] types = new Class<?>[args.length];
                int index = 0;
                for (Object o : args)
                {
                    types[index++] = o.getClass();
                }
                Constructor<?> constructor = parserClass.getConstructor(types);
                return constructor.newInstance(args);
            }
        }

        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex)
        {
            throw new ParserException(ex);
        }
    }
    /**
     * Creates generated class either by using ClassLoader
     * @param cls Annotated class acting also as superclass for created parser
     * @return
     * @throws ParserException When implementation class is not compiled 
     */
    public static Class<?> getGenClass(Class<?> cls) throws ParserException
    {
        GenClassname genClassname = cls.getAnnotation(GenClassname.class);
        if (genClassname == null)
        {
            throw new IllegalArgumentException("@GenClassname not set in "+cls);
        }
        try
        {
            return loadGenClass(cls);
        }
        catch (ClassNotFoundException ex)
        {
            throw new ParserException(cls+" classes implementation class not compiled.\n"+
                    "Possible problem with annotation processor.\n"+
                    "Try building the whole project and check that implementation class "+genClassname.value()+" exist!");
            /*
            try
            {
                GenClassCompiler pc = GenClassCompiler.compile(El.getTypeElement(cls.getCanonicalName()), null);
                return pc.loadDynamic();
            }
            catch (IOException ex1)
            {
                throw new ParserException(ex1);
            }
            */
        }
    }
    /**
     * Creates instance of implementation class and loads it dynamic. This method
     * is used in testing.
     * @param cls
     * @return
     * @throws IOException 
     */
    public static Object createDynamicInstance(Class<?> cls) throws IOException
    {
        GenClassCompiler pc = GenClassCompiler.compile(El.getTypeElement(cls.getCanonicalName()), null);
        return pc.loadDynamic();
    }
    /**
     * Loads generated class instance by using ClassLoader. Return null if unable to
     * load class
     * @param cls Annotated class acting also as superclass for created parser
     * @return
     * @throws ParserException 
     */
    public static Object loadGenInstance(String classname) throws ParserException
    {
        try
        {
            Class<?> parserClass;
            try
            {
                parserClass = Class.forName(classname);
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
     * Loads generated class instance by using ClassLoader. Return null if unable to
     * load class
     * @param cls Annotated class acting also as superclass for created parser
     * @return
     * @throws ParserException 
     */
    public static Object loadGenInstance(Class<?> cls) throws ParserException
    {
        try
        {
            Class<?> parserClass;
            try
            {
                parserClass = loadGenClass(cls);
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
     * Creates generated class by using ClassLoader. Return null if unable to
     * load class
     * @param cls Annotated class acting also as superclass for created parser
     * @return
     * @throws ClassNotFoundException 
     */
    public static Class<?> loadGenClass(Class<?> cls) throws ClassNotFoundException
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
