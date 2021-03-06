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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import javax.lang.model.element.Modifier;
import org.vesalainen.bcc.FieldInitializer;
import org.vesalainen.bcc.SubClass;
import org.vesalainen.bcc.model.El;
import org.vesalainen.grammar.state.DFA;
import org.vesalainen.grammar.state.DFAState;
import org.vesalainen.grammar.state.NFA;
import org.vesalainen.grammar.state.NFAState;
import org.vesalainen.grammar.state.Scope;
import org.vesalainen.parser.util.Input;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parser.util.UnderflowException;

/**
 * This regular expression implementation is DFA rather than NFA based. Using DFA is much
 * faster but there are some limitations.
 * Following features are not supported:
 *
 * <p>Capturing groups are not supported
 *
 * <p>Reluctant and possessive quantifiers are not supported.
 *
 * <p>Special constructs are not supported
 *
 * <p>You can get Regex instance by using compile method. Compiling takes time. 
 *
 * <p>Regex classes are thread safe
 *
 * @see java.util.regex.Pattern
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="doc-files/Regex-regexp.html#BNF">BNF Syntax for Regular expression</a>
 */
public abstract class Regex
{

    public enum Option
    {

        /**
         * Enables case-insensitive matching.
         */
        CASE_INSENSITIVE,
        /**
         * Creates a matcher for grammars where a fixed string ends the match. Examples
         * are xml comments ending in --> or PCDATA ending in ]]>. Regular expression
         * like '.*\\-\\->' without FIXED_ENDER option doesn't work. However with
         * FIXED_ENDER option the underlying NFA is modified to support that kind
         * of parsing before DFA creation. Note that the suffix is pushed back after
         * recognition.
         */
        FIXED_ENDER,
        /**
         * Normally parser tries to match as more characters as is possible. For 
         * expression 'LITERAL|LITERALS' it accepts LITERALS for input LITERALS.
         * if FIXED_ENDER options is used 'LITERAL' is matched instead without reading further 
         * input.
         */
        ACCEPT_IMMEDIATELY;

        public static boolean supports(Option[] options, Option option)
        {
            for (Option opt : options)
            {
                if (opt.equals(option))
                {
                    return true;
                }
            }
            return false;
        }
    };
    private static RegexParserIntf<Integer> regexParser;
    protected static boolean debug;
    protected boolean acceptEmpty;
    protected String expression;
    protected int minLength;
    protected int maxLength;

    /**
     * Returns the maximum length of accepted string or 2147483647 if expression
     * can accept infinite length string.
     * @return
     */
    public int getMaxLength()
    {
        return maxLength;
    }

    /**
     * Returns the minimum length of accepted string.
     * @return
     */
    public int getMinLength()
    {
        return minLength;
    }

    /**
     * Returns the expression from which this Regex has been compiled.
     * @return
     */
    public String getExpression()
    {
        return expression;
    }
    /**
     * Returns a regular expression from a literal expression which has '?' 
     * wildcard for any single character and * for any number of character.
     * @param literal
     * @return 
     */
    public static String wildcard(String literal)
    {
        StringBuilder sb = new StringBuilder();
        for (int ii = 0; ii < literal.length(); ii++)
        {
            char cc = literal.charAt(ii);
            switch (cc)
            {
                case '[':
                case ']':
                case '(':
                case ')':
                case '\\':
                case '-':
                case '^':
                case '+':
                case '|':
                case '.':
                case '{':
                case '}':
                case '&':
                case '$':
                case ',':
                    sb.append("\\").append(cc);
                    break;
                case '*':
                    sb.append(".*");
                    break;
                case '?':
                    sb.append(".");
                    break;
                default:
                    sb.append(cc);
                    break;
            }
        }
        return sb.toString();
    }
    /**
     * Escapes all regex control characters returning expression suitable for
     * literal parsing.
     * @param literal
     * @return Escaped string
     */
    public static String escape(String literal)
    {
        StringBuilder sb = new StringBuilder();
        for (int ii = 0; ii < literal.length(); ii++)
        {
            char cc = literal.charAt(ii);
            switch (cc)
            {
                case '[':
                case ']':
                case '(':
                case ')':
                case '\\':
                case '-':
                case '^':
                case '*':
                case '+':
                case '?':
                case '|':
                case '.':
                case '{':
                case '}':
                case '&':
                case '$':
                case ',':
                    sb.append("\\").append(cc);
                    break;
                default:
                    sb.append(cc);
                    break;
            }
        }
        return sb.toString();
    }
    /**
     * Double escapes all regex control characters plus linefeed and tab 
     * characters returning expression suitable for printing.
     * @param literal
     * @return Escaped string
     */
    public static String printable(String literal)
    {
        StringBuilder sb = new StringBuilder();
        for (int ii = 0; ii < literal.length(); ii++)
        {
            char cc = literal.charAt(ii);
            switch (cc)
            {
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '[':
                case ']':
                case '(':
                case ')':
                case '\\':
                case '-':
                case '^':
                case '*':
                case '+':
                case '?':
                case '|':
                case '.':
                case '{':
                case '}':
                case '&':
                case '$':
                case ',':
                    sb.append("\\\\").append(cc);
                    break;
                default:
                    sb.append(cc);
                    break;
            }
        }
        return sb.toString();
    }

