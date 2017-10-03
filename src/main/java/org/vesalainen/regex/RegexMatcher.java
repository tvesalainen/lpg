/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.PrimitiveIterator.OfInt;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.vesalainen.grammar.state.DFA;
import org.vesalainen.grammar.state.DFAState;
import org.vesalainen.grammar.state.NFA;
import org.vesalainen.grammar.state.NFAState;
import org.vesalainen.grammar.state.Scope;
import org.vesalainen.regex.Regex.Option;
import org.vesalainen.util.Matcher;

/**
 * An regex implementation of matcher. 
 * <p>This implementation creates DFA in runtime and is therefore slower than
 * using compiled Regex.
 * 
 * <p>This class is not thread safe except for streams.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 * @see org.vesalainen.regex.Regex
 */
public class RegexMatcher<T> implements Matcher<T>
{
    protected RegexParserIntf<T> parser = RegexParserFactory.newInstance();
    protected Scope<NFAState<T>> nfaScope = new Scope<>("org.vesalainen.regex.RegexMatcher");
    protected NFA<T> nfa;
    protected DFA<T> dfa;
    protected DFAState<T> root;
    protected DFAState<T> state;
    protected T matched;
    /**
     * Creates RegexMatcher
     */
    public RegexMatcher()
    {
    }
    /**
     * @deprecated Use RegexMatcher() and addExpression(...
     * Creates RegexMatcher with initial expression
     * @param expr
     * @param attach
     * @param options 
     */
    public RegexMatcher(String expr, T attach, Option... options)
    {
        addExpression(expr, attach, options);
    }
    /**
     * Add expression. 
     * @param expr
     * @param attach
     * @param options 
     */
    public void addExpression(String expr, T attach, Option... options)
    {
        if (nfa == null)
        {
            nfa = parser.createNFA(nfaScope, expr, attach, options);
        }
        else
        {
            NFA<T> nfa2 = parser.createNFA(nfaScope, expr, attach, options);
            nfa = new NFA<>(nfaScope, nfa, nfa2);
        }
    }
    /**
     * Compiles expressions
     */
    public RegexMatcher compile()
    {
        if (root == null)
        {
            Scope<DFAState<T>> dfaScope = new Scope<>("org.vesalainen.regex.RegexMatcher");
            if (nfa == null)
            {
                nfa = new NFA(dfaScope);
            }
            dfa = nfa.constructDFA(dfaScope);
            state = root = dfa.getRoot();
            parser = null;
            nfaScope = null;
            nfa = null;
        }
        return this;
    }
    /**
     * Returns true if compile method is called.
     * @return 
     */
    public boolean isCompiled()
    {
        return root != null;
    }
    /**
     * Matches given text. Returns associated token if match, otherwise null.
     * @param text
     * @return 
     */
    public T match(CharSequence text)
    {
        if (root == null)
        {
            throw new IllegalStateException("not compiled");
        }
        int length = text.length();
        for (int ii=0;ii<length;ii++)
        {
            switch (match(text.charAt(ii)))
            {
                case Error:
                    return null;
                case Match:
                    return getMatched();
            }
        }
        return null;
    }
    /**
     * Matches given text as int-iterator. Returns associated token if match, otherwise null.
     * @param text
     * @return 
     */
    public T match(OfInt text)
    {
        if (root == null)
        {
            throw new IllegalStateException("not compiled");
        }
        while (text.hasNext())
        {
            switch (match(text.nextInt()))
            {
                case Error:
                    return null;
                case Match:
                    return getMatched();
            }
        }
        return null;
    }
    /**
     * Returns the match result as soon as in accepting state. Is not greedy.
     * For a* will match a from aaa.
     * @param cc
     * @return 
     * @throws java.lang.NullPointerException If not compiled
     */
    @Override
    public Status match(int cc)
    {
        state = state.transit(cc);
        if (state != null)
        {
            if (state.isAccepting())
            {
                matched = state.getToken();
                state = root;
                return Status.Match;
            }
            else
            {
                return Status.Ok;
            }
        }
        else
        {
            state = root;
            return Status.Error;
        }
    }
    /**
     * Returns attachment for last matched expression.
     * @return 
     */
    @Override
    public T getMatched()
    {
        return matched;
    }

    @Override
    public void clear()
    {
        state = root;
    }
    /**
     * Returns stream that contains subsequences delimited by this regex.
     * <p>Stream is safe to use same regex from different thread.
     * @param seq
     * @return 
     */
    public Stream<CharSequence> split(CharSequence seq)
    {
        return StreamSupport.stream(new SpliteratorImpl(seq, this), false);
    }
    /**
     * Returns stream that contains subsequences delimited by given regex
     * @param seq
     * @param regex
     * @param options
     * @return 
     */
    public static Stream<CharSequence> split(CharSequence seq, String regex, Option... options)
    {
        return StreamSupport.stream(new SpliteratorImpl(seq, regex, options), false);
    }
    /**
     * Returns stream that contains subsequences delimited by given matcher
     * <p>Stream is safe to use same regex from different thread.
     * @param seq
     * @param matcher
     * @return 
     */
    public static Stream<CharSequence> split(CharSequence seq, RegexMatcher matcher)
    {
        return StreamSupport.stream(new SpliteratorImpl(seq, matcher), false);
    }
    private static class SpliteratorImpl implements Spliterator<CharSequence>
    {
        private CharSequence seq;
        private int length;
        private DFAState<String> root;
        private DFAState<String> state;
        private int start;
        private int end;

        public SpliteratorImpl(CharSequence seq, String regex, Option... options)
        {
            this(seq, new RegexMatcher(regex, "token", options));
        }

        public SpliteratorImpl(CharSequence seq, RegexMatcher matcher)
        {
            this.seq = seq;
            this.length = seq.length();
            if (!matcher.isCompiled())
            {
                matcher.compile();
            }
            root = state = matcher.root;
        }
        
        @Override
        public boolean tryAdvance(Consumer<? super CharSequence> action)
        {
            while (start < length && delim(seq.charAt(start)))
            {
                start++;
            }
            if (start + 1 >= length)
            {
                return false;
            }
            end = start+1;
            while (end < length && !delim(seq.charAt(end)))
            {
                end++;
            }
            action.accept(seq.subSequence(start, end));
            start = end+1;
            return true;
        }

        public boolean delim(int cc)
        {
            state = state.transit(cc);
            if (state != null)
            {
                return true;
            }
            else
            {
                state = root;
                return false;
            }
        }
        @Override
        public Spliterator<CharSequence> trySplit()
        {
            return null;
        }

        @Override
        public long estimateSize()
        {
            return 1;
        }

        @Override
        public int characteristics()
        {
            return 0;
        }
        
    }
}
