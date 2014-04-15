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

import org.vesalainen.regex.Range;

/**
 * Transition class represents a transition from state to state. Transition can have a condition.
 * If condition is null the transition is epsilon transition.
 * @author tkv
 */
public class Transition<S extends State>
{
    private Range condition;
    private S from;
    private S to;
    private int repeat = 1;
    /**
     * Creates an epsilon transition
     * @param from
     * @param to
     */
    public Transition(S from, S to)
    {
        assert from != null;
        assert to != null;
        this.from = from;
        this.to = to;
    }
    /**
     * Creates a non epsilon transition
     * @param condition
     * @param from
     * @param to
     */
    public Transition(Range condition, S from, S to)
    {
        assert from != null;
        assert to != null;
        this.condition = condition;
        this.from = from;
        this.to = to;
    }
    /**
     * Returns true if this transition is epsilon.
     * @return
     */
    public boolean isEpsilon()
    {
        return condition == null;
    }

    public Range getCondition()
    {
        return condition;
    }

    public S getFrom()
    {
        return from;
    }

    public S getTo()
    {
        return to;
    }

    @Override
    public String toString()
    {
        return from+ "-" + condition + ">" + to;
    }

    public int getRepeat()
    {
        return repeat;
    }

    void setRepeat(int repeat)
    {
        this.repeat = repeat;
    }

    void setTo(S to)
    {
        this.to = to;
    }

}