    public static void setDebug(boolean d)
    {
        debug = d;
    }

    protected void trace(int a, String msg)
    {
        System.err.println(a + ": " + msg);
    }

    /**
     * Return true if text matches the regex
     * @param text
     * @return
     * @throws IOException
     */
    public boolean isMatch(CharSequence text)
    {
        try
        {
            if (text.length() == 0)
            {
                return acceptEmpty;
            }
            InputReader reader = Input.getInstance(text);
            return isMatch(reader);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException("can't happen");
        }
    }

    /**
     * Return true if input matches the regex
     * @param input
     * @param size
     * @return
     * @throws IOException
     */
    public boolean isMatch(PushbackReader input, int size) throws IOException
    {
        InputReader reader = Input.getInstance(input, size);
        return isMatch(reader);
    }

    /**
     * Return true if input matches the regex. Using shared buffer reduces the
     * need to allocate new buffer for several natches.
     * @param input
     * @param shared Shared buffer
     * @return
     * @throws IOException
     * @throws SyntaxErrorException
     */
    public boolean isMatch(PushbackReader input, char[] shared) throws IOException
    {
        InputReader reader = Input.getInstance(input, shared);
        return isMatch(reader);
    }

    /**
     * Return true if input matches the regex.
     * @param reader
     * @return
     * @throws IOException
     * @throws SyntaxErrorException
     */
    public boolean isMatch(InputReader reader) throws IOException
    {
        int rc = match(reader);
        return (rc == 1 && reader.read() == -1);
    }

    /**
     * Attempts to match input to regex
     * @param text
     * @return
     * @throws SyntaxErrorException
     */
    public String match(CharSequence text)
    {
        try
        {
            if (text.length() == 0)
            {
                if (acceptEmpty)
                {
                    return "";
                }
                else
                {
                    throw new SyntaxErrorException("empty string not accepted");
                }
            }
            InputReader reader = Input.getInstance(text);
            int rc = match(reader);
            if (rc == 1 && reader.read() == -1)
            {
                return reader.getString();
            }
            else
            {
                throw new SyntaxErrorException("syntax error"
                        + "\n"
                        + reader.getLineNumber() + ": " + reader.getLine()
                        + "\n"
                        + pointer(reader.getColumnNumber() + 2));
            }
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException("can't happen");
        }
    }

    /**
     * Matches the whole input and returns the matched string
     * @param in
     * @param size
     * @return
     * @throws IOException
     * @throws SyntaxErrorException
     */
    public String getMatch(PushbackReader in, int size) throws IOException, SyntaxErrorException
    {
        InputReader reader = Input.getInstance(in, size);
        String s = getMatch(reader);
        reader.release();
        return s;
    }

    /**
     * Matches the whole input and returns the matched string
     * @param text
     * @return
     * @throws SyntaxErrorException
     */
    public String getMatch(CharSequence text)
    {
        try
        {
            if (text.length() == 0)
            {
                if (acceptEmpty)
                {
                    return "";
                }
                else
                {
                    throw new SyntaxErrorException("empty string not accepted");
                }
            }
            InputReader reader = Input.getInstance(text);
            return getMatch(reader);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException("can't happen");
        }
    }

