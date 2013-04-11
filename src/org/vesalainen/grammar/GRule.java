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
import java.io.StringWriter;
import org.vesalainen.parser.util.PeekableIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.Typ;
import org.vesalainen.lpg.Action;
import org.vesalainen.lpg.Item;
import org.vesalainen.parser.util.HtmlPrinter;
import org.vesalainen.parser.util.NumSet;
import org.vesalainen.parser.util.Numerable;

/**
 *
 * @author tkv
 */
public class GRule implements Action, Comparable<GRule>, Numerable
{
    private int number;
    private Nonterminal left;
    private List<Symbol> right;
    private List<Item> itemList = new ArrayList<>();
    private Set<Item> adequateItem = new NumSet<>();
    private ExecutableElement reducer;
    private int originalNumber;
    private boolean synthetic;
    private final String document;

    protected GRule(GRule oth)
    {
        number = oth.number;
        left = oth.left;
        right = oth.right;
        itemList = oth.itemList;
        adequateItem = oth.adequateItem;
        reducer = oth.reducer;
        synthetic = oth.synthetic;
        document = oth.document;
    }

    GRule(Nonterminal left, List<Symbol> right, boolean synthetic, String document)
    {
        this.left = left;
        this.right = right;
        this.synthetic = synthetic;
        this.document = document;
    }

    void setNumber(int number)
    {
        this.number = number;
    }

    public boolean isAccepting()
    {
        return left.isStart();
    }

    public TypeMirror getReducerType()
    {
        if (reducer == null)
        {
            return Typ.Void;
        }
        else
        {
            return reducer.getReturnType();
        }
    }

    public Set<Item> getAdequateItem()
    {
        return adequateItem;
    }

    public PeekableIterator<Item> getAdequateItemPtr()
    {
        return new PeekableIterator(adequateItem.iterator());
    }

    public void setAdequateItem()
    {
        assert adequateItem.isEmpty();
        adequateItem.add(itemList.get(itemList.size()-1));
    }
    public void addItem(Item item)
    {
        itemList.add(item);
    }

    public Item getItem(int index)
    {
        return itemList.get(index);
    }

    public Item predessor(Item item)
    {
        return predessor(item, 1);
    }

    public Item predessor(Item item, int distance)
    {
        return itemList.get(item.getDot() - distance);
    }

    public Item next(Item item)
    {
        return itemList.get(item.getDot() + 1);
    }


    @Override
    public int getNumber()
    {
        return number;
    }

    public ExecutableElement getReducer()
    {
        return reducer;
    }

    public void setReducer(ExecutableElement reducer)
    {
        this.reducer = reducer;
    }

    public Symbol firstSymbol()
    {
        if (right.isEmpty())
        {
            return null;
        }
        else
        {
            return right.get(0);
        }
    }

    public Nonterminal getLeft()
    {
        return left;
    }

    public List<Symbol> getRight()
    {
        return right;
    }

    public Symbol getFirstRight()
    {
        if (right.isEmpty())
        {
            return null;
        }
        else
        {
            return right.get(0);
        }
    }
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (obj instanceof GRule)
        {
            final GRule other = (GRule) obj;
            if (this.left != other.left && (this.left == null || !this.left.equals(other.left)))
            {
                return false;
            }
            if (this.right != other.right && (this.right == null || !this.right.equals(other.right)))
            {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return number;
    }

    @Override
    public String toString()
    {
        return "Rule "+number;
    }

    public String getDescription()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(number).append(": ");
        sb.append(left);
        sb.append(" ::=");
        for (Symbol r : right)
        {
            sb.append(" ");
            sb.append(r.toString());
        }
        if (reducer != null)
        {
            sb.append(" reducer ");
            sb.append(reducer.toString());
        }
        return sb.toString();
    }

    public int compareTo(GRule o)
    {
        return number - o.number;
    }

    void setOriginalNumber(int number)
    {
        originalNumber = number;
    }

    public int getOriginalNumber()
    {
        return originalNumber;
    }

    public boolean isSynthetic()
    {
        return synthetic;
    }

    public void print(HtmlPrinter p) throws IOException
    {
        if (!synthetic)
        {
            p.p();
            p.linkDestination("rule"+number);
            if (reducer != null)
            {
                p.linkSource(makeRef(reducer, p.getLevel()), number+": "+left.toString());
            }
            else
            {
                p.print(left);
            }
            p.print(" ::=");
            for (Symbol symbol : right)
            {
                p.print(" ");
                symbol.print(p);
            }
        }
    }

    private String makeRef(ExecutableElement reducer, int level)
    {
        StringWriter sb = new StringWriter();
        for (int ii=0;ii<level;ii++)
        {
            sb.append("../");
        }
        TypeElement te = (TypeElement) reducer.getEnclosingElement();
        sb.append(te.getQualifiedName().toString().replace('.', '/'));
        sb.append(".html#");
        sb.append(reducer.getSimpleName());
        sb.append("(");
        boolean f = true;
        for (VariableElement param : reducer.getParameters())
        {
            if (!f)
            {
                sb.append(", ");
            }
            f = false;
            El.printElements(sb, param);
        }
        sb.append(")");
        return sb.toString();
    }

    public String getDocument()
    {
        return document;
    }

}
