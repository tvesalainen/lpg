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
    /**
     * Load variable value and push to stack
     * @param identifier
     * @throws IOException 
     */
    void loadVariable(String identifier) throws IOException;
    /**
     * Push literal number to stack.
     * @param number
     * @throws IOException 
     */
    void number(String number) throws IOException;
    /**
     * Set index parsing mode. In true mode we are parsing inside bracket where 
     * index type must be int.
     * @param on 
     */
    void setIndex(boolean on);

    void loadArray() throws IOException;

    void loadArrayItem() throws IOException;
    /**
     * Convert this handlers type to given type.
     * @param type
     * @throws IOException 
     */
    void convertTo(T type) throws IOException;
    /**
     * Convert given type to this handlers type.
     * @param type
     * @throws IOException 
     */
    void convertFrom(T type) throws IOException;
    /**
     * Invoke given method.
     * @param method
     * @throws IOException 
     */
    void invoke(M method) throws IOException;
    /**
     * Push fields current value to stack.
     * @param field
     * @throws IOException 
     */
    void loadField(F field) throws IOException;
    /**
     * Generates code that calculates power with integer argument rather that
     * using pow() function.
     * @param i
     * @throws IOException 
     * @see java.lang.Math#pow(double, double) 
     */
    void pow(int i) throws IOException;
    /**
     * Returns method with given name and with given number of arguments.
     * @param funcName
     * @param args
     * @return
     * @throws IOException 
     */
    M findMethod(String funcName, int args) throws IOException;
    /**
     * Returns list of parameters for given method.
     * @param method
     * @return
     * @throws IOException 
     */
    List<? extends P> getParameters(M method) throws IOException;
    /**
     * Returns the return type of given method.
     * @param method
     * @return
     * @throws IOException 
     */
    T getReturnType(M method) throws IOException;
    /**
     * Returns type of parameter.
     * @param parameter
     * @return
     * @throws IOException 
     */
    T asType(P parameter) throws IOException;
    /**
     * Returns method for given class, name and parameters.
     * @param cls
     * @param name
     * @param parameters
     * @return
     * @throws IOException 
     */
    M getMethod(Class<?> cls, String name, Class<?>... parameters) throws IOException;
    /**
     * Returns field for given class and name.
     * @param cls
     * @param name
     * @return
     * @throws IOException 
     */
    F getField(Class<?> cls, String name) throws IOException;
    /**
     * Returns true if given method wants it's parameter as radians.
     * @param method
     * @return
     * @throws IOException 
     */
    boolean isRadianArgs(M method) throws IOException;
    /**
     * Returns true if given methods return value is in radians.
     * @param method
     * @return
     * @throws IOException 
     */
    boolean isRadianReturn(M method) throws IOException;
}