    /**
     * Matches the whole input and returns the matched string
     * @param in
     * @param shared
     * @return
     * @throws IOException
     * @throws SyntaxErrorException
     */
    public String getMatch(PushbackReader in, char[] shared) throws IOException, SyntaxErrorException
    {
        InputReader reader = Input.getInstance(in, shared);
        String s = getMatch(reader);
        reader.release();
        return s;
    }

    /**
     * Matches the whole input and returns the matched string
     * @param reader
     * @return
     * @throws IOException
     * @throws SyntaxErrorException
     */
    public String getMatch(InputReader reader) throws IOException, SyntaxErrorException
    {
        int rc = match(reader);
        if (rc == 1 && reader.read() == -1)
        {
            return reader.getString();
        }
        else
        {
            throw new SyntaxErrorException("syntax error"
                    + "\n"
                    + reader.getLineNumber() + ": " + reader.getLine()
                    + "\n"
                    + pointer(reader.getColumnNumber() + 2));
        }
    }

    /**
     * Returns true if input start matches the regular expression
     * @param text
     * @return
     */
    public boolean startsWith(CharSequence text)
    {
        try
        {
            if (text.length() == 0)
            {
                if (acceptEmpty)
                {
                    return true;
                }
                else
                {
                    throw new SyntaxErrorException("empty string not accepted");
                }
            }
            InputReader reader = Input.getInstance(text);
            return startsWith(reader);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException("can't happen");
        }
    }

    /**
     * Returns true if input start matches the regular expression
     * @param in
     * @param size
     * @return
     * @throws IOException
     */
    public boolean startsWith(PushbackReader in, int size) throws IOException
    {
        InputReader reader = Input.getInstance(in, size);
        boolean b = startsWith(reader);
        reader.release();
        return b;
    }

    /**
     * Returns true if input start matches the regular expression
     * @param in
     * @param shared
     * @return
     * @throws IOException
     */
    public boolean startsWith(PushbackReader in, char[] shared) throws IOException
    {
        InputReader reader = Input.getInstance(in, shared);
        boolean b = startsWith(reader);
        reader.release();
        return b;
    }

    /**
     * Returns true if input start matches the regular expression
     * @param reader
     * @return
     * @throws IOException
     */
    public boolean startsWith(InputReader reader) throws IOException
    {
        int rc = match(reader);
        return rc == 1;
    }

    /**
     * Matches the start of text and returns the matched string
     * @param text
     * @return
     * @throws SyntaxErrorException
     */
    public String lookingAt(CharSequence text)
    {
        try
        {
            if (text.length() == 0)
            {
                if (acceptEmpty)
                {
                    return "";
                }
                else
                {
                    throw new SyntaxErrorException("empty string not accepted");
                }
            }
            InputReader reader = Input.getInstance(text);
            return lookingAt(reader);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException("can't happen");
        }
    }

    /**
     * Matches the start of text and returns the matched string
     * @param in
     * @param size
     * @return
     * @throws IOException
     * @throws SyntaxErrorException
     */
    public String lookingAt(PushbackReader in, int size) throws IOException, SyntaxErrorException
    {
        InputReader reader = Input.getInstance(in, size);
        String s = lookingAt(reader);
        reader.release();
        return s;
    }

    /**
     * Matches the start of text and returns the matched string
     * @param in
     * @param shared
     * @return
     * @throws IOException
     * @throws SyntaxErrorException
     */
    public String lookingAt(PushbackReader in, char[] shared) throws IOException, SyntaxErrorException
    {
        InputReader reader = Input.getInstance(in, shared);
        String s = lookingAt(reader);
        reader.release();
        return s;
    }

    /**
     * Matches the start of text and returns the matched string
     * @param reader
     * @return
     * @throws IOException
     * @throws SyntaxErrorException
     */
    public String lookingAt(InputReader reader) throws IOException, SyntaxErrorException
    {
        int rc = match(reader);
        if (rc == 1)
        {
            return reader.getString();
        }
        else
        {
            throw new SyntaxErrorException("syntax error"
                    + "\n"
                    + reader.getLineNumber() + ": " + reader.getLine()
                    + "\n"
                    + pointer(reader.getColumnNumber() + 2));
        }
    }

    private String pointer(int p)
    {
        StringBuilder sb = new StringBuilder();
        for (int ii = 0; ii < p; ii++)
        {
            sb.append(" ");
        }
        sb.append("^^^");
        return sb.toString();
    }

