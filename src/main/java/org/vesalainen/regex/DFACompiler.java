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
package org.vesalainen.regex;

import org.vesalainen.grammar.state.Transition;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.grammar.state.DFAState;
import org.vesalainen.grammar.state.DFA;
import org.vesalainen.bcc.LookupList;
import org.vesalainen.bcc.MethodCompiler;
import java.io.IOException;
import java.util.Iterator;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.SubClass;
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.Jav;
import org.vesalainen.bcc.model.Typ;
import org.vesalainen.bcc.model.UpdateableElement;
import org.vesalainen.grammar.state.DFADistributor;

/**
 * A byte code compiler base class for generating methods using DFA.
 * 
 * Method return either token from accepting DFA state, errorToken if syntax error
 * or eofToken if eof.
 * @author tkv
 */
public abstract class DFACompiler<T> extends MethodCompiler
{
    public static int MAXSTATES = 2000;
    public static int MAXSTATESUSEWIDE = 500;
    public static int dfaCount;
    public static int byteCount;
    protected DFA<T> dfa;
    protected T errorToken;
    protected T eofToken;
    protected TypeMirror tokenType;
    protected boolean subCompiler;
    protected boolean repeats;
    private final Jav jav = new Jav();

    public DFACompiler(DFA<T> dfa, T errorToken, T eofToken)
    {
        this.dfa = dfa;
        this.errorToken = errorToken;
        this.eofToken = eofToken;
        TypeMirror errorType = Typ.getTypeFor(errorToken.getClass());
        if (Typ.isSameType(Typ.String, errorType))
        {
            this.tokenType = Typ.String;
        }
        else
        {
            this.tokenType = Typ.unboxedType(errorType);
        }
        if (!Typ.isJavaConstantType(tokenType))
        {
            throw new IllegalArgumentException(tokenType+" is not java constant class");
        }
        for (DFAState<T> state : dfa)
        {
            if (
                eofToken.equals(state.getToken()) ||
                errorToken.equals(state.getToken())
                )
            {
                throw new IllegalArgumentException(state.getToken()+" token is also error or eof token");
            }
        }
    }

    @Override
    public void implement() throws IOException
    {
        if (!Typ.isSameType(tokenType, getReturnType()))
        {
            throw new IllegalArgumentException(tokenType+" is not expected return type "+getReturnType()+" of method");
        }
        if (dfa.initialSize() > MAXSTATES)
        {
            SubClass subClass = getSubClass();
            DFADistributor<T> dd = new DFADistributor<>(dfa, MAXSTATES);
            dd.distribute();
            for (DFA<T> ddfa : dd.getDistributedDFAs())
            {
                String subName = jav.makeJavaIdentifier(getMethodDescription()+ddfa.name());
                ExecutableElement distributedMethod = getDistributedMethod(subName);
                DFACompiler<T> sc = copy(ddfa);
                sc.setSubCompiler(true);
                subClass.defineMethod(sc, distributedMethod);
            }
        }
        try
        {
            compile();
        }
        catch (NoSuchMethodException ex)
        {
            throw new IOException(ex);
        }
    }

    protected ExecutableElement getDistributedMethod(String newName)
    {
        ExecutableElement distributedMethod = El.createUpdateableElement(executableElement);
        UpdateableElement ue = (UpdateableElement) distributedMethod;
        ue.setSimpleName(El.getName(newName));
        return distributedMethod;
    }
    protected void compile() throws IOException, NoSuchMethodException
    {
        if (dfa.initialSize() > MAXSTATESUSEWIDE)
        {
            setWideIndex(true);
        }
        nameArgument("reader", 1);
        addVariable("cc", int.class);
        addVariable("accepted", tokenType);
        //if (repeats)
        {
            addVariable("index", int.class);
        }
        fixAddress("start");
        iconst(-1);
        tstore("cc");
        tconst(errorToken);
        tstore("accepted");
        int count = 0;
        Iterator<DFAState<T>> si = dfa.iterator();
        while (si.hasNext())
        {
            DFAState s = si.next();
            compile(s, !si.hasNext());
            count++;
        }
        fixAddress("error");
        error();
        fixAddress("pushback");
        pushback();
        fixAddress("exit");
        exit();
        fixAddress("eof");
        eof();
        dfaCount += dfa.initialSize();
        byteCount += position();
    }

    protected abstract void error() throws IOException, NoSuchMethodException;
    protected void pushback() throws IOException, NoSuchMethodException
    {

    }
    
    protected abstract void exit() throws IOException, NoSuchMethodException;

    protected void afterState(DFAState<T> s) throws IOException, NoSuchMethodException
    {
    }

