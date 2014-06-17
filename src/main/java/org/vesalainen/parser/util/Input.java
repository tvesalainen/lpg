/*
 * Copyright (C) 2014 Timo Vesalainen
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

package org.vesalainen.parser.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Deque;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.Typ;
import org.vesalainen.grammar.GTerminal;
import org.vesalainen.parser.ParserConstants;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.regex.Range;
import org.vesalainen.regex.SyntaxErrorException;
import org.xml.sax.InputSource;

/**
 * A base class for parser input
 * 
 * @author Timo Vesalainen
 * @param <I> Input type. Reader, InputStream, String,...
 */
public abstract class Input<I> implements InputReader
{
    protected int size;           // size of ring buffer (=buffer.length)
    protected int end;            // position of last actual read char
    protected int cursor;         // position of current input
    protected IncludeLevel includeLevel = new IncludeLevel();
    protected Deque<IncludeLevel> includeStack;
    protected int length;         // length of current input
    protected int findSkip;       // number of characters the find can skip after unsucces
    protected int findMark = -1;  // position where find could have last accessed the string
    protected int waterMark = 0;  // lowest position where buffer can be reused
    protected boolean useOffsetLocatorException;

    protected abstract int get(int index);
    protected abstract void set(int index, int value);
    protected abstract int fill(I input, int offset, int length) throws IOException;
    protected abstract void close(I input) throws IOException;
    protected abstract boolean ready(I input) throws IOException;
    protected abstract void reuse(I input);
    
