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
package org.vesalainen.parser.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import org.vesalainen.parser.ParserConstants;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.regex.SyntaxErrorException;

/**
 * Interface for parser input. 
 * 
 * <p>Note! Name InputReader is for compatibility.
 * @author tkv
 */
public interface InputReader extends CharSequence, AutoCloseable
{

    @Override
    public void close() throws IOException;
    
    /**
     * Set current character set. Only supported with InputStreams!
     * @param cs 
     */
    public void setEncoding(String cs);
    /**
     * Set current character set. Only supported with InputStreams!
     * @param cs 
     */
    public void setEncoding(Charset cs);
    /**
     * Set's the source of current input
     * @param source A string describing the input source, like filename.
     */
    public void setSource(String source);
    /**
     * Get's the source of current input 
     * @return A string describing the input source, like filename.
     */
    public String getSource();
    /**
     * Called by the parser. Underlining input (stream/reader) is checked if it
     * implement Recoverable interface. If it does, it's recover method is called.
     * If that method returns true, the parsing continueas at the start. Otherwice
     * throws SyntaxErrorException
     * @throws SyntaxErrorException 
     */
    public void recover() throws SyntaxErrorException;
    /**
     * Called by the parser. Underlining input (stream/reader) is checked if it
     * implement Recoverable interface. If it does, it's recover method is called.
     * If that method returns true, the parsing continueas at the start. Otherwice
     * throws SyntaxErrorException
     * @param thr
     * @throws SyntaxErrorException 
     */
    public void recover(@ParserContext(ParserConstants.THROWABLE) Throwable thr) throws SyntaxErrorException;
    /**
     * Called by the parser. Underlining input (stream/reader) is checked if it
     * implement Recoverable interface. If it does, it's recover method is called.
     * If that method returns true, the parsing continueas at the start. Otherwice
     * throws SyntaxErrorException
     * @param expecting Description of expected input.
     * @param token Description of read input.
     * @throws SyntaxErrorException 
     */
    public void recover(            
            @ParserContext(ParserConstants.ExpectedDescription) String expecting, 
            @ParserContext(ParserConstants.LastToken) String token) throws SyntaxErrorException;
    /**
     * Convenient method for parser to throw SyntaxErrorException
     * @throws SyntaxErrorException 
     */
    public void throwSyntaxErrorException() throws SyntaxErrorException;
    /**
     * Convenient method for parser to throw SyntaxErrorException
     * @param thr
     * @throws SyntaxErrorException 
     */
    public void throwSyntaxErrorException(@ParserContext(ParserConstants.THROWABLE) Throwable thr) throws SyntaxErrorException;
    /**
     * Convenient method for parser to throw SyntaxErrorException
     * @param expecting
     * @param token
     * @throws SyntaxErrorException 
     */
    public void throwSyntaxErrorException(
            @ParserContext(ParserConstants.ExpectedDescription) String expecting, 
            @ParserContext(ParserConstants.LastToken) String token) throws SyntaxErrorException;
    /**
     * Return true if next input is eof
     * @return
     * @throws IOException 
     */
    public boolean isEof() throws IOException;
    /**
     * Synchronizes actual reader to current cursor position
     * @throws IOException
     */
    public void release() throws IOException;
    /**
     * Returns the length of current input
     * @return
     */
    public int getLength();
    /**
     * Returns the start position of current input
     * @return
     */
    public int getStart();
    /**
     * Returns the end position of current input
     * @return
     */
    public int getEnd();
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
    public int getFieldRef();
    /**
     * Returns the last matched input
     * @return 
     */
    public String getString();
    /**
     * Returns the string matched with fieldref
     * @param fieldRef
     * @return string matched with fieldref
     */
    public String getString(int fieldRef);
    /**
     * Returns a CharSequence matched with fieldRef.
     * @param fieldRef
     * @return 
     */
    public CharSequence getCharSequence(int fieldRef);
    /**
     * Returns buffered data as String. Buffered data is ready in array.
     * @return 
     */
    public String buffered();
    /**
     * Returns a CharSequence
     * @param start Start
     * @param length Length
     * @return 
     */
    public CharSequence getCharSequence(int start, int length);
    /**
     * Returns last read line.
     * @return 
     */
    public String getLine();
    /**
     * Returns the input data after last release call
     * @return 
     */
    public String getInput();
    
    /**
     * get a char from input buffer.
     * @param offset 0 is last read char.
     * @return
     * @throws IOException
     */
    public int peek(int offset) throws IOException;
    /**
     * Set how many characters we can skip after failed find.
     * @param acceptStart 
     */
    public void setAcceptStart(int acceptStart);
    /**
     * Marks to position where find could accept the input.
     */
    public void findAccept();
    /**
     * Unread to the last findMark. Used after succesfull find.
     */
    public void findPushback() throws IOException;
    /**
     * Resets positions suitable for next find. Used after failed find to continue at next
     * character.
     * @throws IOException
     */
    public void findRecover() throws IOException;
    /**
     * Rewinds cursor position count characters. Used for unread.
     * @param count
     * @throws IOException
     */
    public void rewind(int count) throws IOException;
    public void unread() throws IOException;
    public void unreadLa(int len) throws IOException;
    public void unread(int c) throws IOException;
    /**
     * Reads from ring buffer or from actual reader.
     * @return
     * @throws IOException
     */
    public int read() throws IOException;
    public void reRead(int count) throws IOException;