    /**
     * Finds next match and returns the matched string
     * @param text
     * @return
     * @throws SyntaxErrorException
     */
    public String find(CharSequence text)
    {
        try
        {
            if (text.length() == 0)
            {
                if (acceptEmpty)
                {
                    return "";
                }
                else
                {
                    throw new SyntaxErrorException("empty string not accepted");
                }
            }
            if (acceptEmpty)
            {
                throw new IllegalArgumentException("using find for expression that accepts empty string");
            }
            InputReader reader = Input.getInstance(text);
            int rc = find(reader);
            if (rc == 1)
            {
                return reader.getString();
            }
            else
            {
                throw new SyntaxErrorException("string matching '" + expression + "' not found");
            }
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException("can't happen", ex);
        }
    }

    /**
     * Finds next match and returns the matched string
     * @param in
     * @param size
     * @return Matched string
     * @throws IOException
     * @throws SyntaxErrorException
     */
    public String find(PushbackReader in, int size) throws IOException, SyntaxErrorException
    {
        if (acceptEmpty)
        {
            throw new IllegalArgumentException("using find for  '" + expression + "'  that accepts empty string");
        }
        InputReader reader = Input.getInstance(in, size);
        int rc = find(reader);
        reader.release();
        if (rc == 1)
        {
            return reader.getString();
        }
        else
        {
            throw new SyntaxErrorException("string matching  '" + expression + "'  not found");
        }
    }

    /**
     * Replaces regular expression matches in text with replacement string
     * @param text
     * @param replacement
     * @return
     */
    public String replace(CharSequence text, CharSequence replacement)
    {
        try
        {
            if (text.length() == 0)
            {
                if (acceptEmpty)
                {
                    return "";
                }
            }
            CharArrayWriter caw = new CharArrayWriter();
            InputReader reader = Input.getInstance(text);
            ObsoleteSimpleReplacer fsp = new ObsoleteSimpleReplacer(replacement);
            replace(reader, caw, fsp);
            return caw.toString();
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException("can't happen", ex);
        }
    }

    /**
     * Replaces regular expression matches in text using replacer
     * @param text
     * @param replacer
     * @return
     * @throws IOException
     */
    public String replace(CharSequence text, ObsoleteReplacer replacer) throws IOException
    {
        if (text.length() == 0)
        {
            return "";
        }
        CharArrayWriter caw = new CharArrayWriter();
        InputReader reader = Input.getInstance(text);
        replace(reader, caw, replacer);
        return caw.toString();
    }
    /**
     * Replaces regular expression matches in text using replacer
     * @param text
     * @param bufferSize bufferSize must be > text.length() + max insert text
     * @param replacer
     * @return
     * @throws IOException 
     */
    public String replace(CharSequence text, int bufferSize, ObsoleteReplacer replacer) throws IOException
    {
        if (text.length() == 0)
        {
            return "";
        }
        CharArrayWriter caw = new CharArrayWriter();
        InputReader reader = Input.getInstance(text, bufferSize);
        replace(reader, caw, replacer);
        return caw.toString();
    }

    /**
     * Writes in to out replacing every match with a string 
     * @param in
     * @param bufferSize
     * @param out
     * @param format
     * @throws java.io.IOException
     * @see String.format
     */
    public void replace(PushbackReader in, int bufferSize, Writer out, String format) throws IOException
    {
        InputReader reader = Input.getInstance(in, bufferSize);
        ObsoleteSimpleReplacer fsp = new ObsoleteSimpleReplacer(format);
        replace(reader, out, fsp);
    }
    public void replace(CharSequence text, Writer out, String format) throws IOException
    {
        if (text.length() > 0)
        {
            InputReader reader = Input.getInstance(text);
            ObsoleteSimpleReplacer fsp = new ObsoleteSimpleReplacer(format);
            replace(reader, out, fsp);
        }
    }


    /**
     * Replaces regular expression matches in input using replacer
     * @param in
     * @param bufferSize
     * @param out
     * @param replacer
     * @throws IOException
     */
    public void replace(PushbackReader in, int bufferSize, Writer out, ObsoleteReplacer replacer) throws IOException
    {
        InputReader reader = Input.getInstance(in, bufferSize);
        replace(reader, out, replacer);
    }

    public void replace(CharSequence text, Writer out, ObsoleteReplacer replacer) throws IOException
    {
        if (text.length() > 0)
        {
            InputReader reader = Input.getInstance(text);
            replace(reader, out, replacer);
        }
    }

