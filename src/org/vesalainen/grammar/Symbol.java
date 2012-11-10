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
package org.vesalainen.grammar;

import java.io.IOException;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import org.vesalainen.parser.util.HtmlPrinter;
import org.vesalainen.parser.util.Numerable;

/**
 * NOTE! this is abstract class to protect setNumber method not being public
 * @author tkv
 */
public abstract class Symbol implements Numerable
{
    public static final int FIRST_NUMBER = 6;
    
    private int number;

    Symbol(int number)
    {
        this.number = number;
    }
    
    /**
     * Note that number is set by grammar. Any symbol must have unique number.
     * @param number
     */
    void setNumber(int number)
    {
        if (number < 0)
        {
            throw new IllegalArgumentException("nonterminal number must be positive");
        }
        this.number = number;
    }
    @Override
    public int getNumber()
    {
        return number;
    }

    abstract public String getName();
    abstract public boolean isStart();
    abstract public boolean isNil();
    abstract public boolean isOmega();
    abstract public boolean isEmpty();
    abstract public boolean isEof();
    abstract public boolean isError();

    abstract public Member getReducer();

    abstract public Type getReducerType();

    abstract public void print(HtmlPrinter p) throws IOException;
}
