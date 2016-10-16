/*
 * Copyright (C) 2013 Timo Vesalainen
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
import java.util.List;
import org.vesalainen.math.Arithmetic;

/**
 * @author Timo Vesalainen
 * @param <T> Type
 * @param <M> Method
 * @param <F> Field
 * @param <P> Parameter
 */
public interface ExpressionHandler<T,M,F,P> extends Arithmetic
{
    void loadVariable(String identifier) throws IOException;

    void number(String number) throws IOException;

    void setIndex(boolean on);

    void loadArray() throws IOException;

    void loadArrayItem() throws IOException;

    void convertTo(T aClass) throws IOException;

    void convertFrom(T aClass) throws IOException;

    void invoke(M method) throws IOException;
    
    void loadField(F field) throws IOException;

    void pow(int i) throws IOException;

    M findMethod(String funcName, int args) throws IOException;
    
    List<? extends P> getParameters(M method) throws IOException;
    
    T getReturnType(M method) throws IOException;
    
    T asType(F variable) throws IOException;
    
    M getMethod(Class<?> cls, String name, Class<?>... parameters) throws IOException;
    
    F getField(Class<?> cls, String name) throws IOException;
    
    boolean isDegreeArgs(M method) throws IOException;
    
    boolean isDegreeReturn(M method) throws IOException;
}
