/*
 * Copyright (C) 2013 Timo Vesalainen
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.vesalainen.bcc.ClassCompiler;
import org.vesalainen.bcc.FieldInitializer;
import org.vesalainen.bcc.MethodCompiler;
import org.vesalainen.bcc.SubClass;
import org.vesalainen.bcc.type.ClassWrapper;
import org.vesalainen.bcc.type.Generics;
import org.vesalainen.grammar.math.DEH;
import org.vesalainen.grammar.math.MathExpressionParser;
import org.vesalainen.grammar.math.MethodExpressionHandler;
import org.vesalainen.grammar.math.MethodExpressionHandlerFactory;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GenRegex;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.MapDef;
import org.vesalainen.parser.annotation.MathExpression;
import org.vesalainen.regex.Regex;

/**
 * @author Timo Vesalainen
 */
public class GenClassCompiler  implements ClassCompiler, ParserConstants
{
    protected Type thisClass;
    protected Class<?> superClass;
    protected SubClass subClass;
    protected File classDir;
    protected File srcDir;
    protected List<RegexWrapper> regexList;
    /**
     * Creates a parser using annotations in parserClass.
     * @param superClass
     * @param fullyQualifiedname
     * @throws NoSuchMethodException
     * @throws IOException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     */
    public GenClassCompiler(Class<?> superClass) throws IOException, ReflectiveOperationException
    {
        this(ClassWrapper.wrap(getThisClassname(superClass), superClass));
    }
    /**
     * Creates a parser using grammar.
     * @param superClass Super class for parser. Possible parser annotations
     * are not processed.
     * @param fullyQualifiedname  Parser class name .
     * @param grammar
     * @throws NoSuchMethodException
     * @throws IOException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException 
     */
    public GenClassCompiler(ClassWrapper thisClass) throws IOException, ReflectiveOperationException
    {
        this.superClass = (Class<?>) thisClass.getSuperclass();
        this.thisClass = thisClass;
        this.subClass = new SubClass(thisClass);
    }
    public static GenClassCompiler compile(Class<?> superClass, File sourceDir, File classesDir) throws IOException, ReflectiveOperationException
    {
        GenClassCompiler compiler;
        if (superClass.isAnnotationPresent(GrammarDef.class))
        {
            compiler = new ParserCompiler(superClass);
        }
        else
        {
            if (superClass.isAnnotationPresent(MapDef.class))
            {
                compiler = new MapCompiler(superClass);
            }
            else
            {
                compiler = new GenClassCompiler(superClass);
            }
        }
        compiler.setClassDir(classesDir);
        compiler.setSrcDir(sourceDir);
        compiler.compile();
        if (classesDir == null)
        {
            System.err.println("warning! classes directory not set");
        }
        else
        {
            compiler.saveClass();
        }
        return compiler;
    }
    /**
     * Handles initializer, constructor and other common annotation compilations.
     * <p>Common handled annotations are: @GenRegex
     * @throws IOException
     * @throws ReflectiveOperationException 
     */
    @Override
    public void compile() throws IOException, ReflectiveOperationException
    {
        compileInitializers();
        compileConstructors();
        Class<?> clazz = superClass;
        while (clazz != null)
        {
            compile(clazz);
            clazz = clazz.getSuperclass();
        }

    }

    private void compile(Class<?> clazz) throws IOException, ReflectiveOperationException
    {
        for (Method method : clazz.getDeclaredMethods())
        {
            if (method.isAnnotationPresent(MathExpression.class))
            {
                compileMathExpression(method);
            }
        }
    }
    private void compileMathExpression(Method method) throws IOException, ReflectiveOperationException
    {
        Class<?> returnType = method.getReturnType();
        if (!(
                returnType.isInstance(Number.class) ||
                (returnType.isPrimitive() && !boolean.class.equals(returnType))
                ))
        {
            throw new IllegalArgumentException(method+" return type is not number");
        }
        for (Class<?> param : method.getParameterTypes())
        {
            if (!param.isArray())
            {
                if (!returnType.equals(param))
                {
                    throw new IllegalArgumentException(method+" parameter type not the same as return type");
                }
            }
        }
        Class<? extends Number> nType = (Class<? extends Number>) returnType;
        MethodCompiler mc = subClass.override(Modifier.PUBLIC, method);
        MethodExpressionHandler handler = MethodExpressionHandlerFactory.getInstance(method, mc);
        MathExpressionParser parser = (MathExpressionParser) GenClassFactory.getGenInstance(MathExpressionParser.class);
        MathExpression me = method.getAnnotation(MathExpression.class);
        DEH expression = parser.parse(me.value(), handler);
        expression.execute(handler);
        mc.treturn(nType);
        mc.end();
    }

