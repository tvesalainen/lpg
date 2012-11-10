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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.vesalainen.bcc.ClassFile;
import org.vesalainen.bcc.MethodCompiler;
import org.vesalainen.bcc.SubClass;

/**
 * @author Timo Vesalainen
 */
public class MethodExpressionHandlerFactory implements ExpressionHandlerFactory
{
    private Method method;
    private MethodCompiler methodCompiler;

    public MethodExpressionHandlerFactory(Method method, MethodCompiler methodCompiler)
    {
        this.method = method;
        this.methodCompiler = methodCompiler;
    }

    @Override
    public ExpressionHandler getInstance(Class<? extends Number> type)
    {
        if (type.isPrimitive())
        {
            return new PrimitiveExpressionHandler(methodCompiler, type, this);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Method getMethod()
    {
        return method;
    }

    public MethodCompiler getMethodCompiler()
    {
        return methodCompiler;
    }

}
