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
import org.vesalainen.bcc.MethodCompiler;

/**
 * @author Timo Vesalainen
 */
public class PrimitiveExpressionHandler extends MethodExpressionHandler
{

    public PrimitiveExpressionHandler(MethodCompiler mc, Class<? extends Number> type, MethodExpressionHandlerFactory factory)
    {
        super(mc, type, factory);
        switch (type.getName())
        {
            case "byte":
            case "char":
            case "short":
                type = int.class;
                break;
            case "int":
            case "long":
            case "float":
            case "double":
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
        mc.get(field);
    }

    @Override
    public void abs() throws IOException
    {
        invoke("abs", 1);
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
    public void arrayIndex() throws IOException
    {
        mc.taload(safeType);
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

}