    public static InputReader getInstance(File file, int size) throws FileNotFoundException
    {
        return new ReaderInput(file, size);
    }
    public static InputReader getInstance(File file, int size, String cs) throws FileNotFoundException
    {
        return new ReaderInput(file, size, cs);
    }
    public static InputReader getInstance(File file, int size, String cs, boolean upper) throws FileNotFoundException
    {
        return new ReaderInput(file, size, cs, upper);
    }
    public static InputReader getInstance(File file, int size, Charset cs) throws FileNotFoundException
    {
        return new ReaderInput(file, size, cs);
    }
    public static InputReader getInstance(File file, int size, Charset cs, boolean upper) throws FileNotFoundException
    {
        return new ReaderInput(file, size, cs, upper);
    }
    /**
     * Constructs an InputReader with default charset
     * @param is
     * @param size size of inner ring buffer
     */
    public static InputReader getInstance(InputStream is, int size)
    {
        return new ReaderInput(is, size);
    }
    public static InputReader getInstance(InputStream is, int size, boolean upper)
    {
        return new ReaderInput(is, size, upper);
    }
    /**
     * Constructs an InputReader
     * @param is
     * @param size size of inner ring buffer
     * @param cs Character set
     */
    public static InputReader getInstance(InputStream is, int size, String cs)
    {
        return new ReaderInput(is, size, cs);
    }
    public static InputReader getInstance(InputStream is, int size, String cs, boolean upper)
    {
        return new ReaderInput(is, size, cs, upper);
    }
    /**
     * Constructs an InputReader
     * @param is
     * @param size
     * @param cs 
     */
    public static InputReader getInstance(InputStream is, int size, Charset cs)
    {
        return new ReaderInput(is, size, cs);
    }
    public static InputReader getInstance(InputStream is, int size, Charset cs, boolean upper)
    {
        return new ReaderInput(is, size, cs, upper);
    }
    /**
     * Constructs an InputReader
     * @param sr
     * @param size 
     */
    public static InputReader getInstance(StreamReader sr, int size)
    {
        return new ReaderInput(sr, size);
    }
    /**
     * Constructs an InputReader
     * @param in
     * @param size
     * @param upper If true input is converted upper-case, if false input is converted lower-case
     */
    public static InputReader getInstance(Reader in, int size, boolean upper)
    {
        return new ReaderInput(in, size, upper);
    }
    /**
     * Constructs an InputReader
     * @param in
     * @param size 
     */
    public static InputReader getInstance(Reader in, int size)
    {
        return new ReaderInput(in, size);
    }
    /**
     * Constructs an InputReader
     * @param in
     * @param size 
     */
    public static InputReader getInstance(PushbackReader in, int size)
    {
        return new ReaderInput(in, size);
    }
    /**
     * Constructs an InputReader
     * @param in
     * @param shared Shared ringbuffer.
     */
    public static InputReader getInstance(PushbackReader in, char[] shared)
    {
        return new ReaderInput(in, shared);
    }
    /**
     * Constructs an InputReader
     * 
     * <p>Note! This implementation doesn't support insert. Use the method
     * with size.
     * @param text
     * @return 
     * @see org.vesalainen.parser.util.Input#getInstance(java.lang.CharSequence, int) 
     */
    public static InputReader getInstance(CharSequence text)
    {
        return new TextInput(text);
    }
    /**
     * Constructs an InputReader
     * @param text
     * @param size 
     * @return  
     */
    public static InputReader getInstance(CharSequence text, int size)
    {
        return new ReaderInput(text, size);
    }
    /**
     * Constructs an InputReader
     * @param array
     * @return 
     */
    public static InputReader getInstance(char[] array)
    {
        return new ReaderInput(array);
    }
    /**
     * Constructs an InputReader
     * @param input
     * @param size Ringbuffer size
     * @return
     * @throws IOException 
     */
    public static InputReader getInstance(InputSource input, int size) throws IOException
    {
        InputReader inputReader = null;
        Reader reader = input.getCharacterStream();
        if (reader != null)
        {
            inputReader = new ReaderInput(reader, size);
        }
        else
        {
            InputStream is = input.getByteStream();
            String encoding = input.getEncoding();
            if (is != null)
            {
                if (encoding != null)
                {
                    inputReader = new ReaderInput(is, size, encoding);
                }
                else
                {
                    inputReader = new ReaderInput(is, size, "US-ASCII");
                }
            }
            else
            {
                String sysId = input.getSystemId();
                try
                {
                    URI uri = new URI(sysId);
                    InputStream uis = uri.toURL().openStream();
                    if (encoding != null)
                    {
                        inputReader = new ReaderInput(uis, size, encoding);
                    }
                    else
                    {
                        inputReader = new ReaderInput(uis, size, "US-ASCII");
                    }
                }
                catch (URISyntaxException ex)
                {
                    throw new IOException(ex);
                }
            }
        }
        inputReader.setSource(input.getSystemId());
        return inputReader;
    }
    @Override
    public void useOffsetLocatorException(boolean useOffsetLocatorException)
    {
        this.useOffsetLocatorException = useOffsetLocatorException;
    }
    /**
     * Set current character set. Only supported with InputStreams!
     * @param cs 
     */
    @Override
    public void setEncoding(String cs)
    {
        setEncoding(Charset.forName(cs));
    }
    /**
     * Set current character set. Only supported with InputStreams!
     * @param cs 
     */
    @Override
    public void setEncoding(Charset cs)
    {
        if (includeLevel.getStreamReader() == null)
        {
            throw new UnsupportedOperationException("setting charset not supported with current input");
        }
        includeLevel.getStreamReader().setCharset(cs);
    }
    /**
     * Set's the source of current input
     * @param source A string describing the input source, like filename.
     */
    @Override
    public void setSource(String source)
    {
        includeLevel.setSource(source);
    }
    /**
     * Get's the source of current input 
     * @return A string describing the input source, like filename.
     */
    @Override
    public String getSource()
    {
        return includeLevel.getSource();
    }
    
    @Override
    public void recover() throws SyntaxErrorException
    {
        if (! tryRecover())
        {
            throwSyntaxErrorException(null);
        }
    }
    @Override
    public void recover(@ParserContext(ParserConstants.THROWABLE) Throwable thr) throws SyntaxErrorException
    {
        if (! tryRecover())
        {
            throwSyntaxErrorException(thr);
        }
    }
    @Override
    public void recover(            
            @ParserContext(ParserConstants.ExpectedDescription) String expecting, 
            @ParserContext(ParserConstants.LastToken) String token) throws SyntaxErrorException

    {
        if (! tryRecover())
        {
            throwSyntaxErrorException(expecting, token);
        }
    }
    private boolean tryRecover()
    {
        if (includeLevel.in instanceof Recoverable)
        {
            Recoverable recoverable = (Recoverable) includeLevel.in;
            if (recoverable.recover())
            {
                clear();
                return true;
            }
        }
        return false;
    }
    @Override
    public void throwSyntaxErrorException() throws SyntaxErrorException
    {
        throwSyntaxErrorException(null);
    }
    @Override
    public void throwSyntaxErrorException(@ParserContext(ParserConstants.THROWABLE) Throwable thr) throws SyntaxErrorException
    {
        String source = includeLevel.getSource();
        if (useOffsetLocatorException)
        {
            throw new OffsetLocatorException("syntax error", source, getStart(), getEnd(), thr);
        }
        else
        {
            int line = getLineNumber();
            int column = getColumnNumber();
            throw new LineLocatorException("source: "+source+"\n"+
                    "syntax error at line "+line+": pos "+column+
                    "\n"+
                    getLine()+
                    "\n"+
                    pointer(getColumnNumber()),
                    source,
                    line,
                    column,
                    thr
                    );
        }
    }

