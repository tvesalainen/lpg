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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.vesalainen.bcc.MethodCompiler;

/**
 * @author Timo Vesalainen
 */
public abstract class MethodExpressionHandler implements ExpressionHandler
{
    protected Method method;
    protected MethodCompiler mc;
    protected Class<? extends Number> type;
    private Class<? extends Number> safe;

    private enum VarType {LocalVariable, Field, Getter, Enum };
    protected Class<?> variableType;
    protected Class<?> componentType;
    private Map<Class<?>,Integer> ranking = new HashMap<>();

    public MethodExpressionHandler(Method method, MethodCompiler methodCompiler, Class<? extends Number> type)
    {
        this.method = method;
        this.mc = methodCompiler;
        this.type = type;
        
        ranking.put(int.class, 1);
        ranking.put(long.class, 2);
        ranking.put(float.class, 3);
        ranking.put(double.class, 4);
    }
    
    public Method findMethod(String funcName, int args) throws IOException
    {
        Class<?>[] params = new Class<?>[args];
        Class<?> type = getType();
        Arrays.fill(params, type);
        List<Class<?>> classList = new ArrayList<>();
        Method result = null;
        int match = Integer.MAX_VALUE;
        Class<?> cls = method.getDeclaringClass();
        while (cls != null)
        {
            classList.add(cls);
            cls = cls.getSuperclass();
        }
        classList.add(Math.class);
        classList.add(MoreMath.class);
        MethodIterator mi = new MethodIterator(funcName, args, classList);
        while (mi.hasNext())
        {
            Method mtd = mi.next();
            int ma = getMatch(mtd);
            if (ma == 0)
            {
                return mtd;
            }
            if (ma < match)
            {
                result = mtd;
                match = ma;
            }
        }
        return result;
    }

    private int getMatch(Method method)
    {
        int res = getMatch(method.getReturnType());
        if (res < 0)
        {
            return res;
        }
        for (Class<?> t : method.getParameterTypes())
        {
            int m = getMatch(t);
            if (m < 0)
            {
                return m;
            }
            res += m;
        }
        return res;
    }
    private int getMatch(Class<?> t)
    {
        return ranking.get(t) - ranking.get(type);
    }
    public Class<? extends Number> getType()
    {
        return type;
    }

    /**
     * Set index parsing mode. In true mode we are parsing inside bracket where 
     * index type must be int.
     * @param b 
     */
    @Override
    public void setIndex(boolean b)
    {
        if (b)
        {
            assert safe == null;
            safe = type;
            type = int.class;
        }
        else
        {
            assert type == int.class;
            assert safe != null;
            type = safe;
            safe = null;
        }
    }

    @Override
    public void loadVariable(String identifier) throws IOException
    {
        if (Modifier.isAbstract(method.getModifiers()))
        {
            throw new IllegalArgumentException("abstract method "+method+" doesn't have argument names. Create a empty body for function");
        }
        VarType varType = null;
        if (mc.hasLocalVariable(identifier))
        {
            loadLocalVariable(identifier);
            variableType = (Class<?>) mc.getLocalType(identifier);
            varType = VarType.LocalVariable;
        }
        Class<?> cls = method.getDeclaringClass();
        try
        {
            Field field = cls.getDeclaredField(identifier);
            if (varType != null)
            {
                throw new IllegalArgumentException(identifier+" is ambiguous. Field and "+varType+" matches");
            }
            variableType = field.getType();
            loadField(field);
            varType = VarType.Field;
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
            if (varType != null)
            {
                throw new IllegalArgumentException(identifier+" is ambiguous. Getter and "+varType+" matches");
            }
            variableType = getMethod.getReturnType();
            invoke(getMethod);
            varType = VarType.Getter;
        }
        catch (NoSuchMethodException | SecurityException ex)
        {
        }
        int ordinal = findEnum(cls, identifier);
        if (ordinal >= 0)
        {
            if (varType != null)
            {
                throw new IllegalArgumentException(identifier+" is ambiguous. Enum and "+varType+" matches");
            }
            variableType = int.class;
            number(String.valueOf(ordinal));
            varType = VarType.Enum;
        }
        componentType = variableType.getComponentType();
        if (varType == null)
        {
            throw new IllegalArgumentException("argument "+identifier+" not found");
        }
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

    private class MethodIterator implements Iterator<Method> 
    {
        private String name;
        private int argCount;
        private Iterator<Class<?>> iterator;
        private Method[] methods;
        private int index;
        private Method next;

        public MethodIterator(String name, int argCount, List<Class<?>> list)
        {
            this.name = name;
            this.argCount = argCount;
            this.iterator = list.iterator();
            next = getNext();
        }

        @Override
        public boolean hasNext()
        {
            return next != null;
        }

        @Override
        public Method next()
        {
            Method res = next;
            next = getNext();
            return res;
        }
        
        private Method getNext()
        {
            Method m = getNext2();
            while (m != null)
            {
                if (
                        name.equals(m.getName()) &&
                        m.getParameterTypes().length == argCount
                        )
                {
                    return m;
                }
                m = getNext2();
            }
            return null;
        }
        private Method getNext2()
        {
            while ((methods != null && index < methods.length) || iterator.hasNext())
            {
                if (methods != null && index < methods.length)
                {
                    return methods[index++];
                }
                else
                {
                    Class<?> cls = iterator.next();
                    methods = cls.getMethods();
                    index = 0;
                }
            }
            return null;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
