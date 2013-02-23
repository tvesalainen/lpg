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

package org.vesalainen.grammar.math;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.vesalainen.bcc.MethodCompiler;
import org.vesalainen.bcc.SubClass;
import org.vesalainen.bcc.type.ClassWrapper;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.MathExpression;

/**
 * @author Timo Vesalainen
 */
public class MathCompiler 
{
    private final Class<?> superClass;
    private final SubClass subClass;

    public MathCompiler(Class<?> superClass) throws IOException, ReflectiveOperationException
    {
        this(ClassWrapper.wrap(getThisClassname(superClass), superClass));
    }
    public MathCompiler(ClassWrapper thisClass) throws IOException, ReflectiveOperationException
    {
        this.superClass = (Class<?>) thisClass.getSuperclass();
        subClass = new SubClass(thisClass);
        subClass.codeDefaultConstructor();
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

    public void compile() throws IOException, ReflectiveOperationException
    {
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
                parser.parse(me, handler);
                mc.treturn(nType);
                mc.end();
            }
        }
        subClass.save(new File("C:\\Users\\tkv\\Documents\\NetBeansProjects\\LPG\\build\\classes"));
    }
    public Object newInstance() throws IOException
    {
        return subClass.newInstance();
    }
}
