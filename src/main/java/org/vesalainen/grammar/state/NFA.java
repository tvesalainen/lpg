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

import org.vesalainen.graph.DiGraphIterator;
import java.io.IOException;
import java.util.Iterator;
import org.vesalainen.regex.RangeSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.vesalainen.parser.util.NumMap;
import org.vesalainen.parser.util.NumSet;

/**
 * This class represents the nondeterministic finite automaton. 
 * @author tkv
 * @param <T>
 */
public final class NFA<T> implements Iterable<NFAState<T>>
{
    private static final Integer INFINITY = 99999;
    
    private final Scope<NFAState<T>> scope;
    private final NFAState<T> first;
    private NFAState<T> last;
    private boolean union;  // true if NFA constructed by union method
    /**
     * Creates an empty nfa
     * @param scope
     */
    public NFA(Scope<NFAState<T>> scope)
    {
        this.scope = scope;
        first = new NFAState<>(scope);
        last = new NFAState<>(scope);
        first.addEpsilon(last);
    }
    /**
     * Construct a new nfa by cloning the other.
     * @param scope
     * @param other
     */
    public NFA(Scope<NFAState<T>> scope, NFA<T> other)
    {
        this.scope = scope;
        Map<NFAState<T>,NFAState<T>> map = new NumMap<>();
        for (NFAState<T> s : other)
        {
            NFAState<T> to = map.get(s);
            if (to == null)
            {
                to = new NFAState(scope, s, map);
                map.put(s, to);
            }
        }
        first = map.get(other.first);
        last = map.get(other.last);
    }
    /**
     * Constructs a nfa containing two states which have transitions from first
     * to last
     * <p>
     * first -rs> last
     * @param scope
     * @param rs Range set containing the transition ranges.
     */
    public NFA(Scope<NFAState<T>> scope, RangeSet rs)
    {
        this.scope = scope;
        first = new NFAState<>(scope);
        last = new NFAState<>(scope);
        first.addTransition(rs, last);
    }
    /**
     * Creates a union nfa. First state has epsilon moves to both nfa's and both
     * nfa's have epsilon moves to last state.
     * @param scope
     * @param nfa1
     * @param nfa2
     */
    public NFA(Scope<NFAState<T>> scope, NFA<T> nfa1, NFA<T> nfa2)
    {
        this.scope = scope;
        union = true;
        if (nfa1.union)
        {
            first = nfa1.first;
            last = new NFAState<>(scope);
            nfa1.last.addEpsilon(last);
            first.addEpsilon(nfa2.getFirst());
            nfa2.getLast().addEpsilon(last);
        }
        else
        {
            if (nfa2.union)
            {
                first = nfa2.first;
                last = new NFAState<>(scope);
                nfa2.last.addEpsilon(last);
                first.addEpsilon(nfa1.getFirst());
                nfa1.getLast().addEpsilon(last);
            }
            else
            {
                first = new NFAState<>(scope);
                last = new NFAState<>(scope);
                first.addEpsilon(nfa1.getFirst());
                first.addEpsilon(nfa2.getFirst());
                nfa1.getLast().addEpsilon(last);
                nfa2.getLast().addEpsilon(last);
            }
        }
    }

    public NFAState<T> getFirst()
    {
        return first;
    }

    public NFAState<T> getLast()
    {
        return last;
    }
    
    /**
     * Marks all nfa states that can be accepting to end stop.
     */
    public void analyzeEndStop()
    {
        DFA<T> dfa = constructDFA(new Scope<DFAState<T>>("analyzeEndStop"));
        for (DFAState<T> s : dfa)
        {
            if (s.isAccepting())
            {
                if (s.isEndStop())
                {
                    for (NFAState<T> n : s.getNfaSet())
                    {
                        if (n.isAccepting())
                        {
                            n.setEndStop(true);
                        }
                    }
                }
            }
        }
    }
    /**
     *  Constructs a dfa from using first nfa state as starting state.
     * @param scope
     * @return
     */
    public DFA<T> constructDFA(Scope<DFAState<T>> scope)
    {
        return new DFA<>(first.constructDFA(scope), scope.count());
    }
    /**
     * Concatenates this to nfa by making epsilon move from this last to nfa first.
     * @param nfa
     */
    public void concat(NFA<T> nfa)
    {
        last.addEpsilon(nfa.getFirst());
        last = nfa.last;
    }
    /**
     * Changes this nfa to be a Kleenes star by adding epsilon moves from first to
     * last and vice versa. Extra epsilon connected state is added at the end.
     * This is to prevent future epsilon moves from flowing backwards. E.g (ab*)?
     */
    public void star()
    {
        last.addEpsilon(first);
        first.addEpsilon(last);
        NFAState<T> s = new NFAState<>(scope);
        last.addEpsilon(s);
        last = s;
    }
    /**
     * Changes this nfa to be optional by adding epsilon from first to last.
     */
    public void opt()
    {
        first.addEpsilon(last);
    }

