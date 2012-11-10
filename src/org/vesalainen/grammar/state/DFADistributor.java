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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.vesalainen.parser.util.NumMap;
import org.vesalainen.parser.util.VisitSet;
import org.vesalainen.regex.Regex;

/**
 * @author Timo Vesalainen
 */
public class DFADistributor<T> extends DiGraph<DFAState<T>>
{
    private int maxStates;
    private DFA<T> dfa;
    private List<Candidate> candidateList = new ArrayList<>();
    private Map<DFAState<T>,Candidate> candidateMap = new NumMap<>();
    private List<DFA<T>> distributedDFAs = new ArrayList<>();
    private VisitSet<DFAState<T>> incomingSet = new VisitSet<>();
    private VisitSet<DFAState<T>> closure = new VisitSet<>();
    

    public DFADistributor(DFA<T> dfa, int maxStates)
    {
        this.dfa = dfa;
        this.maxStates = maxStates;
    }

    public void distribute()
    {
        distribute(dfa.getRoot(), dfa.initialSize());
    }
    private void distribute(DFAState<T> state, int rootCount)
    {
        CandidateComparator comp = new CandidateComparator(rootCount - maxStates);
        List<Candidate> firstLevel = new ArrayList<>();
        for (DFAState<T> s : state.edges())
        {
            candidateList.clear();
            candidateMap.clear();
            incomingSet.clear();
            closure.clear();
            reset();
            traverse(s);
            Collections.sort(candidateList, comp);
            if (!candidateList.isEmpty())
            {
                firstLevel.add(candidateList.get(0));
            }
        }
        int distCount = 0;
        Collections.sort(firstLevel, comp);
        for (Candidate c : firstLevel)
        {
            distCount += c.getCount();
            c.getState().setDistributed(true);
            DFA<T> ndfa = new DFA<>(c.getState(), c.getCount(), dfa);
            distributedDFAs.add(ndfa);
            if (rootCount - distCount < maxStates)
            {
                break;
            }
        }
        if (rootCount - distCount > maxStates)
        {
            throw new IllegalArgumentException("DFA too big to fit in java method");
        }
        dfa.subtractDistributedSize(distCount);
        List<DFA<T>> redistributeList = new ArrayList<>();
        Iterator<DFA<T>> it = distributedDFAs.iterator();
        while (it.hasNext())
        {
            DFA<T> ddfa = it.next();
            if (ddfa.initialSize() > maxStates)
            {
                redistributeList.add(ddfa);
            }
        }
        for (DFA<T> ddfa : redistributeList)
        {
            DFADistributor<T> dfaDistributor = new DFADistributor<>(ddfa, maxStates);
            dfaDistributor.distribute();
            distributedDFAs.addAll(dfaDistributor.distributedDFAs);
        }
    }

    public List<DFA<T>> getDistributedDFAs()
    {
        return distributedDFAs;
    }

    @Override
    protected void enter(DFAState<T> x)
    {
        closure.add(x);
        incomingSet.addAll(x.inStates());
    }

    @Override
    protected void branch(DFAState<T> x)
    {
        addCandidate(x);
    }

    @Override
    protected void exit(DFAState<T> x, int count)
    {
        Candidate c = getCandidate(x);
        if (c != null)
        {
            VisitSet<DFAState<T>> complement = incomingSet.complement(closure);
            if (complement.size() == 1)
            {
                c.setCount(count);
            }
        }
    }
    
    private void addCandidate(DFAState<T> state)
    {
        Candidate c = new Candidate(state);
        candidateList.add(c);
        candidateMap.put(state, c);
    }
    private Candidate getCandidate(DFAState<T> state)
    {
        return candidateMap.get(state);
    }
    
    private class Candidate
    {
        private DFAState<T> state;
        private int count;

        public Candidate(DFAState<T> state)
        {
            this.state = state;
        }

        public int getCount()
        {
            return count;
        }

        public void setCount(int count)
        {
            this.count = count;
        }

        public DFAState<T> getState()
        {
            return state;
        }

        public void setState(DFAState<T> state)
        {
            this.state = state;
        }

        public String toString()
        {
            return "Candidate{" + "state=" + state + ", count=" + count + '}';
        }
        
    }
    private class CandidateComparator implements Comparator<Candidate>
    {
        private int opt;

        public CandidateComparator(int opt)
        {
            this.opt = opt;
        }
        
        @Override
        public int compare(Candidate o1, Candidate o2)
        {
            if (o1.count > opt && o2.count > opt)
            {
                return o1.count - o2.count;
            }
            else
            {
                return o2.count - o1.count;
            }
        }
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)    
    {
        try
        {
            DFA<Integer> dfa = Regex.createDFA("qwertyuio|asdfghjkl|zxcvbnm|[qwe]+");
            DFADistributor<Integer> distributor = new DFADistributor<>(dfa, 10);
            distributor.distribute();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
