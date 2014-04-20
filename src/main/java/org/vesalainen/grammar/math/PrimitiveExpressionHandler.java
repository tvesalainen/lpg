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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.MethodCompiler;
import org.vesalainen.bcc.model.Typ;

/**
 * @author Timo Vesalainen
 */
public class PrimitiveExpressionHandler extends MethodExpressionHandler
{
    private boolean category2;
    public PrimitiveExpressionHandler(ExecutableElement method, MethodCompiler mc, TypeMirror type)
    {
        super(method, mc, type);
        switch (type.getKind())
        {
            case BYTE:
            case CHAR:
            case SHORT:
                type = Typ.Int;
                break;
            case INT:
            case FLOAT:
                break;
            case LONG:
            case DOUBLE:
                category2 = true;
                break;
            default:
                throw new UnsupportedOperationException(type+" not supported.");
        }
    }

    @Override
    public void invoke(ExecutableElement method) throws IOException
    {
        mc.invoke(method);
    }

    @Override
    public void loadLocalVariable(String name) throws IOException
    {
        mc.tload(name);
    }

    @Override
    public void loadField(VariableElement field) throws IOException
    {
        if (field.getModifiers().contains(Modifier.STATIC))
        {
            mc.getStaticField(field);
        }
        else
        {
            mc.aload(0);    // this
            mc.getField(field);
        }
        convertFrom(field.asType());
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
        switch (type.getKind())
        {
            case INT:
                mc.tconst(Integer.parseInt(number));
                break;
            case LONG:
                mc.tconst(Long.parseLong(number));
                break;
            case FLOAT:
                mc.tconst(Float.parseFloat(number));
                break;
            case DOUBLE:
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
    public void convertTo(TypeMirror to) throws IOException
    {
        mc.convert(type, to);
    }

    @Override
    public void convertFrom(TypeMirror from) throws IOException
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
