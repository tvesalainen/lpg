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

import java.util.Iterator;
import org.vesalainen.regex.Regex;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.vesalainen.parser.util.NumMap;
import org.vesalainen.regex.Range;

/**
 * This class represents the deterministic finite automaton. Note that all states
 * included in automaton are not necessarily members of this deque.
 * @author tkv
 */
public final class DFA<T> implements Iterable<DFAState<T>>
{
    private DFAState<T> root;
    private int size;
    private boolean acceptStart;
    private DFA<T> parent;

    DFA(DFAState<T> root, int size)
    {
        this.root = root;
        this.size = size;
    }

    DFA(DFAState<T> root, int size, DFA<T> parent)
    {
        this.root = root;
        this.size = size;
        this.parent = parent;
    }
    
    public DFAState<T> getRoot()
    {
        return root;
    }
    public String name()
    {
        if (parent == null)
        {
            return "";
        }
        else
        {
            return parent.name()+root.toString();
        }
    }
    /**
     * Note that this initialSize is the initial initialSize. If changes are made to the dfa
     * the initialSize is not automatically updated.
     * @return 
     */
    public int initialSize()
    {
        return size;
    }
    
    void subtractDistributedSize(int distributedSize)
    {
        size -= distributedSize;
    }
    /**
     * Returns true if at least one of the states of this dfa accepts a range prefix.
     * For 'abcdef' returns false. For 'abcabcde' returns true.
     * @return
     */
    public boolean isAcceptStart()
    {
        return acceptStart;
    }

