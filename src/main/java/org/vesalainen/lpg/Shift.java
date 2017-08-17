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
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Shift implements TerminalAction
{
    private GTerminal symbol;
    private Action action;

    public Shift(GTerminal symbol, Action action)
    {
        this.symbol = symbol;
        this.action = action;
    }

    public Action getAction()
    {
        return action;
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
        final Shift other = (Shift) obj;
        if (this.symbol != other.symbol && (this.symbol == null || !this.symbol.equals(other.symbol)))
        {
            return false;
        }
        if (this.action != other.action && (this.action == null || !this.action.equals(other.action)))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 59 * hash + (this.symbol != null ? this.symbol.hashCode() : 0);
        hash = 59 * hash + (this.action != null ? this.action.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return symbol + " Shift " + action;
    }

}
