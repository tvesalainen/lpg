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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.MethodCompiler;
import org.vesalainen.bcc.model.Typ;
import org.vesalainen.util.NoNeedToContinueException;

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
    public void invoke(ExecutableElement method) throws Exception
    {
        mc.invoke(method);
    }

    @Override
    public void loadLocalVariable(String name) throws Exception
    {
        mc.tload(name);
    }

    @Override
    public void loadField(VariableElement field) throws Exception
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
    public void add() throws Exception
    {
        mc.tadd(type);
    }

    @Override
    public void subtract() throws Exception
    {
        mc.tsub(type);
    }

    @Override
    public void mul() throws Exception
    {
        mc.tmul(type);
    }

    @Override
    public void div() throws Exception
    {
        mc.tdiv(type);
    }

    @Override
    public void mod() throws Exception
    {
        mc.trem(type);
    }

    @Override
    public void neg() throws Exception
    {
        mc.tneg(type);
    }

    @Override
    public void number(String number) throws Exception
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
    public void loadArray() throws Exception
    {
        mc.aaload();
    }

    @Override
    public void loadArrayItem() throws Exception
    {
        mc.taload(type);
    }

    @Override
    public void dup() throws Exception
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
    public void convertTo(TypeMirror to) throws Exception
    {
        mc.convert(type, to);
    }

    @Override
    public void convertFrom(TypeMirror from) throws Exception
    {
        mc.convert(from, type);
    }

    @Override
    public void pow(int pow) throws Exception
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

    @Override
    public void eq() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void ne() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void lt() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void le() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void gt() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void ge() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void not() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void and() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void or() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void checkAnd() throws NoNeedToContinueException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void checkOr() throws NoNeedToContinueException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
