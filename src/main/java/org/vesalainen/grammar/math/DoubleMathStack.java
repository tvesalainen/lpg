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
import java.util.ArrayList;
import java.util.List;
import org.vesalainen.lang.Primitives;
import org.vesalainen.util.DoubleStack;

/**
 *
 * @author tkv
 */
public abstract class DoubleMathStack extends DoubleStack implements ExpressionHandler<Class<?>,String,Field,Class<?>>
{

    @Override
    public void loadVariable(String identifier) throws IOException
    {
        push(getVariable(identifier));
    }

    protected abstract double getVariable(String identifier) throws IOException;
    
    @Override
    public void number(String number) throws IOException
    {
        push(Primitives.parseDouble(number));
    }

    @Override
    public void setIndex(boolean on)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void loadArray() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void loadArrayItem() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public void invoke(String method) throws IOException
    {
        switch (method)
        {
            case "abs":
                abs();
                break;
            case "acos":
                acos();
                break;
            case "add":
                add();
                break;
            case "asin":
                asin();
                break;
            case "atan":
                atan();
                break;
            case "cbrt":
                cbrt();
                break;
            case "ceil":
                ceil();
                break;
            case "cos":
                cos();
                break;
            case "cosh":
                cosh();
                break;
            case "div":
                div();
                break;
            case "dup":
                dup();
                break;
            case "exp":
                exp();
                break;
            case "expm1":
                expm1();
                break;
            case "floor":
                floor();
                break;
            case "log":
                log();
                break;
            case "log10":
                log10();
                break;
            case "log1p":
                log1p();
                break;
            case "mod":
                mod();
                break;
            case "mul":
                mul();
                break;
            case "neg":
                neg();
                break;
            case "sin":
                sin();
                break;
            case "sinh":
                sinh();
                break;
            case "sqrt":
                sqrt();
                break;
            case "tan":
                tan();
                break;
            case "tanh":
                tanh();
                break;
            case "toDegrees":
                toDegrees();
                break;
            case "toRadians":
                toRadians();
                break;
            case "atan2":
                atan2();
                break;
            case "hypot":
                hypot();
                break;
            case "max":
                max();
                break;
            case "min":
                min();
                break;
            case "pow":
                pow();
                break;
            default:
                throw new UnsupportedOperationException(method+" not supported");
        }
    }

    @Override
    public void loadField(Field field) throws IOException
    {
        try
        {
            push(field.getDouble(null));
        }
        catch (IllegalArgumentException | IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }
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
    public String findMethod(String funcName, int args) throws IOException
    {
        switch (funcName)
        {
            case "abs":
            case "acos":
            case "add":
            case "asin":
            case "atan":
            case "cbrt":
            case "ceil":
            case "cos":
            case "cosh":
            case "div":
            case "dup":
            case "exp":
            case "expm1":
            case "floor":
            case "log":
            case "log10":
            case "log1p":
            case "mod":
            case "mul":
            case "neg":
            case "sin":
            case "sinh":
            case "sqrt":
            case "tan":
            case "tanh":
            case "toDegrees":
            case "toRadians":
                if (args != 1)
                {
                    throw new IllegalArgumentException("wrong number of parameter "+args+" in "+funcName);
                }
                return funcName;
            case "atan2":
            case "hypot":
            case "max":
            case "min":
            case "pow":
                if (args != 2)
                {
                    throw new IllegalArgumentException("wrong number of parameter "+args+" in "+funcName);
                }
                return funcName;
            default:
                throw new UnsupportedOperationException(funcName+" not supported");
        }
    }

    @Override
    public List<? extends Class<?>> getParameters(String method)
    {
        List<Class<?>> list = new ArrayList<>();
        switch (method)
        {
            case "abs":
            case "acos":
            case "add":
            case "asin":
            case "atan":
            case "cbrt":
            case "ceil":
            case "cos":
            case "cosh":
            case "div":
            case "dup":
            case "exp":
            case "expm1":
            case "floor":
            case "log":
            case "log10":
            case "log1p":
            case "mod":
            case "mul":
            case "neg":
            case "sin":
            case "sinh":
            case "sqrt":
            case "tan":
            case "tanh":
            case "toDegrees":
            case "toRadians":
                list.add(double.class);
                break;
            case "atan2":
            case "hypot":
            case "max":
            case "min":
            case "pow":
                list.add(double.class);
                list.add(double.class);
                break;
            default:
                throw new UnsupportedOperationException(method+" not supported");
        }
        return list;
    }

    @Override
    public Class<?> getReturnType(String method)
    {
        switch (method)
        {
            case "abs":
            case "acos":
            case "add":
            case "asin":
            case "atan":
            case "atan2":
            case "cbrt":
            case "ceil":
            case "cos":
            case "cosh":
            case "div":
            case "dup":
            case "exp":
            case "expm1":
            case "floor":
            case "hypot":
            case "log":
            case "log10":
            case "log1p":
            case "max":
            case "min":
            case "mod":
            case "mul":
            case "neg":
            case "pow":
            case "sin":
            case "sinh":
            case "sqrt":
            case "tan":
            case "tanh":
            case "toDegrees":
            case "toRadians":
                return double.class;
            default:
                throw new UnsupportedOperationException(method+" not supported");
        }
    }

    @Override
    public Class<?> asType(Class<?> variable)
    {
        return variable;
    }

    @Override
    public String getMethod(Class<?> cls, String name, Class<?>... parameters) throws IOException
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
    public boolean isRadianArgs(String method) throws IOException
    {
        switch (method)
        {
            case "cos":
            case "sin":
            case "tan":
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean isRadianReturn(String method) throws IOException
    {
        switch (method)
        {
            case "acos":
            case "asin":
            case "atan":
                return true;
            default:
                return false;
        }
    }

}
