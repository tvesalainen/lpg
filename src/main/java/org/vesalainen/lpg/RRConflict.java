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
class RRConflict extends Conflict
{
    private Item item1;
    private Item item2;
    private GTerminal symbol;

    public RRConflict(Item item1, Item item2, GTerminal symbol)
    {
        this.item1 = item1;
        this.item2 = item2;
        this.symbol = symbol;
    }
    @Override
    public String toString()
    {
        return "Reduce/Reduce conflict on "+item1+" item "+item2+" symbol "+symbol;
    }

}
