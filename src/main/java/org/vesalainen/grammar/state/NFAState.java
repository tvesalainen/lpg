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

import org.vesalainen.graph.Vertex;
import org.vesalainen.graph.DiGraphIterator;
import org.vesalainen.regex.Range;
import org.vesalainen.regex.RangeSet;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This class represent a state in nondeterministic finite automaton (NFA)
 * Note that epsilon transition is implemented being a null range
 * @author tkv
 * @param <T>
 */
public final class NFAState<T> extends State<T> implements Vertex<NFAState<T>>, Iterable<NFAState<T>>
{
    private final Map<Range,Set<Transition<NFAState<T>>>> transitions = new HashMap<>();
    private boolean endStop;
    private int fixedEndLength;
    private boolean acceptImmediately;  // if true the string is accepted without trying to read more input
    // edges and inStates are initially constructed from transitions. However during
    // dfa distribution these are changed while transitions are not!
    private final Set<NFAState<T>> edges = new HashSet<>();
    private final Set<NFAState<T>> inStates = new HashSet<>();
    
    /**
     * Construct a nfa state which has no transitions.
     * @param scope
     */
    public NFAState(Scope<NFAState<T>> scope)
    {
        super(scope);
    }
    /**
     * Construct a new nfa state by cloning other state as well as all connected
     * states.
     * @param other
     * @param map
     */
    NFAState(Scope<NFAState<T>> scope, NFAState<T> other, Map<NFAState<T>,NFAState<T>> map)
    {
        super(scope, other);
        map.put(other, this);
        for (Range r : other.transitions.keySet())
        {
            Set<Transition<NFAState<T>>> s = other.transitions.get(r);
            for (Transition<NFAState<T>> t : s)
            {
                NFAState<T> to = map.get(t.getTo());
                if (to == null)
                {
                    to = new NFAState<>(scope, t.getTo(), map);
                }
                addTransition(r, to);
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

    /**
     * If FIXED_ENDER option is used this method returns the length of fixed end.
     * For example fixed end length for .*end is 3.
     * @return
     */
    public int getFixedEndLength()
    {
        return fixedEndLength;
    }

    public void setFixedEndLength(int fixedEndLength)
    {
        this.fixedEndLength = fixedEndLength;
    }

    public boolean hasTransitions()
    {
        return !transitions.isEmpty();
    }
    public int getTransitionCount()
    {
        int count = 0;
        for (Range range : transitions.keySet())
        {
            Set<Transition<NFAState<T>>> set = transitions.get(range);
            count += set.size();
        }
        return count;
    }
    /**
     * Returns true if transition with condition to state exists
     * @param condition
     * @param state
     * @return
     */
    public boolean hasTransitionTo(Range condition, NFAState<T> state)
    {
        Set<Transition<NFAState<T>>> set = transitions.get(condition);
        if (set != null)
        {
            for (Transition<NFAState<T>> tr : set)
            {
                if (state.equals(tr.getTo()))
                {
                    return true;
                }
            }
        }
        return false;
    }
    public RangeSet getNonEpsilonConditions()
    {
        RangeSet rs = new RangeSet();
        for (Range range : transitions.keySet())
        {
            if (range != null)
            {
                Set<Transition<NFAState<T>>> set2 = transitions.get(range);
                for (Transition<NFAState<T>> tr : set2)
                {
                    rs.add(range);
                }
            }
        }
        return rs;
    }
    public RangeSet getNonEpsilonConditionsTo(NFAState<T> state)
    {
        RangeSet rs = new RangeSet();
        for (Range range : transitions.keySet())
        {
            if (range != null)
            {
                Set<Transition<NFAState<T>>> set2 = transitions.get(range);
                for (Transition<NFAState<T>> tr : set2)
                {
                    if (state.equals(tr.getTo()))
                    {
                        rs.add(range);
                    }
                }
            }
        }
        return rs;
    }
    /**
     * Returns a set of NFAState<T>s to where a non epsilon transition exists
     * @return
     */
    public Set<NFAState<T>> getNonEpsilonDestinations()
    {
        Set<NFAState<T>> set = new HashSet<>();
        for (Range range : transitions.keySet())
        {
            if (range != null)
            {
                Set<Transition<NFAState<T>>> set2 = transitions.get(range);
                for (Transition<NFAState<T>> tr : set2)
                {
                    set.add(tr.getTo());
                }
            }
        }
        return set;
    }
    public void removeAllNonEpsilonConditionsTo(NFAState<T> state)
    {
        for (Range range : transitions.keySet())
        {
            if (range != null)
            {
                Set<Transition<NFAState<T>>> set2 = transitions.get(range);
                for (Transition<NFAState<T>> tr : set2)
                {
                    if (state.equals(tr.getTo()))
                    {
                        set2.remove(tr);
                    }
                }
            }
        }
    }
    /**
     * Returns non epsilon confitions to transit to given state
     * @param state
     * @return
     */
    public RangeSet getConditionsTo(NFAState<T> state)
    {
        RangeSet rs = new RangeSet();
        for (Range range : transitions.keySet())
        {
            if (range != null)
            {
                Set<Transition<NFAState<T>>> set2 = transitions.get(range);
                for (Transition<NFAState<T>> tr : set2)
                {
                    if (state.equals(tr.getTo()))
                    {
                        rs.add(range);
                    }
                }
            }
        }
        return rs;

    }
    /**
     * Returns Transition set instance if NFAState<T> has only transitions to given
     * state and no transitions to anywhere else. Otherwise returns null.
     * @param state
     * @return
     */
    public Set<Transition<NFAState<T>>> getSingleTransitionTo(NFAState<T> state)
    {
        Set<Transition<NFAState<T>>> set = new HashSet<>();
        if  (transitions.size() != 1)
        {
            return null;
        }
        for (Range range : transitions.keySet())
        {
            Set<Transition<NFAState<T>>> s = transitions.get(range);
            for (Transition<NFAState<T>> tr : s)
            {
                if (state.equals(tr.getTo()))
                {
                    set.add(tr);
                }
                else
                {
                    return null;    // to somewhere else
                }
            }
        }
        if (set.isEmpty())
        {
            return null;
        }
        else
        {
            return set;
        }
    }
    public NFAState<T> prev()
    {
        if (inStates.size() == 1)
        {
            for (NFAState<T> p : inStates)
            {
                return p;
            }
        }
        return null;
    }
    public Set<NFAState<T>> inStates()
    {
        return inStates;
    }
    /**
     * Returns true if set only has one epsilon transition.
     * @param set
     * @return
     */
    public static boolean isSingleEpsilonOnly(Set<Transition<NFAState>> set)
    {
        if (set.size() != 1)
        {
            return false;
        }
        for (Transition<NFAState> tr : set)
        {
            if (tr.isEpsilon())
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns true if this state is included in accepting dfa state
     * @return
     */
    public boolean isEndStop()
    {
        return endStop;
    }
    void setEndStop(boolean endStop)
    {
        this.endStop = endStop;
    }
    /**
     * Returns true if this state is not accepting and doesn't have any outbound
     * transitions.
     * @param nfaSet
     * @return
     */
    boolean isDeadEnd(Set<NFAState<T>> nfaSet)
    {
        for (Set<Transition<NFAState<T>>> set : transitions.values())
        {
            for (Transition<NFAState<T>> t : set)
            {
                if (!nfaSet.contains(t.getTo()))
                {
                    return false;
                }
            }
        }
        return true;
    }
    /**
     * Construct a dfa by using this state as starting state.
     * @param dfaScope
     * @return
     */
    public DFAState<T> constructDFA(Scope<DFAState<T>> dfaScope)
    {
        Map<Set<NFAState<T>>,DFAState<T>> all = new HashMap<>();
        Deque<DFAState<T>> unmarked = new ArrayDeque<>();
        Set<NFAState<T>> startSet = epsilonClosure(dfaScope);
        DFAState<T> startDfa = new DFAState<>(dfaScope, startSet);
        all.put(startSet, startDfa);
        unmarked.add(startDfa);
        while (!unmarked.isEmpty())
        {
            DFAState<T> dfa = unmarked.pop();
            for (Range c : dfa.possibleMoves())
            {
                Set<NFAState<T>> moveSet = dfa.nfaTransitsFor(c);
                if (!moveSet.isEmpty())
                {
                    Set<NFAState<T>> newSet = epsilonClosure(dfaScope, moveSet);
                    DFAState<T> ndfa = all.get(newSet);
                    if (ndfa == null)
                    {
                        ndfa = new DFAState<>(dfaScope, newSet);
                        all.put(newSet, ndfa);
                        unmarked.add(ndfa);
                    }
                    dfa.addTransition(c, ndfa);
                }
            }
        }
        // optimize
        for (DFAState<T> dfa : all.values())
        {
            dfa.removeTransitionsFromAcceptImmediatelyStates();
        }
        for (DFAState<T> dfa : all.values())
        {
            dfa.removeDeadEndTransitions();
        }
        for (DFAState<T> dfa : startDfa)
        {
            dfa.optimizeTransitions();
        }
        return startDfa;
    }
    /**
     * Returns all ranges from all transitions
     * @return
     */
    RangeSet getConditions()
    {
        RangeSet is = new RangeSet();
        for (Range ic : transitions.keySet())
        {
            if (ic != null)
            {
                is.add(ic);
            }
        }
        return is;
    }
    /**
     * Returns a set of states that can be reached by epsilon transitions.
     * @param marked
     * @return
     */
    private Set<NFAState<T>>  epsilonTransitions(StateVisitSet<NFAState<T>> marked)
    {
        marked.add(this);
        Set<NFAState<T>> set = new HashSet<>();
        for (NFAState<T> nfa : epsilonTransit())
        {
            if (!marked.contains(nfa))
            {
                set.add(nfa);
                set.addAll(nfa.epsilonTransitions(marked));
            }
        }
        return set;
    }
    /**
     * Creates a dfa state from all nfa states that can be reached from this state
     * with epsilon move.
     * @param scope
     * @return
     */
    public Set<NFAState<T>> epsilonClosure(Scope<DFAState<T>> scope)
    {
        Set<NFAState<T>> set = new HashSet<>();
        set.add(this);
        return epsilonClosure(scope, set);
    }
    /**
     *
     * @param set
     * @return
     */
    private Set<NFAState<T>> epsilonClosure(Scope<DFAState<T>> scope, Set<NFAState<T>> set)
    {
        StateVisitSet<NFAState<T>> marked = new StateVisitSet<>();
        Set<NFAState<T>> result = new HashSet<>();
        result.addAll(set);
        for (NFAState<T> nfa : set)
        {
            result.addAll(nfa.epsilonTransitions(marked));
        }
        return result;
    }
    
    private Set<NFAState<T>> epsilonTransit()
    {
        return transit(null);
    }

    Set<NFAState<T>> transit(Range condition)
    {
        Set<NFAState<T>> set = new HashSet<>();
        for (Range r : transitions.keySet())
        {
            if (
                    (condition == null && r == null) ||
                    (r != null && r.contains(condition))
                    )
            {
                Set<Transition<NFAState<T>>> s = transitions.get(r);
                for (Transition<NFAState<T>> t : s)
                {
                    set.add(t.getTo());
                }
            }
        }
        return set;
    }
    /**
     * Adds a set of transitions
     * @param rs
     * @param to
     */
    public void addTransition(RangeSet rs, NFAState<T> to)
    {
        for (Range c : rs)
        {
            addTransition(c, to);
        }
        edges.add(to);
        to.inStates.add(this);
    }
    /**
     * Adds a transition
     * @param condition
     * @param to
     * @return
     */
    public Transition<NFAState<T>> addTransition(Range condition, NFAState<T> to)
    {
        Transition<NFAState<T>> t = new Transition<>(condition, this, to);
        Set<Transition<NFAState<T>>> set = transitions.get(t.getCondition());
        if (set == null)
        {
            set = new HashSet<>();
            transitions.put(t.getCondition(), set);
        }
        set.add(t);
        edges.add(to);
        to.inStates.add(this);
        return t;
    }
    /**
     * Adds a epsilon transition
     * @param to
     */
    public void addEpsilon(NFAState<T> to)
    {
        Transition<NFAState<T>> t = new Transition<>(this, to);
        Set<Transition<NFAState<T>>> set = transitions.get(null);
        if (set == null)
        {
            set = new HashSet<>();
            transitions.put(null, set);
        }
        set.add(t);
        edges.add(to);
        to.inStates.add(this);
    }
    /**
     * Returns all transitions
     * @return
     */
    public Collection<Set<Transition<NFAState<T>>>> getTransitions()
    {
        return transitions.values();
    }
    
    public void dump(int position, StringBuilder sb, Map<NFAState<T>,Integer> map, Deque<NFAState<T>> deque)
    {
        if (!transitions.isEmpty())
        {
            boolean first = true;
            for (Range range : transitions.keySet())
            {
                for (Transition<NFAState<T>> t : transitions.get(range))
                {
                    if (first)
                    {
                        if (map.containsKey(t.getTo()))
                        {
                            sb.append(this);
                        }
                        else
                        {
                            int p = sb.length();
                            sb.append("-"+range+">"+t.getTo());
                            p = sb.length()-p;
                            t.getTo().dump(p, sb, map, deque);
                        }
                        first = false;
                    }
                    else
                    {
                        if (!map.containsKey(t.getTo()))
                        {
                            map.put(t.getTo(), position);
                            deque.addLast(t.getTo());
                        }
                    }
                }
            }
        }
        else
        {
            sb.append(this);
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
        final NFAState<T> other = (NFAState<T>) obj;
        if (this.scope != other.scope)
        {
            throw new IllegalArgumentException("testing equality of NFAState<R>s from different scope");
        }
        if (this.transitions != other.transitions && (this.transitions == null || !this.transitions.equals(other.transitions)))
        {
            return false;
        }
        if (this.number != other.number)
        {
            return false;
        }
        return true;
    }

    @Override
    public Collection<NFAState<T>> edges()
    {
        return edges;
    }

    @Override
    public Iterator<NFAState<T>> iterator()
    {
        return DiGraphIterator.getInstance(this, Vertex::edges);
    }

}
