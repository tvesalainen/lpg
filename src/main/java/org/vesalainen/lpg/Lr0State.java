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
import org.vesalainen.grammar.Nonterminal;
import org.vesalainen.grammar.GTerminal;
import org.vesalainen.parser.util.PeekableIterator;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.vesalainen.parser.util.NumSet;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Lr0State implements Comparable<Lr0State>, State, Action, ShiftAct
{
    private int stateNumber;
    private Set<Item> kernelItems;
    private Set<Item> completeItems = new NumSet<>();
    private Set<Shift> shiftList = new LinkedHashSet<>();
    private Set<Goto> gotoList = new LinkedHashSet<>();
    private GRule defaultReduce;
    private Set<Reduce> reduceList = new LinkedHashSet<>();
    private Set<Lr0State> inStates = new NumSet<>(); // in_stat
    private Set<GTerminal> readSet = new NumSet<>();
    private boolean cyclic;
    private boolean noShiftOnErrorSym;
    private boolean singleCompleteItem;
    private Set<GTerminal> inputSet;

    public Lr0State(int number, Set<Item> kernelItems)
    {
        this.stateNumber = number;
        this.kernelItems = kernelItems;
    }

    public boolean isSingleCompleteItem()
    {
        return singleCompleteItem;
    }

    public void setSingleCompleteItem(boolean singleCompleteItem)
    {
        this.singleCompleteItem = singleCompleteItem;
    }

    public boolean isNoShiftOnErrorSym()
    {
        return noShiftOnErrorSym;
    }

    public void setNoShiftOnErrorSym(boolean noShiftOnErrorSym)
    {
        this.noShiftOnErrorSym = noShiftOnErrorSym;
    }

    public boolean isCyclic()
    {
        return cyclic;
    }

    public void setCyclic(boolean cyclic)
    {
        this.cyclic = cyclic;
    }

    public void addReadSet(GTerminal symbol)
    {
        readSet.add(symbol);
    }

    public void addReadSet(Collection<GTerminal> symbols)
    {
        readSet.addAll(symbols);
    }

    public void setReadSet(Collection<GTerminal> symbols)
    {
        readSet.clear();
        readSet.addAll(symbols);
    }

    public Set<GTerminal> getReadSet()
    {
        return readSet;
    }

    public void addInState(Lr0State state)
    {
        inStates.add(state);
    }
    
    public Set<Lr0State> getInStates()
    {
        return inStates;
    }

    public boolean isFirst()
    {
        return stateNumber == 1;
    }

    public void setDefaultReduce(GRule rule)
    {
        defaultReduce = rule;
    }

    public GRule getDefaultReduce()
    {
        return defaultReduce;
    }

    public void addReduce(GTerminal symbol, GRule rule)
    {
        if (rule != null)
        {
            reduceList.add(new Reduce(symbol, rule));
        }
    }

    public void addShift(GTerminal symbol, Action action)
    {
        shiftList.add(new Shift(symbol, action));
    }

    public void addGoto(Nonterminal symbol, Action action)
    {
        gotoList.add(new Goto(symbol, action));
    }

    public int getNumber()
    {
        return stateNumber;
    }

    public Set<Reduce> getReduceList()
    {
        return reduceList;
    }

    public Set<Goto> getGotoList()
    {
        return gotoList;
    }

    public Set<Item> getCompleteItems()
    {
        return completeItems;
    }

    public Set<Item> getKernelItems()
    {
        return kernelItems;
    }

    public int getStateNumber()
    {
        return stateNumber;
    }

    public PeekableIterator<Item> getCompleteItemsPtr()
    {
        return new PeekableIterator(completeItems.iterator());
    }

    public void addCompleteItem(Item completeItem)
    {
        completeItems.add(completeItem);
    }

    public PeekableIterator<Item> getKernelItemsPtr()
    {
        return new PeekableIterator(kernelItems.iterator());
    }

    public Set<Shift> getShiftList()
    {
        return shiftList;
    }

    public void setShiftMap(Set<Shift> shiftList)
    {
        this.shiftList = shiftList;
    }

    @Override
    public String toString()
    {
        return "State "+stateNumber;
    }

    void setInstat(Lr0State stateNo)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public int compareTo(Lr0State o)
    {
        return stateNumber - o.stateNumber;
    }

    public boolean hasInState(State state)
    {
        return inStates.contains(state);
    }

    public int getInStateCount()
    {
        return inStates.size();
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
