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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Reduce implements TerminalAction
{
    private GTerminal symbol;
    private GRule rule;

    public Reduce(GTerminal symbol, GRule rule)
    {
        this.symbol = symbol;
        this.rule = rule;
    }

    public GRule getRule()
    {
        return rule;
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
        final Reduce other = (Reduce) obj;
        if (this.symbol != other.symbol && (this.symbol == null || !this.symbol.equals(other.symbol)))
        {
            return false;
        }
        if (this.rule != other.rule && (this.rule == null || !this.rule.equals(other.rule)))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 79 * hash + (this.symbol != null ? this.symbol.hashCode() : 0);
        hash = 79 * hash + (this.rule != null ? this.rule.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return symbol + " reduce " + rule;
    }

}
