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
import org.vesalainen.regex.RangeSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.vesalainen.parser.util.NumSet;

/**
 * This class represent a state in deterministic finite automaton (DFA)
 * @author tkv
 */
public final class DFAState<T> extends State<T> implements Vertex<DFAState<T>>, Iterable<DFAState<T>>
{
    private Set<NFAState<T>> nfaSet;
    private Map<Range,Transition<DFAState<T>>> transitions = new HashMap<>();
    // edges and inStates are initially constructed from transitions. However during
    // dfa distribution these are changed while transitions are not!
    private Set<DFAState<T>> edges = new NumSet<>();
    private Set<DFAState<T>> inStates = new NumSet<>();
    
    private int acceptStartLength;
    private boolean distributed;    // true if this state is a root of distributed dfa
    private boolean acceptImmediately;  // if true the string is accepted without trying to read more input
    /**
     * Creates a DFAState<R> from a set of NFAStates
     * @param nfaSet
     */
    public DFAState(Scope<DFAState<T>> scope, Set<NFAState<T>> nfaSet)
    {
        super(scope);
        this.nfaSet = nfaSet;
        for (NFAState<T> ns : nfaSet)
        {
            if (ns.isAccepting())
            {
                if (
                        getToken() == null || 
                        getToken().equals(ns.getToken()) ||
                        getPriority() < ns.getPriority()
                        )
                {
                    setToken(ns.getToken());
                    setPriority(ns.getPriority());
                    setAcceptImmediately(ns.isAcceptImmediately());
                }
                else
                {
                    if (getPriority() == ns.getPriority())
                    {
                        throw new AmbiguousExpressionException("conflicting tokens ",getToken(), ns.getToken());
                    }
                }
            }
        }
    }

    public boolean isAcceptImmediately()
    {
        return acceptImmediately;
    }

    public void setAcceptImmediately(boolean acceptImmediately)
    {
        this.acceptImmediately = acceptImmediately;
    }

    public boolean isDistributed()
    {
        return distributed;
    }

    void setDistributed(boolean distributed)
    {
        if (inStates.size() != 1)
        {
            throw new IllegalArgumentException("setting distributed states which has "+inStates.size()+" instates");
        }
        this.distributed = distributed;
        for (DFAState<T> in : inStates)
        {
            in.edges.remove(this);
        }
        inStates.clear();
    }

    /**
     * If FIXED_ENDER option is used this method returns the length of fixed end.
     * For example fixed end length for .*end is 3.
     * @return
     */
    public int getFixedEndLength()
    {
        // if DFAState<R> has fixed end length it must have only one NFAState
        if (nfaSet.size() == 1)
        {
            for (NFAState<T> nfa : nfaSet)
            {
                return nfa.getFixedEndLength();
            }
        }
        return 0;
    }

    /**
     * Returns true if this state can accept a prefix constructed from
     * a list of ranges starting from dfa start state
     * @return
     */
    public int getAcceptStartLength()
    {
        return acceptStartLength;
    }
    /**
     * Set this state can accept a prefix constructed from
     * a list of ranges starting from dfa start state
     * @param acceptStart
     */
    void setAcceptStartLength(int acceptStartLength)
    {
        this.acceptStartLength = acceptStartLength;
    }