    public void replace(PushbackReader in, char[] shared, Writer out, String format) throws IOException
    {
        InputReader reader = Input.getInstance(in, shared);
        ObsoleteSimpleReplacer fsp = new ObsoleteSimpleReplacer(format);
        replace(reader, out, fsp);
    }

    public void replace(PushbackReader in, char[] shared, Writer out, ObsoleteReplacer replacer) throws IOException
    {
        InputReader reader = Input.getInstance(in, shared);
        replace(reader, out, replacer);
    }

    public void replace(InputReader reader, Writer out, ObsoleteReplacer replacer) throws IOException
    {
        long start = 0;
        long end = 0;
        PrintWriter pw = null;
        if (out instanceof PrintWriter)
        {
            pw = (PrintWriter) out;
        }
        else
        {
            pw = new PrintWriter(out);
        }
        while (!reader.isEof())
        {
            reader.clear();
            try
            {
                int rc = find(reader);
                if (rc == 1)
                {
                    if (reader.getLength() == 0)
                    {
                        reader.read();
                    }
                    else
                    {
                        end = reader.getStart();
                        if (end > start)
                        {
                            reader.write(start, (int) (end - start), pw);
                        }
                        start = reader.getEnd();
                        replacer.replace(reader, pw);
                    }
                }
                else
                {
                    break;
                }
            }
            catch (UnderflowException ex)
            {
                System.err.println();
                end = reader.getEnd();
                reader.write(start, (int) (end - start), pw);
                start = end;
                reader.clear();
            }
        }
        end = reader.getEnd();
        reader.write(start, (int) (end - start), pw);
        pw.close();
    }

    /**
     * Splits the input
     * @param text
     * @return
     */
    public String[] split(CharSequence text)
    {
        return split(text, Integer.MAX_VALUE);
    }

    /**
     * Splits the input
     * @param text
     * @param limit See java.util.Pattern.split
     * @return
     */
    public String[] split(CharSequence text, int limit)
    {
        try
        {
            InputReader reader = Input.getInstance(text);
            List<String> list = split(reader, limit);
            return list.toArray(new String[list.size()]);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException("can't happen", ex);
        }
    }

    /**
     * Splits the input
     * @param in
     * @param bufferSize
     * @return
     * @throws IOException
     */
    public String[] split(PushbackReader in, int bufferSize) throws IOException
    {
        return split(in, bufferSize, Integer.MAX_VALUE);
    }

    public String[] split(PushbackReader in, char[] shared) throws IOException
    {
        return split(in, shared, Integer.MAX_VALUE);
    }

    /**
     * Splits the input
     * @param in
     * @param bufferSize
     * @param limit See java.util.Pattern.split
     * @return
     * @throws IOException
     * @see Pattern.split
     */
    public String[] split(PushbackReader in, int bufferSize, int limit) throws IOException
    {
        InputReader reader = Input.getInstance(in, bufferSize);
        List<String> list = split(reader, limit);
        return list.toArray(new String[list.size()]);
    }

    public String[] split(PushbackReader in, char[] shared, int limit) throws IOException
    {
        InputReader reader = Input.getInstance(in, shared);
        List<String> list = split(reader, limit);
        return list.toArray(new String[list.size()]);
    }

    private List<String> split(InputReader reader, int limit) throws IOException
    {
        List<String> list = new ArrayList<>();
        int count = 0;
        long start = 0;
        long end = 0;
        while (!reader.isEof())
        {
            count++;
            if (count == limit)
            {
                CharArrayWriter caw = new CharArrayWriter();
                int cc = reader.read();
                while (cc != -1)
                {
                    caw.write(cc);
                    cc = reader.read();
                }
                list.add(caw.toString());
                return list;
            }
            reader.clear();
            int rc = find(reader);
            if (rc == 1)
            {
                if (reader.getLength() == 0)
                {
                    reader.read();
                }
                else
                {
                    end = reader.getStart();
                    list.add(reader.getString(start, (int) (end - start)));
                    start = reader.getEnd();
                }
            }
            else
            {
                break;
            }
        }
        end = reader.getEnd();
        list.add(reader.getString(start, (int) (end - start)));
        if (limit == 0)
        {
            while (!list.isEmpty() && list.get(list.size() - 1).isEmpty())
            {
                list.remove(list.size() - 1);
            }
        }
        return list;
    }

