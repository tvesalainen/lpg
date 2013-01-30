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
import java.lang.reflect.Member;
import java.util.Iterator;
import org.vesalainen.bcc.MethodImplementor;
import org.vesalainen.bcc.type.MethodWrapper;
import org.vesalainen.bcc.SubClass;
import org.vesalainen.bcc.type.Generics;
import org.vesalainen.grammar.state.DFADistributor;

/**
 * A byte code compiler base class for generating methods using DFA.
 * 
 * Method return either token from accepting DFA state, errorToken if syntax error
 * or eofToken if eof.
 * @author tkv
 */
public abstract class DFACompiler<T> implements MethodImplementor
{
    public static int MAXSTATES = 2000;
    public static int MAXSTATESUSEWIDE = 500;
    public static int dfaCount;
    public static int byteCount;
    protected DFA<T> dfa;
    protected MethodCompiler c;
    protected T errorToken;
    protected T eofToken;
    protected Class<?> tokenClass;
    protected MethodWrapper method;
    protected boolean subCompiler;
    protected boolean repeats;

    public DFACompiler(DFA<T> dfa, T errorToken, T eofToken)
    {
        this.dfa = dfa;
        this.errorToken = errorToken;
        this.eofToken = eofToken;
        if (String.class.equals(errorToken.getClass()))
        {
            this.tokenClass = String.class;
        }
        else
        {
            this.tokenClass = (Class<?>) Generics.getPrimitiveType(errorToken.getClass());
        }
        if (!Generics.isConstantClass(tokenClass))
        {
            throw new IllegalArgumentException(tokenClass+" is not java constant class");
        }
        int count = 0;
        for (DFAState<T> state : dfa)
        {
            if (
                eofToken.equals(state.getToken()) ||
                errorToken.equals(state.getToken())
                )
            {
                throw new IllegalArgumentException(state.getToken()+" token is also error or eof token");
            }
            count++;
        }
    }