    /**
     * Returns true if dfa starting from this state can accept a prefix constructed from
     * a list of ranges
     * @param prefix
     * @return
     */
    int matchLength(List<Range> prefix)
    {
        DFAState<T> state = this;
        int len = 0;
        for (Range range : prefix)
        {
            state = state.transit(range);
            if (state == null)
            {
                break;
            }
            len++;
        }
        return len;
    }
    /**
     * Calculates a value of how selective transitions are from this state.
     * Returns 1 if all ranges accept exactly one character. Greater number
     * means less selectivity
     * @return
     */
    public int getTransitionSelectivity()
    {
        int count = 0;
        for (Transition<DFAState<T>> t : transitions.values())
        {
            Range range = t.getCondition();
            count += range.getTo()-range.getFrom();
        }
        return count/transitions.size();
    }
    /**
     * Return true if one of transition ranges is a boundary match.
     * @return
     */
    public boolean hasBoundaryMatches()
    {
        for (Transition<DFAState<T>> t : transitions.values())
        {
            Range range = t.getCondition();
            if (range.getFrom() < 0)
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Optimizes transition by merging ranges
     */
    void optimizeTransitions()
    {
        HashMap<DFAState<T>,RangeSet> hml = new HashMap<>();
        for (Transition<DFAState<T>> t : transitions.values())
        {
            RangeSet rs = hml.get(t.getTo());
            if (rs == null)
            {
                rs = new RangeSet();
                hml.put(t.getTo(), rs);
            }
            rs.add(t.getCondition());
        }
        transitions.clear();
        for (DFAState<T> dfa : hml.keySet())
        {
            RangeSet rs = RangeSet.merge(hml.get(dfa));
            for (Range r : rs)
            {
                addTransition(r, dfa);
            }
        }
    }
    /**
     * Return a RangeSet containing all ranges
     * @return
     */
    RangeSet possibleMoves()
    {
        List<RangeSet> list = new ArrayList<>();
        for (NFAState<T> nfa : nfaSet)
        {
            list.add(nfa.getConditions());
        }
        return RangeSet.split(list);
    }
    void removeTransitionsFromAcceptImmediatelyStates()
    {
        if (acceptImmediately)
        {
            for (Transition<DFAState<T>> tr : transitions.values())
            {
                DFAState<T> to = tr.getTo();
                edges.remove(to);
                to.inStates.remove(this);
            }
            transitions.clear();
        }
    }
    
    /**
     * Removes transition to states that doesn't contain any outbound transition
     * and that are not accepting states
     */
    void removeDeadEndTransitions()
    {
        Iterator<Range> it = transitions.keySet().iterator();
        while (it.hasNext())
        {
            Range r = it.next();
            if (transitions.get(r).getTo().isDeadEnd())
            {
                it.remove();
            }
        }
    }
    /**
     * Returns true if state doesn't contain any outbound transition
     * and is not accepting state
     * @return
     */
    boolean isDeadEnd()
    {
        if (isAccepting())
        {
            return false;
        }
        for (Transition<DFAState<T>> next : transitions.values())
        {
            if (next.getTo() != this)
            {
                return false;
            }
        }
        return true;
    }
    /**
     * Return true if state doesn't have any outbound transitions
     * @return
     */
    boolean isEndStop()
    {
        return transitions.isEmpty();
    }
    /**
     * Returns a set of nfA states to where it is possible to move by nfa transitions.
     * @param condition
     * @return
     */
    Set<NFAState<T>> nfaTransitsFor(Range condition)
    {
        Set<NFAState<T>> nset = new NumSet<>();
        for (NFAState<T> nfa : nfaSet)
        {
            nset.addAll(nfa.transit(condition));
        }
        return nset;
    }

    /**
     * Returns next DFAState<R> for given input or null if no transition exist
     * @param input
     * @return
     */
    public DFAState<T> transit(int input)
    {
        for (Range c : transitions.keySet())
        {
            if (c instanceof Range)
            {
                Range r = (Range) c;
                if (r.accept(input))
                {
                    return transitions.get(r).getTo();
                }
            }
            else
            {
                throw new UnsupportedOperationException("transit not possible for non InputCondition");
            }
        }
        return null;
    }
    /**
     * Returns next DFAState<R> for given condition or null if no transition exist
     * @param input
     * @return
     */
    public DFAState<T> transit(Range condition)
    {
        Transition<DFAState<T>> t = transitions.get(condition);
        if (t != null)
        {
            return t.getTo();
        }
        return null;
    }
    /**
     * Adds a new transition
     * @param condition
     * @param to
     */
    void addTransition(Range condition, DFAState<T> to)
    {
        Transition<DFAState<T>> t = new Transition<>(condition, this, to);
        t = transitions.put(t.getCondition(), t);
        assert t == null;
        edges.add(to);
        to.inStates.add(this);
    }
    /**
     * return all transitions
     * @return
     */
    public Collection<Transition<DFAState<T>>> getTransitions()
    {
        return transitions.values();
    }
    /**
     * Return the set of nfa states from which this dfa state is constructed of.
     * @return
     */
    Set<NFAState<T>> getNfaSet()
    {
        return nfaSet;
    }

    /*
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (NFAState nfa : nfaSet)
        {
            if (first)
            {
                sb.append(nfa);
                first = false;
            }
            else
            {
                sb.append(","+nfa);
            }
        }
        sb.append("}");
        return sb.toString();
    }
     *
     */

    @Override
    public Collection<DFAState<T>> edges()
    {
        return edges;
    }

    public Collection<DFAState<T>> inStates()
    {
        return inStates;
    }

    @Override
    public Iterator<DFAState<T>> iterator()
    {
        return new DiGraphIterator<>(this);
    }

}