    protected abstract int match(InputReader reader) throws IOException;

    protected abstract int find(InputReader reader) throws IOException;

    /**
     * Compiles a literal string into RegExImpl class. This is ok for testing. use RegexBuilder
     * ant task for release classes
     * @param expression
     * @return
     * @throws IOException
     */
    public static Regex literal(String expression) throws IOException
    {
        return compile(escape(expression));
    }

    /**
     * Compiles a literal string into RegexImpl class. This is ok for testing. use RegexBuilder
     * ant task for release classes
     * @param expression
     * @param options
     * @return
     * @throws IOException
     */
    public static Regex literal(String expression, Option... options) throws IOException
    {
        return compile(escape(expression), options);
    }

    /**
     * Compiles a regular expression into RegExImpl class.
     * @param expression
     * @param options
     * @return
     * @throws NoSuchMethodException
     * @throws NoSuchFieldException
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private static int regexCount;
    public static Regex compile(String expression, Option... options) throws IOException
    {
        String className = "org.vesalainen.regex.Regex"+regexCount;
        regexCount++;
        SubClass subClass = createSubClass(expression, className, options);
        Regex regex = (Regex) subClass.newInstance();
        return regex;
    }

    /**
     * Creates a DFA from regular expression
     * @param expression
     * @return
     */
    public static DFA createDFA(String expression)
    {
        return createDFA(expression, 1);
    }

    /**
     * Creates a DFA from regular expression
     * @param expression
     * @param reducer Reducer marks the accepting state with unique identifier
     * @param options
     * @return
     */
    public static DFA<Integer> createDFA(String expression, int reducer, Option... options)
    {
        NFA<Integer> nfa = createNFA(new Scope<NFAState<Integer>>(expression), expression, reducer, options);
        DFA<Integer> dfa = nfa.constructDFA(new Scope<DFAState<Integer>>(expression));
        return dfa;
    }

    /**
     * Creates an NFA from regular expression
     * @param scope
     * @param expression
     * @return
     */
    public static NFA<Integer> createNFA(Scope<NFAState<Integer>> scope, String expression)
    {
        return createNFA(scope, expression, 1);
    }

    /**
     * Creates an NFA from regular expression
     * @param scope
     * @param expression
     * @param token Token marks the accepting state with unique identifier
     * @param options
     * @return
     */
    public static NFA<Integer> createNFA(Scope<NFAState<Integer>> scope, String expression, int token, Option... options)
    {
        if (regexParser == null)
        {
            regexParser = RegexParserFactory.newInstance();
        }
        return regexParser.createNFA(scope, expression, token, options);
    }

    public static SubClass createSubClass(String expression, String classname, Option... options) throws IOException
    {
        //return createSubClass(expression, createDFA(expression, 1, options), classname);
        SubClass subClass = new SubClass(Regex.class, classname, Modifier.PUBLIC);
        DFA dfa = createDFA(expression, 1, options);
        subClass.codeDefaultConstructor(
                FieldInitializer.getInstance(El.getField(Regex.class, "acceptEmpty"), dfa.acceptEmpty()), 
                FieldInitializer.getInstance(El.getField(Regex.class, "expression"), expression), 
                FieldInitializer.getInstance(El.getField(Regex.class, "minLength"), dfa.minDepth()), 
                FieldInitializer.getInstance(El.getField(Regex.class, "maxLength"), dfa.maxDepth())
                );
        MatchCompiler<Integer> matchCompiler = new MatchCompiler<>(dfa, -1, 0);
        if (debug)
        {
            //Method trace = Regex.class.getDeclaredMethod("trace", Integer.TYPE, String.class);
            //matchComp.setDebug(trace);
        }
        subClass.overrideMethod(matchCompiler, java.lang.reflect.Modifier.PUBLIC, "match", InputReader.class);

        dfa = createDFA(expression, 1, options);
        FindCompiler<Integer> findCompiler = new FindCompiler<>(dfa, -1, 0);
        if (debug)
        {
            //Method trace = Regex.class.getDeclaredMethod("trace", Integer.TYPE, String.class);
            //findComp.setDebug(trace);
        }
        subClass.overrideMethod(findCompiler, java.lang.reflect.Modifier.PUBLIC, "find", InputReader.class);
        return subClass;

    }

}