    /**
     * Returns true if this dfa can accept empty string.
     * @return
     */
    public boolean acceptEmpty()
    {
        return root.isAccepting();
    }
    /**
     * Removes repeated transitions with the same token. After this method call
     * the Transition.getRepeat method must be consulted in able to detect repetitions.
     * @see Transition.getRepeat()
     */
    public boolean RemoveRepeatingTransitions()
    {
        boolean removed = false;
        while (true)
        {
            if (!RemoveRepeatingTransition())
            {
                return removed;
            }
            removed = true;
        }
    }
    private boolean RemoveRepeatingTransition()
    {
        Set<DFAState<T>> set = new HashSet<>();
        for (DFAState<T> state : this)
        {
            if (state.getTransitions().size() == 1)
            {
                set.clear();
                set.add(state);
                Transition<DFAState<T>> tr = state.getTransitions().iterator().next();
                Range condition = tr.getCondition();
                int repeat = 1;
                DFAState<T> to = tr.getTo();
                while (true)
                {
                    set.add(to);
                    if (
                            !to.isAccepting() &&
                            to.getTransitions().size() == 1 &&
                            to.inStates().size() == 1
                            )
                    {
                        Transition<DFAState<T>> totr = to.getTransitions().iterator().next();
                        if (
                                condition.equals(totr.getCondition()) &&
                                !set.contains(totr.getTo())
                                )
                        {
                            repeat++;
                            to = totr.getTo();
                        }
                        else
                        {
                            break;
                        }
                    }
                    else
                    {
                        break;
                    }
                }
                if (repeat > 1)
                {
                    tr.setTo(to);
                    tr.setRepeat(repeat);
                    set.remove(state);
                    set.remove(to);
                    state.edges().removeAll(set);
                    state.edges().add(to);
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Calculates the maximum length of accepted string. Returns Integer.MAX_VALUE
     * if length is infinite. For "if|while" returns 5. For "a+" returns Integer.MAX_VALUE.
     * @return
     */
    public int maxDepth()
    {
        Map<DFAState<T>,Integer> indexOf = new NumMap<>();
        Map<DFAState<T>,Long> depth = new NumMap<>();
        Deque<DFAState<T>> stack = new ArrayDeque<>();
        maxDepth(root, indexOf, stack, depth);
        long d = depth.get(root);
        assert d >= 0;
        if (d >= Integer.MAX_VALUE)
        {
            return Integer.MAX_VALUE;
        }
        else
        {
            return (int) d;
        }
    }

    private void maxDepth(
            DFAState<T> state,
            Map<DFAState<T>,Integer> indexOf,
            Deque<DFAState<T>> stack,
            Map<DFAState<T>,Long> depth
            )
    {
        stack.push(state);
        int index = stack.size();
        indexOf.put(state, index);
        depth.put(state, 0L);

        for (Transition<DFAState<T>> t : state.getTransitions())
        {
            if (!indexOf.containsKey(t.getTo()))
            {
                maxDepth(t.getTo(),indexOf, stack, depth);
            }
            indexOf.put(state, Math.min(indexOf.get(state), indexOf.get(t.getTo())));
            if (indexOf.get(t.getTo()) != Integer.MAX_VALUE)
            {
                depth.put(state, (long)(Integer.MAX_VALUE));
            }
            else
            {
                depth.put(state, Math.max(depth.get(state), depth.get(t.getTo())+t.getCondition().getLength()));
            }
        }
        if (indexOf.get(state) == index)
        {
            DFAState<T> s = stack.peek();
            while (!s.equals(state))
            {
                stack.pop();
                indexOf.put(s, Integer.MAX_VALUE);
                depth.put(s, (long)(Integer.MAX_VALUE));
                s = stack.peek();
            }
            indexOf.put(state, Integer.MAX_VALUE);
            stack.pop();
        }
    }
    /**
     * Calculates the minimum length of accepted string.  For "if|while"
     * returns 2. For "a*" returns 0.
     * @return
     */
    public int minDepth()
    {
        Map<DFAState<T>,Integer> indexOf = new NumMap<>();
        Map<DFAState<T>,Integer> skip = new NumMap<>();
        Deque<DFAState<T>> stack = new ArrayDeque<>();
        DFAState<T> first = root;
        return minDepth(first, indexOf, stack, 0);
    }

    private int minDepth(
            DFAState<T> state,
            Map<DFAState<T>,Integer> indexOf,
            Deque<DFAState<T>> stack,
            int  depth
            )
    {
        stack.push(state);
        int index = stack.size();
        indexOf.put(state, index);
        if (state.isAccepting())
        {
            return depth;
        }
        else
        {
            int nskip = -1;
            for (Transition<DFAState<T>> t : state.getTransitions())
            {
                if (!indexOf.containsKey(t.getTo()))
                {
                    int s = minDepth(t.getTo(),indexOf, stack, depth+t.getCondition().getLength());
                    if (nskip == -1)
                    {
                        nskip = s;
                    }
                    else
                    {
                        nskip = Math.min(nskip, s);
                    }
                }
                indexOf.put(state, Math.min(indexOf.get(state), indexOf.get(t.getTo())));
            }
            if (nskip != -1)
            {
                depth = nskip;
            }
        }

        if (indexOf.get(state) == index)
        {
            DFAState<T> s = stack.peek();
            while (!s.equals(state))
            {
                stack.pop();
                indexOf.put(s, Integer.MAX_VALUE);
                s = stack.peek();
            }
            indexOf.put(state, Integer.MAX_VALUE);
            stack.pop();
        }
        return depth;
    }

    /**
     * Calculates how many characters we can skip after failed find operation
     * @return
     */
    public void calculateMaxFindSkip()
    {
        Map<DFAState<T>,Integer> indexOf = new NumMap<>();
        Map<DFAState<T>,Integer> skip = new NumMap<>();
        Deque<DFAState<T>> stack = new ArrayDeque<>();
        findSkip(root, indexOf, stack, new LinkedList());
        root.setAcceptStartLength(1);
    }

    private void findSkip(
            DFAState<T> state,
            Map<DFAState<T>,Integer> indexOf,
            Deque<DFAState<T>> stack,
            LinkedList prefix
            )
    {
        stack.push(state);
        int index = stack.size();
        indexOf.put(state, index);

        int asl = state.matchLength(prefix);
        state.setAcceptStartLength(asl);
        if (asl > 0)
        {
            acceptStart = true;
        }

        for (Transition<DFAState<T>> t : state.getTransitions())
        {
            if (!indexOf.containsKey(t.getTo()))
            {
                prefix.addLast(t.getCondition());
                findSkip(t.getTo(),indexOf, stack, prefix);
                prefix.removeLast();
            }
            indexOf.put(state, Math.min(indexOf.get(state), indexOf.get(t.getTo())));
        }

        if (indexOf.get(state) == index)
        {
            DFAState<T> s = stack.peek();
            while (!s.equals(state))
            {
                stack.pop();
                indexOf.put(s, Integer.MAX_VALUE);
                s = stack.peek();
            }
            indexOf.put(state, Integer.MAX_VALUE);
            stack.pop();
        }
    }

    @Override
    public Iterator<DFAState<T>> iterator()
    {
        return new DiGraphIterator<>(root);
    }

    public void dump(PrintStream p)
    {
        for (DFAState<T> s : this)
        {
            for (Transition<DFAState<T>> t : s.getTransitions())
            {
                p.println(stateDump(s)+"-"+t.getCondition()+">"+stateDump(t.getTo()));
            }
        }
    }

    String stateDump(DFAState<T> state)
    {
        for (DFAState<T> s : this)
        {
            if (s.equals(state))
            {
                return s.toString();
            }
        }
        throw new IllegalArgumentException(state+" not in "+this);
    }

    public static void main(String... args)
    {
        try
        {
            DFA<Integer> dfa = Regex.createDFA("aaabc");
            dfa.dump(System.err);
            dfa.calculateMaxFindSkip();
            System.err.println("skip="+dfa.isAcceptStart());
            System.err.println("max="+dfa.maxDepth());
            System.err.println("min="+dfa.minDepth());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
