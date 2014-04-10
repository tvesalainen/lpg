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
package org.vesalainen.grammar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.lpg.Item;
import org.vesalainen.parser.util.HtmlPrinter;
import org.vesalainen.parser.util.NumSet;

/**
 *
 * @author tkv
 */
public class Nonterminal extends Symbol
{
    private static final SyntheticBnfParserIntf bnfParser = SyntheticBnfParserFactory.newInstance();
    protected String name;
    private Set<GTerminal> firstSet = new NumSet<>(); // nt_first
    private Set<Nonterminal> closure = new NumSet<>();    // closure
    private boolean nullable;   // nullNt
    private List<Item> firstItems = new ArrayList<>();  // cl_items
    private boolean rmpSelf;
    private Set<Nonterminal> produces = new NumSet<>();
    private Set<Nonterminal> directProduces = new NumSet<>();
    private List<GRule> lhsRule = new ArrayList<>();
    private int stackSize;

    Nonterminal(int number, String name)
    {
        super(number);
        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public boolean hasNonVoidReducer()
    {
        for (GRule rule : lhsRule)
        {
            if (void.class.equals(rule.getReducerType()))
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        return false;
    }

    public int getStackSize()
    {
        return stackSize;
    }

    public void setStackSize(int stackSize)
    {
        this.stackSize = stackSize;
    }

    public void addLhsRule(GRule rule)
    {
        lhsRule.add(rule);
    }
    
    public List<GRule> getLhsRule()
    {
        return lhsRule;
    }

    public void addProduces(Nonterminal symbol)
    {
        produces.add(symbol);
    }

    public void removeProduces(Nonterminal symbol)
    {
        produces.remove(symbol);
    }

    public void addProduces(Collection<Nonterminal> symbols)
    {
        produces.addAll(symbols);
    }

    public void setProduces(Collection<Nonterminal> symbols)
    {
        produces.clear();
        produces.addAll(symbols);
    }

    public Set<Nonterminal> getProduces()
    {
        return produces;
    }

    public void addDirectProduces(Nonterminal symbol)
    {
        directProduces.add(symbol);
    }

    public void addDirectProduces(Collection<Nonterminal> symbols)
    {
        directProduces.addAll(symbols);
    }

    public Set<Nonterminal> getDirectProduces()
    {
        return directProduces;
    }

    public boolean isRmpSelf()
    {
        return rmpSelf;
    }

    public void setRmpSelf(boolean rmpSelf)
    {
        this.rmpSelf = rmpSelf;
    }

    public void addFirstItem(Item item)
    {
        firstItems.add(item);
    }
    
    public List<Item> getFirstItems()
    {
        return firstItems;
    }


    public boolean isNullable()
    {
        return nullable;
    }

    public void setNullable(boolean nullable)
    {
        this.nullable = nullable;
    }

    public void addClosure(Nonterminal nt)
    {
        closure.add(nt);
    }

    public void AddClosure(Collection<Nonterminal> nts)
    {
        closure.addAll(nts);
    }

    public Set<Nonterminal> getClosure()
    {
        return closure;
    }

    public void addFirst(GTerminal symbol)
    {
        firstSet.add(symbol);
    }
    public void addFirst(Collection<GTerminal> symbols)
    {
        firstSet.addAll(symbols);
    }
    public Set<GTerminal> getFirstSet()
    {
        return firstSet;
    }

    @Override
    public String toString()
    {
        return bnfParser.parse(name)
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                ;    //+"("+number+")";
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
        final Nonterminal other = (Nonterminal) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean isStart()
    {
        return false;
    }
    @Override
    public boolean isNil()
    {
        return false;
    }
    @Override
    public boolean isOmega()
    {
        return false;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public boolean isEof()
    {
        return false;
    }

    @Override
    public boolean isError()
    {
        return false;
    }

    @Override
    public ExecutableElement getReducer()
    {
        return lhsRule.get(0).getReducer();
    }

    @Override
    public TypeMirror getReducerType()
    {
        return lhsRule.get(0).getReducerType();
    }

    @Override
    public void print(HtmlPrinter p) throws IOException
    {
        String s = toString();
        p.linkSource("#"+s, s);
    }

    @Override
    public void print(Appendable p) throws IOException
    {
        p.append(toString());
    }

}