    public void compileInitializers() throws IOException
    {
        subClass.codeStaticInitializer(resolvStaticInitializers(superClass));
    }
    public void compileConstructors() throws IOException
    {
        subClass.codeDefaultConstructor(resolvInitializers(superClass));
    }
    private static String getThisClassname(Class<?> parserClass)
    {
        GenClassname genClassname = parserClass.getAnnotation(GenClassname.class);
        if (genClassname == null)
        {
            throw new IllegalArgumentException("@GenClassname missing from "+parserClass);
        }
        return genClassname.value();
    }

    @Override
    public void setClassDir(File classDir)
    {
        this.classDir = classDir;
    }

    @Override
    public void setSrcDir(File srcDir)
    {
        this.srcDir = srcDir;
    }

    public File getClassDir()
    {
        return classDir;
    }

    public File getSrcDir()
    {
        return srcDir;
    }
    /**
     * Saves Parser class in java classfile format in dir. File path is defined by
     * dir and classname. Example dir = c:\temp class is foo.bar.Main file path is
     * c:\temp\foo\bar\Main.class
     * if srcDir is not null, creates a byte code source file to dir. File content is similar to the
     * output of javap utility. Your IDE might be able to use this file for debugging
     * the actual byte code. (NetBeans can if this file located like java source
     * files)
     *
     * Example dir = c:\src class is foo.bar.Main Source file path is
     * c:\src\foo\bar\Main.jasm
     * @throws IOException
     */
    @Override
    public void saveClass() throws IOException
    {
        if (srcDir != null)
        {
            subClass.createSourceFile(srcDir);
        }
        subClass.save(classDir);
        if (regexList != null)
        {
            for (RegexWrapper rw : regexList)
            {
                try
                {
                    Regex.saveAs(rw.getExpression(), classDir, srcDir, rw.getClassname(), rw.getOptions());
                }
                catch (Exception ex)
                {
                    throw new IOException(ex);
                }
            }
        }
    }
    SubClass getSubClass()
    {
        return subClass;
    }

    Type getThisClass()
    {
        return thisClass;
    }

    /**
     * Compile the generated class dynamically. Nice method for experimenting and testing.
     * @return
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Class<?> loadDynamic()
    {
        try
        {
            return subClass.load();
        }
        catch (IOException ex)
        {
            throw new ParserException(ex);
        }
    }

    public Object newInstance()
    {
        try
        {
            Class<?> c = subClass.load();
            return c.newInstance();
        }
        catch (IOException | InstantiationException | IllegalAccessException ex)
        {
            throw new ParserException(ex);
        }
    }
    protected FieldInitializer[] resolvInitializers(Class<?> parserClass)
    {
        List<FieldInitializer> list = new ArrayList<>();
        Class<?> clazz = parserClass;
        while (clazz != null)
        {
            for (Field f : clazz.getDeclaredFields())
            {
                if (!Modifier.isStatic(f.getModifiers()))
                {
                    if (f.isAnnotationPresent(GenRegex.class))
                    {
                        list.add(createRegex(f));
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return list.toArray(new FieldInitializer[list.size()]);
    }
    protected FieldInitializer[] resolvStaticInitializers(Class<?> parserClass)
    {
        List<FieldInitializer> list = new ArrayList<>();
        Class<?> clazz = parserClass;
        while (clazz != null)
        {
            for (Field f : clazz.getDeclaredFields())
            {
                if (Modifier.isStatic(f.getModifiers()))
                {
                    if (f.isAnnotationPresent(GenRegex.class))
                    {
                        list.add(createRegex(f));
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return list.toArray(new FieldInitializer[list.size()]);
    }
    protected FieldInitializer createRegex(Field f)
    {
        if (!Regex.class.isAssignableFrom(f.getType()))
        {
            throw new IllegalArgumentException(f+" cannot be initialized with Regex subclass");
        }
        if (regexList == null)
        {
            regexList = new ArrayList<>();
        }
        GenRegex ra = f.getAnnotation(GenRegex.class);
        String cn = Generics.getFullyQualifiedForm(thisClass)+"Regex"+regexList.size();
        ClassWrapper regexImpl = ClassWrapper.wrap(cn, Regex.class);
        regexList.add(new RegexWrapper(ra.value(), cn, ra.options()));
        return FieldInitializer.getObjectInstance(f, regexImpl);
    }

    private static class RegexWrapper
    {
        private String expression;
        private String classname;
        private Regex.Option[] options;

        public RegexWrapper(String expression, String classname, Regex.Option... options)
        {
            this.expression = expression;
            this.classname = classname;
            this.options = options;
        }

        public String getClassname()
        {
            return classname;
        }

        public String getExpression()
        {
            return expression;
        }

        public Regex.Option[] getOptions()
        {
            return options;
        }

    }
}
