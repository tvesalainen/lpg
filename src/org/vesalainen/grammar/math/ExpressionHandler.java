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
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author Timo Vesalainen
 */
public abstract class ExpressionHandler<F extends ExpressionHandlerFactory> implements ExpressionHandlerFactory
{
    protected Class<? extends Number> type;
    protected F factory;
    protected int stack;
    private Deque<Integer> deque = new ArrayDeque<Integer>();

    public ExpressionHandler(Class<? extends Number> type, F factory)
    {
        this.type = type;
        this.factory = factory;
    }

    @Override
    public ExpressionHandler getInstance(Class<? extends Number> type)
    {
        return factory.getInstance(type);
    }

    public Class<? extends Number> getType()
    {
        return type;
    }
    
    public abstract void loadVariable(String identifier) throws IOException;

    public abstract void abs() throws IOException;

    public abstract void add() throws IOException;

    public abstract void subtract() throws IOException;

    public abstract void mul() throws IOException;

    public abstract void div() throws IOException;

    public abstract void mod() throws IOException;

    public abstract void neg() throws IOException;

    public abstract void number(String number) throws IOException;

    public abstract void arrayIndex() throws IOException;

    public abstract void invoke(String identifier, int stack) throws IOException;

    protected void stack(int change)
    {
        stack += change;
    }

    int pop()
    {
        return stack - deque.pop();
    }

    void push()
    {
        deque.push(stack);
    }

    public abstract void arrayIndexMode(boolean on);


}