    @Override
    public void implement(MethodCompiler c, Member member) throws IOException
    {
        this.c = c;
        this.method = MethodWrapper.wrap(member);
        if (
            !c.getReturnType().equals(tokenClass) &&
            !Generics.getPrimitiveType(tokenClass).equals(c.getReturnType())
            )
        {
            throw new IllegalArgumentException(tokenClass+" is not return type of method");
        }
        if (dfa.initialSize() > MAXSTATES)
        {
            SubClass subClass = c.getSubClass();
            DFADistributor<T> dd = new DFADistributor<>(dfa, MAXSTATES);
            dd.distribute();
            for (DFA<T> ddfa : dd.getDistributedDFAs())
            {
                String subName = MethodWrapper.makeJavaIdentifier(c.getMethodName()+ddfa.name());
                MethodWrapper mw = new MethodWrapper(
                        method.getModifiers(), 
                        method.getDeclaringType(), 
                        subName, 
                        method.getReturnType(), 
                        method.getParameterTypes());
                DFACompiler<T> sc = copy(ddfa);
                sc.setSubCompiler(true);
                mw.setImplementor(sc);
                subClass.implement(mw);
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

    protected void compile() throws IOException, NoSuchMethodException
    {
        if (dfa.initialSize() > MAXSTATESUSEWIDE)
        {
            c.setWideIndex(true);
        }
        c.nameArgument("reader", 1);
        c.addVariable("cc", int.class);
        c.addVariable("accepted", tokenClass);
        //if (repeats)
        {
            c.addVariable("index", int.class);
        }
        c.fixAddress("start");
        c.iconst(-1);
        c.tstore("cc");
        c.tconst(errorToken);
        c.tstore("accepted");
        int count = 0;
        Iterator<DFAState<T>> si = dfa.iterator();
        while (si.hasNext())
        {
            DFAState s = si.next();
            compile(s, !si.hasNext());
            count++;
        }
        c.fixAddress("error");
        error();
        c.fixAddress("pushback");
        pushback();
        c.fixAddress("exit");
        exit();
        c.fixAddress("eof");
        eof();
        dfaCount += dfa.initialSize();
        byteCount += c.position();
        c.end();
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
        c.fixAddress(s.toString());
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
                c.tload("reader");
                c.iconst(range.getBoundaryMatcher());
                c.invokevirtual(InputReader.class.getMethod("isAtBoundary", int.class));
                c.ifeq("error");
                c.goto_n(to.toString());
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
                    c.tconst(first.getRepeat());
                    c.tstore("index");
                    String back = s.toString()+"-repeat";
                    c.fixAddress(back);
                    c.tload("reader");
                    c.invokevirtual(InputReader.class.getMethod("read"));
                    c.tstore("cc");
                    c.tload("cc");
                    c.iflt("eof");
                    Range range = first.getCondition();
                    DFAState to = s.transit(range);
                    compile(range, "error", !ti.hasNext());
                    // ok
                    c.tinc("index", -1);
                    c.tload("index");
                    c.ifne(back);
                    gotoNext(to);
                }
                else
                {
                    c.tload("reader");
                    c.invokevirtual(InputReader.class.getMethod("read"));
                    c.tstore("cc");
                    c.tload("cc");
                    c.iflt("eof");

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
                            c.fixAddress(next);
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
                        c.tload("cc");
                        c.optimizedSwitch("error", ll);
                        for (Transition tr : s.getTransitions())
                        {
                            Range range = tr.getCondition();
                            DFAState<T> to = s.transit(range);
                            String target = s.toString()+"-"+range+">"+to.toString();
                            c.fixAddress(target);
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
                    c.goto_n("exit");
                }
                else
                {
                    c.goto_n("error");
                }
            }
        }
    }

    protected void gotoNext(DFAState<T> s) throws IOException, NoSuchMethodException
    {
        if (s.isDistributed())
        {
            String subName = MethodWrapper.makeJavaIdentifier(c.getMethodName()+s);
            MethodWrapper mw = new MethodWrapper(
                    method.getModifiers(), 
                    method.getDeclaringType(), 
                    subName, 
                    method.getReturnType(), 
                    method.getParameterTypes());
            c.tload("this");
            c.tload("reader");
            c.invoke(mw);
            c.treturn();
        }
        else
        {
            c.goto_n(s.toString());
        }
    }
    protected void accepting(DFAState<T> s) throws IOException, NoSuchMethodException
    {
        if (s.isAccepting())
        {
            c.tconst(s.getToken());
            c.tstore("accepted");
        }
        else
        {
            c.tconst(errorToken);
            c.tstore("accepted");
        }
    }

    protected void compile(Range range, String next, boolean isLast) throws IOException
    {
        if ((range.getTo() - range.getFrom()) == 1)
        {
            c.tload("cc");
            c.iconst(range.getFrom());
            if (isLast)
            {
                c.if_icmpne("error");
            }
            else
            {
                c.if_icmpne(next);
            }
        }
        else
        {
            if (range.getFrom() >= -1)
            {
                c.tload("cc");
                c.iconst(range.getFrom());
                if (isLast)
                {
                    c.if_icmplt("error");
                }
                else
                {
                    c.if_icmplt(next);
                }
            }
            if (range.getTo() < Integer.MAX_VALUE)
            {
                c.tload("cc");
                c.iconst(range.getTo());
                if (isLast)
                {
                    c.if_icmpge("error");
                }
                else
                {
                    c.if_icmpge(next);
                }
            }
        }
    }

    private void eof() throws IOException
    {
        c.tload("accepted");
        c.tconst(errorToken);
        c.if_tcmpne(tokenClass, "eofacc");
        c.tconst(eofToken);
        c.treturn();
        c.fixAddress("eofacc");
        c.tload("accepted");
        c.treturn();
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

