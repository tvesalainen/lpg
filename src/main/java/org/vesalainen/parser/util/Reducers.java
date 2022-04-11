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
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.Typ;

/**
 * @author Timo Vesalainen
 */
public class Reducers
{
    public static <T> List<T> listStart()
    {
        return new ArrayList<>();
    }
    
    public static <T> List<T> listStart(T t)
    {
        List<T> list = new ArrayList<>();
        list.add(t);
        return list;
    }
    
    public static <T> List<T> listNext(List<T> list, T t)
    {
        list.add(t);
        return list;
    }
    
    public static <T> T get()
    {
        return null;
    }
    
    public static <T> T get(T t)
    {
        return t;
    }

    public static boolean isGet(ExecutableElement member)
    {
        TypeElement te = El.getTypeElement(Reducers.class.getCanonicalName());
        return (
                Typ.isSameType(member.getEnclosingElement().asType(), te.asType()) &&
                "get".contentEquals(member.getSimpleName())
                );
    }
}