    protected void compile(DFAState<T> s, boolean isLast) throws IOException, NoSuchMethodException
    {
        fixAddress(s.toString());
        accepting(s);
        if (s.hasBoundaryMatches())
        {
            // boundary match is the only transition
            if (s.getTransitions().size() != 1)
            {
                throw new IllegalArgumentException("number of boundary match transitions not 1. -> illegal usage of boundary match");
            }
            for (Transition tr : s.getTransitions())
            {
                Range range = tr.getCondition();
                DFAState to = s.transit(range);
                tload("reader");
                iconst(range.getBoundaryMatcher());
                invokevirtual(InputReader.class, "isAtBoundary", int.class);
                ifeq("error");
                goto_n(to.toString());
            }
        }
        else
        {
            Iterator<Transition<DFAState<T>>> ti = s.getTransitions().iterator();
            if (ti.hasNext())
            {
                Transition<DFAState<T>> first = ti.next();
                if (first.getRepeat() > 1)
                {
                    tconst(first.getRepeat());
                    tstore("index");
                    String back = s.toString()+"-repeat";
                    fixAddress(back);
                    tload("reader");
                    invokevirtual(InputReader.class, "read");
                    tstore("cc");
                    tload("cc");
                    iflt("eof");
                    Range range = first.getCondition();
                    DFAState to = s.transit(range);
                    compile(range, "error", !ti.hasNext());
                    // ok
                    tinc("index", -1);
                    tload("index");
                    ifne(back);
                    gotoNext(to);
                }
                else
                {
                    tload("reader");
                    invokevirtual(InputReader.class, "read");
                    tstore("cc");
                    tload("cc");
                    iflt("eof");

                    if (s.getTransitionSelectivity() > 2)
                    {
                        Iterator<Transition<DFAState<T>>> tri = s.getTransitions().iterator();
                        while (tri.hasNext())
                        {
                            Transition tr = tri.next();
                            Range range = tr.getCondition();
                            DFAState to = s.transit(range);
                            String next = s.toString()+"-"+range+">"+to.toString();
                            compile(range, next, !tri.hasNext());
                            // ok
                            afterState(s);
                            gotoNext(to);
                            fixAddress(next);
                        }
                    }
                    else
                    {
                        LookupList ll = new LookupList();
                        for (Transition tr : s.getTransitions())
                        {
                            Range range = tr.getCondition();
                            if (range.getFrom() >= 0)
                            {
                                DFAState to = s.transit(range);
                                String target = s.toString()+"-"+range+">"+to.toString();
                                for (int ii=range.getFrom();ii <range.getTo();ii++)
                                {
                                    ll.addLookup(ii, target);
                                }
                            }
                        }
                        tload("cc");
                        optimizedSwitch("error", ll);
                        for (Transition tr : s.getTransitions())
                        {
                            Range range = tr.getCondition();
                            DFAState<T> to = s.transit(range);
                            String target = s.toString()+"-"+range+">"+to.toString();
                            fixAddress(target);
                            // ok
                            afterState(s);
                            gotoNext(to);
                        }
                    }
                }
            }
            else
            {
                if (s.isAccepting())
                {
                    goto_n("exit");
                }
                else
                {
                    goto_n("error");
                }
            }
        }
    }

    protected void gotoNext(DFAState<T> s) throws IOException, NoSuchMethodException
    {
        if (s.isDistributed())
        {
            String subName = jav.makeJavaIdentifier(getMethodDescription()+s);
            ExecutableElement distributedMethod = getDistributedMethod(subName);
            tload("this");
            tload("reader");
            invoke(distributedMethod);
            treturn();
        }
        else
        {
            goto_n(s.toString());
        }
    }
    protected void accepting(DFAState<T> s) throws IOException, NoSuchMethodException
    {
        if (s.isAccepting())
        {
            tconst(s.getToken());
            tstore("accepted");
        }
        else
        {
            tconst(errorToken);
            tstore("accepted");
        }
    }

    protected void compile(Range range, String next, boolean isLast) throws IOException
    {
        if ((range.getTo() - range.getFrom()) == 1)
        {
            tload("cc");
            iconst(range.getFrom());
            if (isLast)
            {
                if_icmpne("error");
            }
            else
            {
                if_icmpne(next);
            }
        }
        else
        {
            if (range.getFrom() >= -1)
            {
                tload("cc");
                iconst(range.getFrom());
                if (isLast)
                {
                    if_icmplt("error");
                }
                else
                {
                    if_icmplt(next);
                }
            }
            if (range.getTo() < Integer.MAX_VALUE)
            {
                tload("cc");
                iconst(range.getTo());
                if (isLast)
                {
                    if_icmpge("error");
                }
                else
                {
                    if_icmpge(next);
                }
            }
        }
    }

    private void eof() throws IOException
    {
        tload("accepted");
        tconst(errorToken);
        if_tcmpne(tokenType, "eofacc");
        tconst(eofToken);
        treturn();
        fixAddress("eofacc");
        tload("accepted");
        treturn();
    }

    protected boolean literal(DFAState<T> s) throws IOException
    {
        return false;
    }

    protected abstract DFACompiler<T> copy(DFA<T> ddfa);

    public void setSubCompiler(boolean subCompiler)
    {
        this.subCompiler = subCompiler;
    }

}