    /**
     * Clears input. After that continues to next input token.
     */
    public void clear();

    /**
     * Returns true if content is string 'true' ignoring case
     * @return
     */
    public boolean parseBoolean();
    /**
     * Returns the only character of string
     * @return
     */
    public char parseChar();
    /**
     * Parses string content to byte "6" -&gt; 6
     * Minus is allowed as first character
     * @return
     */
    public byte parseByte();
    /**
     * Parses string content to short "123" -&gt; 123
     * Minus is allowed as first character
     * @return
     */
    public short parseShort();
    /**
     * Parses string content to int "123" -&gt; 123
     * Minus is allowed as first character
     * @return
     */
    public int parseInt();
    /**
     * Parses string content to int "011" -&gt; 3
     * 
     * <p>Conversion is 1-complement
     * @return
     */
    public int parseIntRadix2();
    /**
     * Parses string content to int "111" -&gt; -1
     * 
     * <p>Conversion is 2-complement
     * @return
     */
    public int parseIntRadix2C2();
    /**
     * Parses string content to int "011" -&gt; 3
     * 
     * <p>Conversion is 1-complement
     * @return
     */
    public long parseLongRadix2();
    /**
     * Parses string content to int "111" -&gt; -1
     * 
     * <p>Conversion is 2-complement
     * @return
     */
    public long parseLongRadix2C2();
    /**
     * Parses string content to long "123" -&gt; 123
     * Minus is allowed as first character
     * @return
     */
    public long parseLong();
    /**
     * Parses string content to float "123.456" -&gt; 123.456
     * Minus is allowed as first character.
     * Decimal separator is dot (.)
     * Scientific notation is supported. E.g -1.23456E-9
     * @return
     */
    public float parseFloat();
    /**
     * Parses string content to double "123.456" -&gt; 123.456
     * Minus is allowed as first character.
     * Decimal separator is dot (.)
     * Scientific notation is supported. E.g -1.23456E-9
     * @return
     */
    public double parseDouble();
    /**
     * Returns true if input matches org.vesalainen.regex.Range.BoundaryType
     * @param ordinal BoundaryType ordinal
     * @return
     * @throws IOException 
     * @see org.vesalainen.regex.Range.BoundaryType
     */
    public boolean isAtBoundary(int ordinal) throws IOException;
    /**
     * Returns line number.
     * @return 
     */
    public int getLineNumber();
    /**
     * Returns column number
     * @return 
     */
    public int getColumnNumber();
    /**
     * Returns used character encoding or null.
     * @return 
     */
    public String getEncoding();
    /**
     * Set the usage of OffsetLocatorException
     * @param useOffsetLocatorException 
     */
    public void useOffsetLocatorException(boolean useOffsetLocatorException);
    /**
     * Inserts text at cursor position
     * @param text 
     * @throws java.io.IOException 
     */
    public void insert(char[] text) throws IOException;
    /**
     * Inserts text at cursor position
     * @param text 
     * @throws java.io.IOException 
     */
    public void insert(CharSequence text) throws IOException;
    /**
     * Writes part of buffers content to writer
     * @param start Start of input
     * @param length Input length
     * @param writer
     * @throws IOException 
     */
    public void write(int start, int length, Writer writer) throws IOException;
    /**
     * Writes part of buffers content to writer
     * @param writer
     * @throws IOException 
     */
    public void write(Writer writer) throws IOException;
    /**
     * Returns string from buffer
     * @param start Start of input
     * @param length Length of input
     * @return 
     */
    public String getString(int start, int length);
    /**
     * Include InputStream at current input. InputStream is read as part of 
     * input. When InputStream ends, input continues using current input.
     * 
     * <p>Included stream is closed at eof
     * 
     * @param is Incuded input
     * @param source Description of the source
     * @throws IOException 
     */
    public void include(InputStream is, String source) throws IOException;
    /**
     * Include InputStream at current input. InputStream is read as part of 
     * input. When InputStream ends, input continues using current input.
     * 
     * <p>Included stream is closed at eof
     * 
     * @param is Included input
     * @param cs Character set
     * @param source Description of the source
     * @throws IOException 
     */
    public void include(InputStream is, String cs, String source) throws IOException;
    /**
     * Include InputStream at current input. InputStream is read as part of 
     * input. When InputStream ends, input continues using current input.
     * 
     * <p>Included stream is closed at eof
     * 
     * @param is Incuded input
     * @param cs Character set
     * @param source Description of the source
     * @throws IOException 
     */
    public void include(InputStream is, Charset cs, String source) throws IOException;
    /**
     * Include Reader at current input. Reader is read as part of 
     * input. When Reader ends, input continues using current input.
     * 
     * <p>Included reader is closed at eof
     * 
     * @param in
     * @param source
     * @throws IOException 
     */
    public void include(Reader in, String source) throws IOException;
}
