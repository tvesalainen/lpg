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
package org.vesalainen.grammar.state;

import org.vesalainen.parser.util.Numerable;

/**
 * A base class for NFAState and DFAState
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class State<T> implements Numerable
{
    protected Scope scope;
    protected int number;
    private T token;
    private int priority;   // used to set acceptance priorities. E.g 'xml' could have higher priority than [a-z]+

    public State(Scope scope)
    {
        this.scope = scope;
        this.number = scope.next();
    }

    /**
     * Copy constructor
     * @param other
     */
    protected State(Scope scope, State<T> other)
    {
        this.scope = scope;
        this.number = scope.next();
        token = other.token;
    }

    int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }
    
    /**
     * Changes priority. priority += change.
     * @param change 
     */
    public void changePriority(int change)
    {
        priority += change;
    }
    /**
     * Returns true if this state is accepting.
     * @return
     */
    public boolean isAccepting()
    {
        return token != null;
    }
    /**
     * Return the reducer value
     * @return
     */
    public T getToken()
    {
        return token;
    }
    /**
     * Sets the reducer value
     * @param token
     */
    public void setToken(T token)
    {
        if (token == null)
        {
            throw new IllegalArgumentException("setting null token is illegal");
        }
        this.token = token;
    }

    @Override
    public String toString()
    {
        if (token == null)
        {
            return "S"+number;
        }
        else
        {
            return "S("+number+")";
        }
    }
    @Override
    public int hashCode()
    {
        return number;
    }

    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final State<T> other = (State<T>) obj;
        if (this.scope != other.scope)
        {
            throw new IllegalArgumentException("testing equality of State<T>s from different scope");
        }
        if (this.number != other.number)
        {
            return false;
        }
        return true;
    }

    public int getNumber()
    {
        return number;
    }

    public Scope getScope()
    {
        return scope;
    }


}
