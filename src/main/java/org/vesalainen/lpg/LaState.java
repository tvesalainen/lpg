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
package org.vesalainen.lpg;

import org.vesalainen.grammar.GRule;
import org.vesalainen.grammar.GTerminal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author tkv
 */
public class LaState implements State, Action, ShiftAct
{
    private State inState;
    private GTerminal symbol;
    private int number;
    private List<LaShift> shiftList = new ArrayList<>();
    private List<LaReduce> reduceList = new ArrayList<>();
    private GRule defaultRule;
    private Set<GTerminal> inputSet;

    public LaState()
    {
    }

    public GRule getDefaultRule()
    {
        return defaultRule;
    }

    public void setDefaultRule(GRule defaultRule)
    {
        this.defaultRule = defaultRule;
    }

    public List<LaReduce> getReduceList()
    {
        return reduceList;
    }

    public List<LaShift> getShiftList()
    {
        return shiftList;
    }

    public State getInState()
    {
        return inState;
    }

    public void setInState(State inState)
    {
        this.inState = inState;
    }

    public GTerminal getSymbol()
    {
        return symbol;
    }

    public void setSymbol(GTerminal symbol)
    {
        this.symbol = symbol;
    }

    public int getNumber()
    {
        return number;
    }

    public void setNumber(int number)
    {
        this.number = number;
    }

    void addShift(GTerminal symbol, Act act)
    {
        shiftList.add(new LaShift(symbol, act));
    }

    void addReduce(GTerminal symbol, Act act)
    {
        reduceList.add(new LaReduce(symbol, act));
    }

    public boolean hasInState(State state)
    {
        return inState != null && inState.equals(state);
    }

    public int getInStateCount()
    {
        if (inState != null)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    @Override
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
        final LaState other = (LaState) obj;
        if (this.number != other.number)
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 29 * hash + this.number;
        return hash;
    }

    @Override
    public String toString()
    {
        return "LaState "+number;
    }

    public void setInputSet(Set<GTerminal> set)
    {
        if (inputSet != null)
        {
            throw new IllegalArgumentException("inputSet already set");
        }
        inputSet = set;
    }

    public Set<GTerminal> getInputSet()
    {
        return inputSet;
    }

}
