/*
 * Copyright (C) 2016 tkv
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
import java.util.List;
import org.vesalainen.lang.Primitives;
import org.vesalainen.util.DoubleStack;

/**
 *
 * @author tkv
 */
public abstract class DoubleMathStack extends DoubleStack implements ExpressionHandler<Class<?>,Method,Field,Class<?>>
{

    @Override
    public void loadVariable(String identifier) throws IOException
    {
        push(getValue(identifier));
    }

    protected abstract double getValue(String identifier) throws IOException;
    
    @Override
    public void number(String number) throws IOException
    {
        push(Primitives.parseDouble(number));
    }

    @Override
    public void setIndex(boolean on)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void loadArray() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void loadArrayItem() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void convertTo(Class<?> aClass) throws IOException
    {
    }

    @Override
    public void convertFrom(Class<?> aClass) throws IOException
    {
    }

    @Override
    public void invoke(Method method) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void loadField(Field field) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void pow(int pow) throws IOException
    {
        for (int ii=1;ii<pow;ii++)
        {
            dup();
        }
        for (int ii=1;ii<pow;ii++)
        {
            mul();
        }
    }

    @Override
    public Method findMethod(String funcName, int args) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<? extends Class<?>> getParameters(Method method)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Class<?> getReturnType(Method method)
    {
        return method.getReturnType();
    }

    @Override
    public Class<?> asType(Field variable)
    {
        return variable.getType();
    }

    @Override
    public Method getMethod(Class<?> cls, String name, Class<?>... parameters) throws IOException
    {
        return findMethod(name, parameters.length);
    }

    @Override
    public Field getField(Class<?> cls, String name)
    {
        try
        {
            return cls.getField(name);
        }
        catch (NoSuchFieldException | SecurityException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean isDegreeArgs(Method method)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDegreeReturn(Method method)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
