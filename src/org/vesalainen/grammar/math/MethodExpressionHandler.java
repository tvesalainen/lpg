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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.vesalainen.bcc.MethodCompiler;

/**
 * @author Timo Vesalainen
 */
public abstract class MethodExpressionHandler extends ExpressionHandler<MethodExpressionHandlerFactory>
{
    protected MethodCompiler mc;
    protected Class<? extends Number> safeType;
    protected int arrayIndexLevel;
    
    public MethodExpressionHandler(MethodCompiler mc, Class<? extends Number> type, MethodExpressionHandlerFactory factory)
    {
        super(type, factory);
        this.mc = mc;
    }

    @Override
    public void loadVariable(String identifier) throws IOException
    {
        Method method = factory.getMethod();
        if (Modifier.isAbstract(method.getModifiers()))
        {
            throw new IllegalArgumentException("abstract method "+method+" doesn't have argument names. Create a empty body for function");
        }
        MethodCompiler mc = factory.getMethodCompiler();
        if (mc.hasLocalVariable(identifier))
        {
            loadLocalVariable(identifier);
            return;
        }
        Class<?> cls = method.getDeclaringClass();
        try
        {
            Field field = cls.getDeclaredField(identifier);
            loadField(field);
            return;
        }
        catch (NoSuchFieldException ex)
        {
        }
        catch (SecurityException ex)
        {
        }
        String getter = "get"+identifier.substring(0, 1).toUpperCase()+identifier.substring(1);
        try
        {
            Method getMethod = cls.getMethod(getter);
            invoke(getMethod);
            return;
        }
        catch (NoSuchMethodException | SecurityException ex)
        {
        }
        int ordinal = findEnum(cls, identifier);
        if (ordinal >= 0)
        {
            number(String.valueOf(ordinal));
            return;
        }
        throw new IllegalArgumentException("argument "+identifier+" not found");
    }

    private int findEnum(Class<?> cls, String name)
    {
        Class<?> c = cls;
        while (c != null)
        {
            for (Class<?> cc : c.getDeclaredClasses())
            {
                if (cc.isEnum())
                {
                    for (Enum enumConstant : (Enum[]) cc.getEnumConstants())
                    {
                        if (name.equals(enumConstant.name()))
                        {
                            return enumConstant.ordinal();
                        }
                    }
                }
            }
            c = c.getSuperclass();
        }
        return -1; 
    }
    @Override
    public void invoke(String funcName, int stack) throws IOException
    {
        Class<?>[] params = new Class<?>[stack];
        Class<?> type = getType();
        Arrays.fill(params, type);
        Method method = null;
        Class<?> cls = factory.getMethod().getDeclaringClass();
        while (cls != null)
        {
            try
            {
                method = cls.getMethod(funcName, params);
                break;
            }
            catch (NoSuchMethodException ex)
            {
            }
            catch (SecurityException ex)
            {
                throw new IllegalArgumentException(ex);
            }
            cls = cls.getSuperclass();
        }
        if (method == null)
        {
            try
            {
                method = Math.class.getMethod(funcName, params);
            }
            catch (NoSuchMethodException ex)
            {
            }
            catch (SecurityException ex)
            {
            }
        }
        if (method == null)
        {
            try
            {
                method = MoreMath.class.getMethod(funcName, params);
            }
            catch (NoSuchMethodException ex)
            {
                throw new IllegalArgumentException(funcName+" method not found");
            }
            catch (SecurityException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
        if (!Modifier.isStatic(method.getModifiers()))
        {
            throw new IllegalArgumentException(funcName+" method is not static");
        }
        if (!type.equals(method.getReturnType()))
        {
            throw new IllegalArgumentException(funcName+" methods return type not "+type);
        }
        invoke(method);
    }

    public void invoke(Method method) throws IOException
    {
        mc.invoke(method);
    }

    public void loadLocalVariable(String name) throws IOException
    {
        mc.tload(name);
    }

    public void loadField(Field field) throws IOException
    {
        mc.get(field);
    }

    @Override
    public void arrayIndexMode(boolean on)
    {
        if (on)
        {
            arrayIndexLevel++;
        }
        else
        {
            arrayIndexLevel--;
            assert arrayIndexLevel >= 0;
        }
        if (on && arrayIndexLevel == 1)
        {
            assert safeType == null;
            safeType = type;
            type = int.class;
        }
        if (!on && arrayIndexLevel == 0)
        {
            assert safeType != null;
            type = safeType;
            safeType = null;
        }
    }

}
