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

/**
 *
 * @author tkv
 */
public class StackElement
{
    private StackElement previous;
    private StackElement next;
    private StackElement link;
    private Lr0State state;
    private int size = 1;

    public StackElement(Lr0State state)
    {
        this.state = state;
    }

    public StackElement(Lr0State state, StackElement previous)
    {
        this.state = state;
        this.size = previous.size + 1;
        this.previous = previous;
    }

    public StackElement getNext()
    {
        return next;
    }

    public StackElement getPrevious()
    {
        return previous;
    }

    public int getSize()
    {
        return size;
    }

    public Lr0State getState()
    {
        return state;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass().equals(obj.getClass()))
        {
            StackElement oth = (StackElement) obj;
            if (state.equals(oth.getState()))
            {
                if (previous == null)
                {
                    return oth.previous == null;
                }
                else
                {
                    return previous.equals(oth.previous);
                }
            }
        }
        return false;
    }


    /***************************************************************************/
    /* This function takes as argument two pointers to sorted lists of stacks. */
    /* It merges the lists in the proper order and returns the resulting list. */
    /***************************************************************************/
    public static StackElement unionConfigSets(StackElement root1, StackElement root2)
    {
        StackElement p1,
                         p2,
                         root = null,
                         tail;

        /*******************************************************************/
        /* This loop iterates over both lists until one (or both) has been */
        /* completely processed. Each time around the loop, a stack is     */
        /* removed from one of the lists and possibly added to the new     */
        /* list. The new list is initially kept as a circular list to      */
        /* preserve the sorted ordering in which elements are added to it. */
        /*******************************************************************/
        while (root1 != null && root2 != null)
        {
            /***************************************************************/
            /* Compare the two stacks in front of the lists for equality.  */
            /* We exit this loop when we encounter the end of one (or both)*/
            /* of the stacks or two elements in them that are not the same.*/
            /***************************************************************/
            for (p1 = root1, p2 = root2;
                    p1 != null && p2 != null;
                    p1 = p1.previous, p2 = p2.previous)
            {
                if (p1.state != p2.state)
                {
                    break;
                }
            }

            /***************************************************************/
            /* We now have 3 cases to consider:                            */
            /*    1. The two stacks are equal? Discard one!                */
            /*    2. List 1 stack is prefix of list 2 stack (p1 == null)?  */
            /*       or list 1 stack is less than list 2 stack?            */
            /*       Remove list 1 stack and add it to new list.           */
            /*    3. List 2 stack is either a prefix of list 1 stack, or   */
            /*       it is smaller!                                        */
            /*       Remove list 2 stack and add it to new list.           */
            /***************************************************************/
            if (p1 == p2) /* are both p1 and p2 null? */

            {
                p2 = root2;
                root2 = root2.next;
            }
            else if ((p1 == null)
                    || ((p2 != null) && (p1.state.getNumber() < p2.state.getNumber())))
            {
                p1 = root1;
                root1 = root1.next;

                if (root == null)
                {
                    p1.next = p1;
                }
                else
                {
                    p1.next = root.next;
                    root.next = p1;
                }
                root = p1;
            }
            else
            {
                p2 = root2;
                root2 = root2.next;

                if (root == null)
                {
                    p2.next = p2;
                }
                else
                {
                    p2.next = root.next;
                    root.next = p2;
                }
                root = p2;
            }
        }

        /*******************************************************************/
        /* At this stage, at least one (or both) list has been expended    */
        /* (or was empty to start with).                                   */
        /* If the new list is not empty, turn it into a linear list and    */
        /* append the unexpended list to it, if any.                       */
        /* Otherwise, set the new list to the nonempty list if any!        */
        /*******************************************************************/
        if (root != null)
        {
            tail = root;
            root = root.next;
            tail.next = (root1 == null ? root2 : root1);
        }
        else
        {
            root = (root1 == null ? root2 : root1);
        }

        return root;

    }
}
