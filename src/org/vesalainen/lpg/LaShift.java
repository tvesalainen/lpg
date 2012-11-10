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

import org.vesalainen.grammar.GTerminal;

/**
 *
 * @author tkv
 */
public class LaShift implements TerminalAction
{
    private GTerminal symbol;
    private Act act;

    public LaShift(GTerminal symbol, Act act)
    {
        this.symbol = symbol;
        this.act = act;
    }

    public Act getAct()
    {
        return act;
    }

    public GTerminal getSymbol()
    {
        return symbol;
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
        final LaShift other = (LaShift) obj;
        if (this.symbol != other.symbol && (this.symbol == null || !this.symbol.equals(other.symbol)))
        {
            return false;
        }
        if (this.act != other.act && (this.act == null || !this.act.equals(other.act)))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 67 * hash + (this.symbol != null ? this.symbol.hashCode() : 0);
        hash = 67 * hash + (this.act != null ? this.act.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return "LaShift{"+symbol+"-"+act+'}';
    }

}
