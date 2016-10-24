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

import java.util.List;
import org.vesalainen.math.Arithmetic;
import org.vesalainen.math.Conditional;

/**
 * @author Timo Vesalainen
 * @param <T> Type
 * @param <M> Method
 * @param <F> Field
 * @param <P> Parameter
 */
public interface ExpressionHandler<T,M,F,P> extends Arithmetic, Conditional
{
    /**
     * Load variable value and push to stack
     * @param identifier
     * @throws Exception 
     */
    void loadVariable(String identifier) throws Exception;
    /**
     * Push literal number to stack.
     * @param number
     * @throws Exception 
     */
    void number(String number) throws Exception;
    /**
     * Set index parsing mode. In true mode we are parsing inside bracket where 
     * index type must be int.
     * @param on 
     */
    void setIndex(boolean on);

    void loadArray() throws Exception;

    void loadArrayItem() throws Exception;
    /**
     * Convert this handlers type to given type.
     * @param type
     * @throws Exception 
     */
    void convertTo(T type) throws Exception;
    /**
     * Convert given type to this handlers type.
     * @param type
     * @throws Exception 
     */
    void convertFrom(T type) throws Exception;
    /**
     * Invoke given method.
     * @param method
     * @throws Exception 
     */
    void invoke(M method) throws Exception;
    /**
     * Push fields current value to stack.
     * @param field
     * @throws Exception 
     */
    void loadField(F field) throws Exception;
    /**
     * Generates code that calculates power with integer argument rather that
     * using pow() function.
     * @param i
     * @throws Exception 
     * @see java.lang.Math#pow(double, double) 
     */
    void pow(int i) throws Exception;
    /**
     * Returns method with given name and with given number of arguments.
     * @param funcName
     * @param args
     * @return
     * @throws Exception 
     */
    M findMethod(String funcName, int args) throws Exception;
    /**
     * Returns list of parameters for given method.
     * @param method
     * @return
     * @throws Exception 
     */
    List<? extends P> getParameters(M method) throws Exception;
    /**
     * Returns the return type of given method.
     * @param method
     * @return
     * @throws Exception 
     */
    T getReturnType(M method) throws Exception;
    /**
     * Returns type of parameter.
     * @param parameter
     * @return
     * @throws Exception 
     */
    T asType(P parameter) throws Exception;
    /**
     * Returns method for given class, name and parameters.
     * @param cls
     * @param name
     * @param parameters
     * @return
     * @throws Exception 
     */
    M getMethod(Class<?> cls, String name, Class<?>... parameters) throws Exception;
    /**
     * Returns field for given class and name.
     * @param cls
     * @param name
     * @return
     * @throws Exception 
     */
    F getField(Class<?> cls, String name) throws Exception;
    /**
     * Returns true if given method wants it's parameter as radians.
     * @param method
     * @return
     * @throws Exception 
     */
    boolean isRadianArgs(M method) throws Exception;
    /**
     * Returns true if given methods return value is in radians.
     * @param method
     * @return
     * @throws Exception 
     */
    boolean isRadianReturn(M method) throws Exception;
}