    /**
     * Precondition is that the NFA is ,e.g, for [a-z]+end like
     * <code>
     * s1-a-z>s2-a-z>s3-ε>s4-e>s5-ε>s6-n>s7-ε>s8-d>s9
     *         <--ε-->
     * </code>
     * After modification there exists recovery transitions to repetition and transition
     * s2 ->s3 is modified not to include e
     * <code>
     * s1-a-z>s2-a-df-z>s3-ε>s4-e>s5-ε>s6-n>s7-ε>s8-d>s9
     *         <--ε----->
     *         <-----[a-mo-z>----------|
     *         <---------------[a-ce-z>----------|
     * </code>
     * @param nfa
     */
    public static void modifyFixedEnder(NFA nfa)
    {
        List<NFAState> list = new ArrayList<>();
        NFAState end = nfa.getLast();
        if (end.hasTransitions())
        {
            throw new IllegalArgumentException("FIXED_ENDER for not fixed end expression. E.g ab*");
        }
        NFAState prev = end;
        Set<Transition<NFAState>> firstOfFixedEnder = null;
        NFAState current = prev.prev();
        while (current != null)
        {
            if (current.hasTransitionTo(null, prev) && prev.hasTransitionTo(null, current))
            {
                break;
            }
            Set<Transition<NFAState>> transitionSet = current.getSingleTransitionTo(prev);
            if (transitionSet != null)
            {
                if (!NFAState.isSingleEpsilonOnly(transitionSet))
                {
                    list.add(current);
                    firstOfFixedEnder = transitionSet;
                }
            }
            else
            {
                if (current.hasTransitionTo(null, prev))
                {
                    prev = current;
                    current = prev.prev();
                }
                else
                {
                    throw new IllegalArgumentException("FIXED_ENDER expression problem");
                }
                break;
            }
            prev = current;
            current = prev.prev();
        }
        if (list.isEmpty())
        {
            throw new IllegalArgumentException("FIXED_ENDER too short. E.g, a*a");
        }
        if (!current.hasTransitionTo(null, prev) || !prev.hasTransitionTo(null, current))
        {
            throw new IllegalArgumentException("FIXED_ENDER expression doesn't have repetition E.g a*. (1)");
        }
        RangeSet rs = current.getNonEpsilonConditionsTo(prev);
        if (rs.isEmpty())
        {
            throw new IllegalArgumentException("FIXED_ENDER expression doesn't have repetition E.g a*.(2)");
        }
        for (NFAState s : list)
        {
            RangeSet rs2 = new RangeSet(rs);
            RangeSet rs3 = s.getNonEpsilonConditions();
            rs2.remove(rs3);
            s.addTransition(rs2, current);
        }
        current.removeAllNonEpsilonConditionsTo(prev);
        RangeSet rs4 = new RangeSet();
        for (Transition<NFAState> tr : firstOfFixedEnder)
        {
            rs4.add(tr.getCondition());
        }
        rs.remove(rs4);
        current.addTransition(rs, prev);
        end.setFixedEndLength(list.size());
    }
    /**
     * Returns a set of all connected NFAState<R>s
     * @return
     */
    public Set<NFAState<T>> getAll()
    {
        Set<NFAState<T>> set = new NumSet<>();
        Map<NFAState<T>,Integer> indexOf = new NumMap<>();
        Deque<NFAState<T>> stack = new ArrayDeque<>();
        for (NFAState<T> state : this)
        {
            if (!indexOf.containsKey(state))
            {
                collect(state, indexOf, stack, set);
            }

        }
        return set;
    }

    private void collect(
            NFAState<T> state,
            Map<NFAState<T>,Integer> indexOf,
            Deque<NFAState<T>> stack,
            Set<NFAState<T>> set
            )
    {
        stack.push(state);
        int index = stack.size();
        indexOf.put(state, index);
        set.add(state);

        for (Set<Transition<NFAState<T>>> st : state.getTransitions())
        {
            for (Transition<NFAState<T>> t : st)
            {
                if (!indexOf.containsKey(t.getTo()))
                {
                    collect(t.getTo(),indexOf, stack, set);
                }
                indexOf.put(state, Math.min(indexOf.get(state), indexOf.get(t.getTo())));
            }
        }
        if (indexOf.get(state) == index)
        {
            NFAState<T> s = stack.peek();
            while (!s.equals(state))
            {
                stack.pop();
                indexOf.put(s, INFINITY);
                s = stack.peek();
            }
            indexOf.put(state, INFINITY);
            stack.pop();
        }
    }

    public void dump(Appendable p) throws IOException
    {
        for (NFAState<T> s : this)
        {
            for (Set<Transition<NFAState<T>>> set : s.getTransitions())
            {
                for (Transition<NFAState<T>> t : set)
                p.append(s+"-"+t.getCondition()+">"+t.getTo()).append('\n');
            }
        }
    }

    @Override
    public String toString()
    {
        /*
        StringBuilder sb = new StringBuilder();
        Map<NFAState,Integer> map = new HashMap<NFAState,Integer>();
        Deque<NFAState> deque = new ArrayDeque<NFAState>();
        NFAState state = getFirst();
        map.put(state, 0);
        deque.add(state);
        int p = 0;
        sb.append(state.name());
        p = sb.length()-p;
        while (!deque.isEmpty())
        {
            int pos = p + map.get(state);
            state = deque.removeFirst();
            state.dump(pos, sb, map, deque);
            sb.append('\n');
        }
        return sb.toString();
        */
        StringBuilder sb = new StringBuilder();
        try
        {
            dump(sb);
            return sb.toString();
        }
        catch (IOException ex)
        {
            return ex.getMessage();
        }
        /*
        boolean first = true;
        sb.append("{");
        for (NFAState s : this)
        {
            if (first)
            {
                sb.append(s);
                first = false;
            }
            else
            {
                sb.append(","+s);
            }
        }
        sb.append("}");
        return sb.toString();
         * 
         */
    }

    @Override
    public Iterator<NFAState<T>> iterator()
    {
        return new DiGraphIterator<>(first);
    }

}