    @Override
    public void throwSyntaxErrorException(
            @ParserContext(ParserConstants.ExpectedDescription) String expecting, 
            @ParserContext(ParserConstants.LastToken) String token) throws SyntaxErrorException
    {
        String source = includeLevel.getSource();
        if (useOffsetLocatorException)
        {
            throw new OffsetLocatorException("Expected: '"+expecting+"' got "+token+"='"+getString()+"'", source, getStart(), getEnd());
        }
        else
        {
            int line = getLineNumber();
            int column = getColumnNumber();
            throw new LineLocatorException("source: "+source+"\n"+
                    "Expected: '"+expecting+"' at line "+line+": pos "+column+
                    "\n"+
                    getLine()+
                    "\n"+
                    pointer(getColumnNumber())+
                    "\n got "+token+"='"+getString()+"'",
                    source,
                    line,
                    column
                    );
        }
    }

    private String pointer(int p)
    {
        StringBuilder sb = new StringBuilder();
        for (int ii=1;ii<p;ii++)
        {
            sb.append(" ");
        }
        sb.append("^^^");
        return sb.toString();
    }
    /**
     * Return true if next input is eof
     * @return
     * @throws IOException 
     */
    @Override
    public boolean isEof() throws IOException
    {
        return peek(1) == -1;
    }
    /**
     * Synchronizes actual reader to current cursor position
     * @throws IOException
     */
    @Override
    public void release() throws IOException
    {
        if (includeLevel.in != null && end != cursor)
        {
            if (includeLevel.in instanceof PushbackReader)
            {
                PushbackReader pr = (PushbackReader) includeLevel.in;
                for (int ii=end-1;ii>=cursor;ii--)
                {
                    pr.unread(get(ii));
                }
                end = cursor;
            }
            else
            {
                throw new UnsupportedOperationException("release() only supported for java.io.PushbackReader. Not for "+includeLevel.in.getClass().getName());
            }
        }
    }
    /**
     * Returns the length of current input
     * @return
     */
    @Override
    public int getLength()
    {
        return length;
    }
    /**
     * Returns the start position of current input
     * @return
     */
    @Override
    public int getStart()
    {
        return cursor-length;
    }
    /**
     * Returns the end position of current input
     * @return
     */
    @Override
    public int getEnd()
    {
        return cursor;
    }
    /**
     * Returns a reference to current field. Field start and length are decoded
     * in int value. This method is used in postponing or avoiding string object 
     * creation. String or Iterator<String> can be constructed later by using
     * getString(int fieldRef) or getCharSequence(fieldRef) methods. 
     * 
     * <p>Note! If buffer size is too small the fieldRef might not be available.
     * 
     * <p>Same effect is by storing start = getStart() and len = getLength() and 
     * later calling getString(start, end) as long as the circular buffer is not
     * reused.
     * @return
     */
    @Override
    public int getFieldRef()
    {
        if (size > 0xffff)
        {
            throw new IllegalArgumentException("fieldref not supported when buffer size is >65535");
        }
        return (cursor-length) % size + length * 0x10000;
    }
    /**
     * @deprecated This methods usage is unclear
     * @param fieldRef1
     * @param fieldRef2
     * @return 
     */
    public int concat(int fieldRef1, int fieldRef2)
    {
        int l1 = fieldRef1>>16;
        int s1 = fieldRef1 & 0xffff;
        int l2 = fieldRef2>>16;
        int s2 = fieldRef2 & 0xffff;
        return (s1) % size + (l1+l2) * 0x10000;
    }
    /**
     * @deprecated This methods usage is unclear
     * @param fieldRef
     * @param buf
     * @return 
     */
    public boolean equals(int fieldRef, char[] buf)
    {
        int l = fieldRef>>16;
        int s = fieldRef & 0xffff;
        for (int ii=0;ii<l;ii++)
        {
            if (buf[ii] != get((s+ii)))
            {
                return false;
            }
        }
        return true;
    }
    /**
     * Returns the last matched input
     * @return 
     */
    @Override
    public String getString()
    {
        return getString(cursor-length, length);
    }
    /**
     * Returns the string matched with fieldref
     * @param fieldRef
     * @return string matched with fieldref
     */
    @Override
    public String getString(int fieldRef)
    {
        return getString(fieldRef & 0xffff, fieldRef>>16);
    }
    /**
     * Returns a CharSequence matched with fieldRef.
     * @param fieldRef
     * @return 
     */
    @Override
    public CharSequence getCharSequence(int fieldRef)
    {
        return getCharSequence(fieldRef & 0xffff, fieldRef>>16);
    }

