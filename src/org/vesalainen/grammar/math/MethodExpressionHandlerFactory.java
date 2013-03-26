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
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.MethodCompiler;
import org.vesalainen.bcc.model.Typ;

/**
 * @author Timo Vesalainen
 */
public class MethodExpressionHandlerFactory
{
    public static MethodExpressionHandler getInstance(ExecutableElement method, MethodCompiler methodCompiler)
    {
        if (Typ.isPrimitive(method.getReturnType()))
        {
            return new PrimitiveExpressionHandler(method, methodCompiler, method.getReturnType());
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
