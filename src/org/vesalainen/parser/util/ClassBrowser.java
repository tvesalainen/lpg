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
package org.vesalainen.parser.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.Typ;

/**
 * @author Timo Vesalainen
 */
public class ClassBrowser
{
    /**
     * Return effective methods for a class. All methods accessible at class 
     * are returned. That includes superclass methods which are not overriden.
     * @param cls
     * @return 
     */
    public static Collection<ExecutableElement> getMethods(TypeElement cls)
    {
        List<ExecutableElement> list = new ArrayList<>();
        while (cls != null)
        {
            for (ExecutableElement method : ElementFilter.methodsIn(cls.getEnclosedElements()))
            {
                if (!overrides(list, method))
                {
                    list.add(method);
                }
            }
            cls = (TypeElement) Typ.asElement(cls.getSuperclass());
        }
        return list;
    }
    private static boolean overrides(Collection<ExecutableElement> methods, ExecutableElement method)
    {
        for (ExecutableElement m : methods)
        {
            if (El.overrides(m, method, (TypeElement)m.getEnclosingElement()))
            {
                return true;
            }
        }
        return false;
    }
}