    /**
     * Returns buffered data as String. Buffered data is ready in array.
     * @return 
     */
    @Override
    public String buffered()
    {
        return getString(cursor, end-cursor);
    }
    /**
     * Returns a CharSequence
     * @param s Start
     * @param l Length
     * @return 
     */
    @Override
    public CharSequence getCharSequence(int s, int l)
    {
        return new CharSequenceImpl(s, l);
    }
    @Override
    public String getLine()
    {
        int c = includeLevel.getColumn();
        if (cursor-c < end-size)
        {
            int len = size / 2;
            return "... "+getString(end-len, len);
        }
        else
        {
            return getString(cursor-c, end-(cursor-c));
        }
    }
    /**
     * Returns the input data after last release call
     * @return 
     */
    @Override
    public String getInput()
    {
        return getString(cursor-length, length);
    }
    
    @Override
    public String toString()
    {
        return getInput();
    }
    /**
     * get a char from input buffer.
     * @param offset 0 is last read char.
     * @return
     * @throws IOException
     */
    @Override
    public int peek(int offset) throws IOException
    {
        int target = cursor + offset - 1;
        if (target - end > size || target < end - size || target < 0)
        {
            throw new IllegalArgumentException("offset "+offset+" out of buffer");
        }
        if (target >= end)
        {
            int la = 0;
            while (target >= end)
            {
                int cc = read();
                if (cc == -1)
                {
                    if (target+la == end)
                    {
                        return -1;
                    }
                    else
                    {
                        throw new IOException("eof");
                    }
                }
                la++;
            }
            rewind(la);
        }
        return get(target);
    }
    /**
     * Set how many characters we can skip after failed find.
     * @param acceptStart 
     */
    @Override
    public void setAcceptStart(int acceptStart)
    {
        findSkip = acceptStart;
    }
    /**
     * Marks to position where find could accept the input.
     */
    @Override
    public void findAccept()
    {
        findMark = cursor;
    }
    /**
     * Unread to the last findMark. Used after succesfull find.
     */
    @Override
    public void findPushback() throws IOException
    {
        assert findMark >= 0;
        rewind(cursor-findMark);
    }
    /**
     * Resets positions suitable for next find. Used after failed find to continue at next
     * character.
     * @throws IOException
     */
    @Override
    public void findRecover() throws IOException
    {
        assert findSkip >= 0;
        if (findSkip > 0)
        {
            rewind(length-findSkip);
        }
        length = 0;
    }
    /**
     * Rewinds cursor position count characters. Used for unread.
     * @param count
     * @throws IOException
     */
    @Override
    public void rewind(int count) throws IOException
    {
        if (count < 0)
        {
            throw new IllegalArgumentException("negative rewind "+count);
        }
        cursor -= count;
        if (cursor < end - size || cursor < 0)
        {
            throw new IOException("insufficient room in the pushback buffer");
        }
        length -= count;
        if (length < 0)
        {
            throw new IOException("rewinding past input");
        }
        int ld = 0;
        for (int ii=0;ii<count;ii++)
        {
            if (get((cursor+ii)) == '\n')
            {
                ld++;
            }
        }
        if (ld > 0)
        {
            int l = includeLevel.getLine();
            includeLevel.setLine(l - ld);
            int c = 0;
            int start = Math.max(0, end-size);
            for (int ii=cursor;ii>=start;ii--)
            {
                if (get(ii) == '\n')
                {
                    break;
                }
                c++;
            }
            includeLevel.setColumn(c);
        }
        else
        {
            int c = includeLevel.getColumn();
            includeLevel.setColumn(c - count);
        }
    }
    @Override
    public void unread() throws IOException
    {
        rewind(length);
    }
    @Override
    public void unreadLa(int len) throws IOException
    {
        length += len;
        rewind(length);
    }
    @Override
    public void unread(int c) throws IOException
    {
        rewind(1);
    }
    /**
     * Reads from ring buffer or from actual reader.
     * @return
     * @throws IOException
     */
    @Override
    public int read() throws IOException
    {
        assert cursor <= end;
        if (cursor >= end)
        {
            if (includeLevel.in == null)
            {
                return -1;
            }
            int cp = cursor % size;
            int len = Math.min(size-cp, size-(cursor-waterMark));
            int il = fill(includeLevel.in, cp, len);
            if (il == -1)
            {
                if (includeStack != null)
                {
                    while (!includeStack.isEmpty() && il == -1)
                    {
                        close(includeLevel.in);
                        includeLevel = includeStack.pop();
                        il = fill(includeLevel.in, cp, len);
                    }
                    if (il == -1)
                    {
                        return -1;
                    }
                }
                else
                {
                    return -1;
                }
            }
            end+=il;
        }
        int rc = get(cursor++);
        includeLevel.forward(rc);
        length++;
        if (length > size)
        {
            throw new IOException("input size "+length+" exceeds buffer size "+size);
        }
        return rc;
    }
    @Override
    public void reRead(int count) throws IOException
    {
        if (count < 0)
        {
            throw new IOException("count="+count);
        }
        assert cursor <= end;
        for (int ii=0;ii<count;ii++)
        {
            if (cursor >= end)
            {
                throw new IOException("reRead's unread data");
            }
            int rc = get(cursor++);
            includeLevel.forward(rc);
            length++;
            if (length > size)
            {
                throw new IOException("input size "+length+" exceeds buffer size "+size);
            }
        }
    }

