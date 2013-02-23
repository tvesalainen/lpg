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
import org.vesalainen.bcc.MethodCompiler;

/**
 * @author Timo Vesalainen
 */
public class PrimitiveExpressionHandler extends MethodExpressionHandler
{
    private boolean category2;
    public PrimitiveExpressionHandler(Method method, MethodCompiler mc, Class<? extends Number> type)
    {
        super(method, mc, type);
        switch (type.getName())
        {
            case "byte":
            case "char":
            case "short":
                type = int.class;
                break;
            case "int":
            case "float":
                break;
            case "long":
            case "double":
                category2 = true;
                break;
            default:
                throw new UnsupportedOperationException(type+" not supported.");
        }
    }

    @Override
    public void invoke(Method method) throws IOException
    {
        mc.invoke(method);
    }

    @Override
    public void loadLocalVariable(String name) throws IOException
    {
        mc.tload(name);
    }

    @Override
    public void loadField(Field field) throws IOException
    {
        if (!Modifier.isStatic(field.getModifiers()))
        {
            mc.aload(0);    // this
        }
        mc.get(field);
        convertFrom(field.getType());
    }

    @Override
    public void add() throws IOException
    {
        mc.tadd(type);
    }

    @Override
    public void subtract() throws IOException
    {
        mc.tsub(type);
    }

    @Override
    public void mul() throws IOException
    {
        mc.tmul(type);
    }

    @Override
    public void div() throws IOException
    {
        mc.tdiv(type);
    }

    @Override
    public void mod() throws IOException
    {
        mc.trem(type);
    }

    @Override
    public void neg() throws IOException
    {
        mc.tneg(type);
    }

    @Override
    public void number(String number) throws IOException
    {
        switch (type.getName())
        {
            case "int":
                mc.tconst(Integer.parseInt(number));
                break;
            case "long":
                mc.tconst(Long.parseLong(number));
                break;
            case "float":
                mc.tconst(Float.parseFloat(number));
                break;
            case "double":
                mc.tconst(Double.parseDouble(number));
                break;
        }
    }

    @Override
    public void loadArray() throws IOException
    {
        mc.aaload();
    }

    @Override
    public void loadArrayItem() throws IOException
    {
        mc.taload(type);
    }

    @Override
    public void dup() throws IOException
    {
        if (category2)
        {
            mc.dup2();
        }
        else
        {
            mc.dup();
        }
    }

    @Override
    public void convertTo(Class<?> to) throws IOException
    {
        mc.convert(type, to);
    }

    @Override
    public void convertFrom(Class<?> from) throws IOException
    {
        mc.convert(from, type);
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
            mc.tmul(type);
        }
    }

}
