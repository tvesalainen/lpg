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
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Timo Vesalainen
 */
public interface ExpressionHandler
{
    void dup() throws IOException;
    
    void add() throws IOException;

    void div() throws IOException;

    void loadVariable(String identifier) throws IOException;

    void mod() throws IOException;

    void mul() throws IOException;

    void neg() throws IOException;

    void number(String number) throws IOException;

    void subtract() throws IOException;
    
    void setIndex(boolean on);

    void loadArray() throws IOException;

    void loadArrayItem() throws IOException;

    void convertTo(Class<?> aClass) throws IOException;

    void convertFrom(Class<?> aClass) throws IOException;

    void invoke(Method method) throws IOException;
    
    void loadField(Field field) throws IOException;

    void pow(int i) throws IOException;

}