    /**
     * Clears input. After that continues to next input token.
     */
    @Override
    public void clear()
    {
        length = 0;
        findSkip = 0;
        findMark = -1;
        waterMark = cursor;
    }

    @Override
    public void close() throws IOException
    {
        if (includeStack != null)
        {
            while (!includeStack.isEmpty())
            {
                close(includeStack.pop().in);
            }
        }
        if (includeLevel.in != null)
        {
            close(includeLevel.in);
        }
    }

    public static ExecutableElement getParseMethod(TypeMirror type, GTerminal terminal)
    {
        if (Typ.isPrimitive(type))
        {
            String name = type.getKind().name().toLowerCase();
            int radix = terminal.getBase();
            if (radix != 10)
            {
                if (radix > 0)
                {
                    name = name+"Radix"+radix;
                }
                else
                {
                    radix = -radix;
                    name = name+"Radix2C"+radix;
                }
            }
            return El.getMethod(InputReader.class, "parse"+name.toUpperCase().substring(0, 1)+name.substring(1));
        }
        else
        {
            if (Typ.isSameType(type, Typ.String))
            {
                return El.getMethod(InputReader.class, "getString");
            }
            throw new IllegalArgumentException("no parse method for non primitive type "+type+" at "+terminal);
        }
    }
    /**
     * Returns true if content is string 'true' ignoring case
     * @return
     */
    @Override
    public boolean parseBoolean()
    {
        return parseBoolean(cursor-length, length);
    }
    /**
     * Converts part of input
     * @param s Start position starting at 0
     * @param l Length
     * @return
     */
    private boolean parseBoolean(int s, int l)
    {
        if (
            l == 4 &&
            (get(s) == 'T' || get(s) == 't') &&
            (get((s+1)) == 'R' || get((s+1)) == 'r') &&
            (get((s+2)) == 'U' || get((s+2)) == 'u') &&
            (get((s+3)) == 'E' || get((s+3)) == 'e')
                )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    /**
     * Returns the only character of string
     * @return
     */
    @Override
    public char parseChar()
    {
        return parseChar(cursor-length, length);
    }
    /**
     * Converts part of input
     * @param s Start position starting at 0
     * @param l Length
     * @return
     */
    private char parseChar(int s, int l)
    {
        if (l != 1)
        {
            throw new IllegalArgumentException("cannot convert "+this+" to char");
        }
        return (char) get(s);
    }
    /**
     * Parses string content to byte "6" -&gt; 6
     * Minus is allowed as first character
     * @return
     */
    @Override
    public byte parseByte()
    {
        return parseByte(cursor-length, length);
    }
    /**
     * Converts part of input
     * @param s Start position starting at 0
     * @param l Length
     * @return
     */
    private byte parseByte(int s, int l)
    {
        int i = parseInt(s, l);
        if (i < Byte.MIN_VALUE || i > 0xff)
        {
            throw new IllegalArgumentException("cannot convert "+this+" to byte");
        }
        return (byte) i;
    }
    /**
     * Parses string content to short "123" -&gt; 123
     * Minus is allowed as first character
     * @return
     */
    @Override
    public short parseShort()
    {
        return parseShort(cursor-length, length);
    }
    /**
     * Converts part of input
     * @param s Start position starting at 0
     * @param l Length
     * @return
     */
    private short parseShort(int s, int l)
    {
        int i = parseInt(s, l);
        if (i < Short.MIN_VALUE || i > 0xffff)
        {
            throw new IllegalArgumentException("cannot convert "+this+" to short");
        }
        return (short) i;
    }
    /**
     * Parses string content to int "123" -&gt; 123
     * Minus is allowed as first character
     * @return
     */
    @Override
    public int parseInt()
    {
        return parseInt(cursor-length, length);
    }
    /**
     * Parses string content to int "011" -&gt; 3
     * 
     * <p>Conversion is 1-complement
     * @return
     */
    @Override
    public int parseIntRadix2()
    {
        return parseInt(cursor-length, length, 2);
    }
    /**
     * Parses string content to int "111" -&gt; -1
     * 
     * <p>Conversion is 2-complement
     * @return
     */
    @Override
    public int parseIntRadix2C2()
    {
        return parseInt(cursor-length, length, -2);
    }
    /**
     * Parses string content to int "011" -&gt; 3
     * 
     * <p>Conversion is 1-complement
     * @return
     */
    @Override
    public long parseLongRadix2()
    {
        return parseLong(cursor-length, length, 2);
    }
    /**
     * Parses string content to int "111" -&gt; -1
     * 
     * <p>Conversion is 2-complement
     * @return
     */
    @Override
    public long parseLongRadix2C2()
    {
        return parseLong(cursor-length, length, -2);
    }
    /**
     * Converts part of input
     * @param s Start position starting at 0
     * @param l Length
     * @return
     */
    private int parseInt(int s, int l)
    {
        int sign = 1;
        int result = 0;
        int start = 0;
        if (l == 0)
        {
            throw new IllegalArgumentException("cannot convert "+this+" to int");
        }
        if (get(s) == '+')
        {
            start = 1;
        }
        else
        {
            if (get(s) == '-')
            {
                sign = -1;
                start = 1;
            }
        }
        for (int j=start;j<l;j++)
        {
            int ii=s+j;
            switch (get(ii))
            {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    result = 10*result + get(ii) - '0';
                    break;
                default:
                    throw new IllegalArgumentException("cannot convert "+this+" to int");
            }
            if (result < 0)
            {
                throw new IllegalArgumentException("cannot convert "+this+" to int");
            }
        }
        return sign*result;
    }
    /**
     * Converts binary to int
     * @param s
     * @param l
     * @param radix
     * @return 
     */
    private int parseInt(int s, int l, int radix)
    {
        assert radix == 2 || radix == -2;
        if (l > 32)
        {
            throw new IllegalArgumentException("bit number "+l+" is too much for int");
        }
        int result = 0;
        int start = 0;
        if (l == 0)
        {
            throw new IllegalArgumentException("cannot convert "+this+" to int");
        }
        for (int j=start;j<l;j++)
        {
            int ii=s+j;
            result <<= 1;
            switch (get(ii))
            {
                case '0':
                    break;
                case '1':
                    result++;
                    break;
                default:
                    throw new IllegalArgumentException("cannot convert "+this+" to int");
            }
        }
        if (radix > 0 || result < (1<<(l-1)))
        {
            return result;
        }
        else
        {
            return result + (-1<<l);
        }
    }
    private long parseLong(int s, int l, int radix)
    {
        assert radix == 2 || radix == -2;
        if (l > 64)
        {
            throw new IllegalArgumentException("bit number "+l+" is too much for long");
        }
        long result = 0;
        int start = 0;
        if (l == 0)
        {
            throw new IllegalArgumentException("cannot convert "+this+" to long");
        }
        for (int j=start;j<l;j++)
        {
            int ii=s+j;
            result <<= 1;
            switch (get(ii))
            {
                case '0':
                    break;
                case '1':
                    result++;
                    break;
                default:
                    throw new IllegalArgumentException("cannot convert "+this+" to long");
            }
        }
        if (radix > 0 || result < (1L<<(l-1)))
        {
            return result;
        }
        else
        {
            return result + (-1L<<l);
        }
    }
    /**
     * Parses string content to long "123" -&gt; 123
     * Minus is allowed as first character
     * @return
     */
    @Override
    public long parseLong()
    {
        return parseLong(cursor-length, length);
    }
    /**
     * Converts part of input
     * @param s Start position starting at 0
     * @param l Length
     * @return
     */
    private long parseLong(int s, int l)
    {
        int sign = 1;
        long result = 0;
        int start = 0;
        if (l == 0)
        {
            throw new IllegalArgumentException("cannot convert "+this+" to int");
        }
        if (get(s) == '+')
        {
            start = 1;
        }
        else
        {
            if (get(s) == '-')
            {
                sign = -1;
                start = 1;
            }
        }
        for (int j=start;j<l;j++)
        {
            int ii=s+j;
            switch (get(ii))
            {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    result = 10*result + get(ii) - '0';
                    break;
                default:
                    throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to long");
            }
            if (result < 0)
            {
                throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to long");
            }
        }
        return sign*result;
    }

    /**
     * Parses string content to float "123.456" -&gt; 123.456
     * Minus is allowed as first character.
     * Decimal separator is dot (.)
     * Scientific notation is supported. E.g -1.23456E-9
     * @return
     */
    @Override
    public float parseFloat()
    {
        return parseFloat(cursor-length, length);
    }
    /**
     * Converts part of input
     * @param s Start position starting at 0
     * @param l Length
     * @return
     */
    private float parseFloat(int s, int l)
    {
        int sign = 1;
        float result = 0;
        int start = 0;
        int decimal = -1;
        boolean decimalPart = false;
        int mantissa = 0;
        int mantissaSign = 1;
        boolean mantissaPart = false;
        if (length == 0)
        {
            throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to float");
        }
        if (get(s) == '+')
        {
            start = 1;
        }
        else
        {
            if (get(s) == '-')
            {
                sign = -1;
                start = 1;
            }
        }
        for (int j=start;j<l;j++)
        {
            int ii=s+j;
            switch (get(ii))
            {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    if (mantissaPart)
                    {
                        mantissa = 10*mantissa + get(ii) - '0';
                    }
                    else
                    {
                        if (decimalPart)
                        {
                            result += (get(ii) - '0')*Math.pow(10, decimal);
                            decimal--;
                        }
                        else
                        {
                            result = 10*result + get(ii) - '0';
                        }
                    }
                    break;
                case '.':
                    decimalPart = true;
                    break;
                case 'E':
                    mantissaPart = true;
                    break;
                case '-':
                    if (!mantissaPart)
                    {
                        throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to float");
                    }
                    mantissaSign = -1;
                    break;
                case '+':
                    if (!mantissaPart)
                    {
                        throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to float");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to float");
            }
            if (result < 0)
            {
                throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to float");
            }
        }
        return (float) (sign * result * Math.pow(10, mantissa*mantissaSign));
    }

    /**
     * Parses string content to double "123.456" -&gt; 123.456
     * Minus is allowed as first character.
     * Decimal separator is dot (.)
     * Scientific notation is supported. E.g -1.23456E-9
     * @return
     */
    @Override
    public double parseDouble()
    {
        return parseDouble(cursor-length, length);
    }
    /**
     * Converts part of input
     * @param s Start position starting at 0
     * @param l Length
     * @return
     */
    private double parseDouble(int s, int l)
    {
        int sign = 1;
        double result = 0;
        int start = 0;
        int decimal = -1;
        boolean decimalPart = false;
        int mantissa = 0;
        int mantissaSign = 1;
        boolean mantissaPart = false;
        if (length == 0)
        {
            throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to double");
        }
        if (get(s) == '+')
        {
            start = 1;
        }
        else
        {
            if (get(s) == '-')
            {
                sign = -1;
                start = 1;
            }
        }
        for (int j=start;j<l;j++)
        {
            int ii=s+j;
            switch (get(ii))
            {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    if (mantissaPart)
                    {
                        mantissa = 10*mantissa + get(ii) - '0';
                    }
                    else
                    {
                        if (decimalPart)
                        {
                            result += (get(ii) - '0')*Math.pow(10, decimal);
                            decimal--;
                        }
                        else
                        {
                            result = 10*result + get(ii) - '0';
                        }
                    }
                    break;
                case '.':
                    decimalPart = true;
                    break;
                case 'E':
                    mantissaPart = true;
                    break;
                case '-':
                    if (!mantissaPart)
                    {
                        throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to double");
                    }
                    mantissaSign = -1;
                    break;
                case '+':
                    if (!mantissaPart)
                    {
                        throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to double");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to double");
            }
            if (result < 0)
            {
                throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to double");
            }
        }
        return (sign * result * Math.pow(10, mantissa*mantissaSign));
    }

    @Override
    public boolean isAtBoundary(int t) throws IOException
    {
        Range.BoundaryType type = Range.BoundaryType.values()[t];
        switch (type)
        {
            case BOL:
                return includeLevel.startOfLine();
            case EOL:
                return ((includeLevel.startOfLine() || !isLineSeparator(peek(0))) && isLineSeparator(peek(1)));
            case WB:
                return ((includeLevel.startOfLine() || !Character.isLetter(peek(0))) && Character.isLetter(peek(1)));
            case NWB:
                return ((!includeLevel.startOfLine() && Character.isLetter(peek(0))) && !Character.isLetter(peek(1)));
            case BOI:
                return end == 0;
            case EOPM:
                throw new UnsupportedOperationException();
            case EOIL:
                int cc = peek(1);
                return isLineSeparator(cc) || cc == -1;
            case EOI:
                return peek(1) == -1;
            default:
                throw new IllegalArgumentException("unknown boundary "+type);
        }
    }

    private boolean isLineSeparator(int cc)
    {
        return cc == '\r' || cc == '\n';
    }

    @Override
    public int getLineNumber()
    {
        return includeLevel.getLine();
    }

    @Override
    public int getColumnNumber()
    {
        return includeLevel.getColumn();
    }

    @Override
    public String getEncoding()
    {
        if (includeLevel.streamReader != null)
        {
            return includeLevel.streamReader.getCharset().name();
        }
        return null;
    }

    @Override
    public int length()
    {
        return length;
    }

    @Override
    public char charAt(int i)
    {
        if (i<0 || i>=length)
        {
            throw new IllegalArgumentException(i+" index out of range");
        }
        return (char) get((cursor-length+i));
    }

    @Override
    public CharSequence subSequence(int s, int e)
    {
        if (s<0 || s>e || s>=length || e>=length)
        {
            throw new IllegalArgumentException("("+s+", "+e+") index out of range");
        }
        return new CharSequenceImpl(cursor-length+s, e-s);
    }

    
    protected class IncludeLevel
    {
        private I in;
        private StreamReader streamReader;
        private int line = 1;
        private int column;
        private String source = "";

        protected IncludeLevel()
        {
        }

        protected IncludeLevel(I in, String source)
        {
            this.in = in;
            this.source = source;
        }

        protected IncludeLevel(I in, StreamReader streamReader, String source)
        {
            this.in = in;
            this.streamReader = streamReader;
            this.source = source;
        }

        protected void reset()
        {
            in = null;
            streamReader = null;
            line = 1;
            column = 0;
            source = "";
        }
        
        protected void setIn(I in)
        {
            this.in = in;
        }

        protected void setStreamReader(StreamReader streamReader)
        {
            this.streamReader = streamReader;
        }

        protected I getIn()
        {
            return in;
        }

        protected StreamReader getStreamReader()
        {
            return streamReader;
        }

        protected String getSource()
        {
            return source;
        }

        protected int getColumn()
        {
            return column;
        }

        protected int getLine()
        {
            return line;
        }
        
        protected boolean startOfLine()
        {
            return column == 0;
        }

        protected void forward(int rc)
        {
            if (rc == '\n')
            {
                line++;
                column = 0;
            }
            else
            {
                column++;
            }
        }

        protected void setColumn(int column)
        {
            this.column = column;
        }

        protected void setLine(int line)
        {
            this.line = line;
        }

        protected void setSource(String source)
        {
            this.source = source;
        }

    }
    public class CharSequenceImpl implements CharSequence
    {
        private final int s;
        private final int l;
        /**
         * Creates a CharSequence using InputReader array as backing store.
         * @param s Start
         * @param l Length
         */
        public CharSequenceImpl(int s, int l)
        {
            this.s = s;
            this.l = l;
        }
        
        @Override
        public int length()
        {
            return l;
        }

        @Override
        public char charAt(int i)
        {
            if (i >= l || i < 0)
            {
                throw new IllegalArgumentException("index "+i+" out of range");
            }
            return (char) get((s+i));
        }

        @Override
        public CharSequence subSequence(int s, int e)
        {
            if (s < 0 || s >= l || e < 0 || e >= l)
            {
                throw new IllegalArgumentException("Illegal sub range");
            }
            return new CharSequenceImpl(this.s+s, e-s);
        }

        @Override
        public String toString()
        {
            return getString(s, l);
        }
        
    }
}
