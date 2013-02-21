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

import java.lang.reflect.Method;
import org.vesalainen.bcc.MethodCompiler;

/**
 * @author Timo Vesalainen
 */
public class MethodExpressionHandlerFactory
{
    public static MethodExpressionHandler getInstance(Method method, MethodCompiler methodCompiler)
    {
        Class<?> rt = method.getReturnType();
        Class<? extends Number> type = (Class<? extends Number>) rt;
        if (type.isPrimitive())
        {
            return new PrimitiveExpressionHandler(method, methodCompiler, type);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
