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
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.zip.Checksum;
import org.vesalainen.parser.ParserConstants;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.regex.SyntaxErrorException;
import org.vesalainen.util.function.IOBooleanSupplier;

/**
 * Interface for parser input. Use Input.getInstance methods to create one.
 * 
 * <p>Note! Name InputReader is for compatibility.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see org.vesalainen.parser.util.Input#getInstance(java.lang.CharSequence) 
 */
public interface InputReader extends CharSequence, AutoCloseable, ModifiableCharset
{

    @Override
    void close() throws IOException;
    
    /**
     * Set's the source of current input
     * @param source A string describing the input source, like filename.
     */
    void setSource(String source);
    /**
     * Get's the source of current input 
     * @return A string describing the input source, like filename.
     */
    String getSource();
    /**
     * Called by the parser. Underlining input (stream/reader) is checked if it
     * implement Recoverable interface. If it does, it's recover method is called.
     * If that method returns true, the parsing continueas at the start. Otherwice
     * throws SyntaxErrorException
     * @throws SyntaxErrorException 
     */
    void recover() throws SyntaxErrorException, IOException;
    /**
     * Called by the parser. Underlining input (stream/reader) is checked if it
     * implement Recoverable interface. If it does, it's recover method is called.
     * If that method returns true, the parsing continueas at the start. Otherwice
     * throws SyntaxErrorException
     * @param thr
     * @throws SyntaxErrorException 
     */
    void recover(@ParserContext(ParserConstants.THROWABLE) Throwable thr) throws SyntaxErrorException, IOException;
    /**
     * Called by the parser. Underlining input (stream/reader) is checked if it
     * implement Recoverable interface. If it does, it's recover method is called.
     * If that method returns true, the parsing continueas at the start. Otherwice
     * throws SyntaxErrorException
     * @param expecting Description of expected input.
     * @param token Description of read input.
     * @throws SyntaxErrorException 
     */
    void recover(            
            @ParserContext(ParserConstants.ExpectedDescription) String expecting, 
            @ParserContext(ParserConstants.LastToken) String token) throws SyntaxErrorException, IOException;
    /**
     * Convenient method for parser to throw SyntaxErrorException
     * @throws SyntaxErrorException 
     */
    void throwSyntaxErrorException() throws SyntaxErrorException;
    /**
     * Convenient method for parser to throw SyntaxErrorException
     * @param thr
     * @throws SyntaxErrorException 
     */
    void throwSyntaxErrorException(@ParserContext(ParserConstants.THROWABLE) Throwable thr) throws SyntaxErrorException;
    /**
     * Convenient method for parser to throw SyntaxErrorException
     * @param expecting
     * @param token
     * @throws SyntaxErrorException 
     */
    void throwSyntaxErrorException(
            @ParserContext(ParserConstants.ExpectedDescription) String expecting, 
            @ParserContext(ParserConstants.LastToken) String token) throws SyntaxErrorException;
    /**
     * Return true if next input is eof
     * @return
     * @throws IOException 
     */
    boolean isEof() throws IOException;
    /**
     * Sets eof detection function.
     * @param eofFunc 
     */
    void setEof(IOBooleanSupplier eofFunc);
    /**
     * Synchronizes actual reader to current cursor position
     * @throws IOException
     */
    void release() throws IOException;
    /**
     * returns character at index
     * @param index
     * @return 
     */
    int get(long index);
    /**
     * Returns the length of current input
     * @return
     */
    int getLength();
    /**
     * Returns the start position of current input
     * @return
     */
    long getStart();
    /**
     * Returns the end position of current input
     * @return
     */
    long getEnd();
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
    int getFieldRef();
    /**
     * Returns the last matched input
     * @return 
     */
    String getString();
    /**
     * Returns field start extracted from fieldRef.
     * @param fieldRef
     * @return 
     */
    int getStart(int fieldRef);
    /**
     * Returns field length extracted from fieldRef.
     * @param fieldRef
     * @return 
     */
    int getLength(int fieldRef);
    /**
     * Returns the string matched with fieldref
     * @param fieldRef
     * @return string matched with fieldref
     */
    String getString(int fieldRef);
    /**
     * Returns a CharSequence matched with fieldRef.
     * @param fieldRef
     * @return 
     */
    CharSequence getCharSequence(int fieldRef);
    /**
     * Returns buffered data as String. Buffered data is ready in array.
     * @return 
     */
    String buffered();
    /**
     * Returns a CharSequence
     * @param start Start
     * @param length Length
     * @return 
     */
    CharSequence getCharSequence(long start, int length);
    /**
     * Returns last read line.
     * @return 
     */
    String getLine();
    /**
     * Returns the input data after last release call
     * @return 
     */
    String getInput();
    
    /**
     * get a char from input buffer.
     * @param offset 0 is last read char.
     * @return
     * @throws IOException
     */
    int peek(int offset) throws IOException;
    /**
     * Set how many characters we can skip after failed find.
     * @param acceptStart 
     */
    void setAcceptStart(int acceptStart);
    /**
     * Marks to position where find could accept the input.
     */
    void findAccept();
    /**
     * Unread to the last findMark. Used after succesfull find.
     */
    void findPushback() throws IOException;
    /**
     * Resets positions suitable for next find. Used after failed find to continue at next
     * character.
     * @throws IOException
     */
    void findRecover() throws IOException;
    /**
     * Rewinds cursor position count characters. Used for unread.
     * @param count
     * @throws IOException
     */
    void rewind(int count) throws IOException;
    void unread() throws IOException;
    void unreadLa(int len) throws IOException;
    void unread(int c) throws IOException;
    /**
     * Reads from ring buffer or from actual reader.
     * @return
     * @throws IOException
     */
    int read() throws IOException;
    /**
     * Reads times count. This is for testing!
     * @param times
     * @throws IOException 
     */
    void read(int times) throws IOException;
    void reRead(int count) throws IOException;

    /**
     * Clears input. After that continues to next input token.
     */
    void clear();

    /**
     * Returns true if content is string 'true' ignoring case
     * @return
     */
    boolean parseBoolean();
    /**
     * Returns true if content is string 'true' ignoring case
     * @param start
     * @param length
     * @return
     */
    boolean parseBoolean(long start, int length);
    /**
     * Returns the only character of string
     * @return
     */
    char parseChar();
    /**
     * Returns the only character of string
     * @param start
     * @param length
     * @return
     */
    char parseChar(long start, int length);
    /**
     * Parses string content to byte "6" -&gt; 6
     * Minus is allowed as first character
     * @return
     */
    byte parseByte();
    /**
     * Parses string content to byte "6" -&gt; 6
     * Minus is allowed as first character
     * @param start
     * @param length
     * @return
     */
    byte parseByte(long start, int length);
    /**
     * Parses string content to short "123" -&gt; 123
     * Minus is allowed as first character
     * @return
     */
    short parseShort();
    /**
     * Parses string content to short "123" -&gt; 123
     * Minus is allowed as first character
     * @param start
     * @param length
     * @return
     */
    short parseShort(long start, int length);
    /**
     * Parses string content to int "123" -&gt; 123
     * Minus is allowed as first character
     * @return
     */
    int parseInt();
    /**
     * Parses string content to int "123" -&gt; 123
     * Minus is allowed as first character
     * @param start
     * @param length
     * @return
     */
    int parseInt(long start, int length);
    /**
     * Parses string content to int "123" -&gt; 123
     * Minus is allowed as first character
     * @param start
     * @param length
     * @param radix
     * @return
     */
    int parseInt(long start, int length, int radix);
    /**
     * @deprecated Use parseInt(this, 2)
     * Parses string content to int "011" -&gt; 3
     * 
     * <p>Conversion is 1-complement
     * @return
     */
    int parseIntRadix2();
    /**
     * @deprecated Use parseInt(this, -2)
     * Parses string content to int "111" -&gt; -1
     * 
     * <p>Conversion is 2-complement
     * @return
     */
    int parseIntRadix2C2();
    /**
     * @deprecated Use parseLong(this, 2)
     * Parses string content to int "011" -&gt; 3
     * 
     * <p>Conversion is 1-complement
     * @return
     */
    long parseLongRadix2();
    /**
     * @deprecated Use parseLong(this, -2)
     * Parses string content to int "111" -&gt; -1
     * 
     * <p>Conversion is 2-complement
     * @return
     */
    long parseLongRadix2C2();
    /**
     * Parses string content to long "123" -&gt; 123
     * Minus is allowed as first character
     * @return
     */
    long parseLong();
    /**
     * Parses string content to long "123" -&gt; 123
     * Minus is allowed as first character
     * @param start
     * @param length
     * @return
     */
    long parseLong(long start, int length);
    /**
     * Parses string content to long "123" -&gt; 123
     * Minus is allowed as first character
     * @param start
     * @param length
     * @param radix
     * @return
     */
    long parseLong(long start, int length, int radix);
    /**
     * Parses string content to float "123.456" -&gt; 123.456
     * Minus is allowed as first character.
     * Decimal separator is dot (.)
     * Scientific notation is supported. E.g -1.23456E-9
     * @return
     */
    float parseFloat();
    /**
     * Parses string content to float "123.456" -&gt; 123.456
     * Minus is allowed as first character.
     * Decimal separator is dot (.)
     * Scientific notation is supported. E.g -1.23456E-9
     * @param start
     * @param length
     * @return
     */
    float parseFloat(long start, int length);
    /**
     * Parses string content to double "123.456" -&gt; 123.456
     * Minus is allowed as first character.
     * Decimal separator is dot (.)
     * Scientific notation is supported. E.g -1.23456E-9
     * @return
     */
    double parseDouble();
    /**
     * Parses string content to double "123.456" -&gt; 123.456
     * Minus is allowed as first character.
     * Decimal separator is dot (.)
     * Scientific notation is supported. E.g -1.23456E-9
     * @param start
     * @param length
     * @return
     */
    double parseDouble(long start, int length);
    /**
     * Returns true if input matches org.vesalainen.regex.Range.BoundaryType
     * @param ordinal BoundaryType ordinal
     * @return
     * @throws IOException 
     * @see org.vesalainen.regex.Range.BoundaryType
     */
    boolean isAtBoundary(int ordinal) throws IOException;
    /**
     * Returns line number.
     * @return 
     */
    int getLineNumber();
    /**
     * Returns column number
     * @return 
     */
    int getColumnNumber();
    /**
     * Returns used character encoding or null.
     * @return 
     */
    String getEncoding();
    /**
     * Inserts text at cursor position
     * 
     * <p>Optional method. Use @ParserFeature.UseInsert in @ParseMethod if
     * called outside parser. For example in reducer.
     * @see org.vesalainen.parser.ParserFeature#UseInsert
     * 
     * @param text 
     * @throws java.io.IOException 
     */
    void insert(char[] text) throws IOException;
    /**
     * Inserts text at cursor position
     * 
     * <p>Optional method. Use @ParserFeature.UseInsert in @ParseMethod if
     * called outside parser. For example in reducer.
     * @see org.vesalainen.parser.ParserFeature#UseInsert
     * 
     * @param text 
     * @throws java.io.IOException 
     */
    void insert(CharSequence text) throws IOException;
    /**
     * Writes part of buffers content to writer
     * @param start Start of input
     * @param length Input length
     * @param writer
     * @throws IOException 
     */
    void write(long start, int length, Writer writer) throws IOException;
    /**
     * Writes part of buffers content to writer
     * @param writer
     * @throws IOException 
     */
    void write(Writer writer) throws IOException;
    /**
     * Returns string from buffer
     * @param start Start of input
     * @param length Length of input
     * @return 
     */
    String getString(long start, int length);
    /**
     * Include InputStream at current input. InputStream is read as part of 
     * input. When InputStream ends, input continues using current input.
     * 
     * <p>Included stream is closed at eof
     * 
     * <p>Optional method. Use @ParserFeature.UseInclude in @ParseMethod
     * @see org.vesalainen.parser.ParserFeature#UseInclude
     * 
     * @param is Incuded input
     * @param source Description of the source
     * @throws IOException 
     */
    void include(InputStream is, String source) throws IOException;
    /**
     * Include InputStream at current input. InputStream is read as part of 
     * input. When InputStream ends, input continues using current input.
     * 
     * <p>Included stream is closed at eof
     * 
     * <p>Optional method. Use @ParserFeature.UseInclude in @ParseMethod
     * @see org.vesalainen.parser.ParserFeature#UseInclude
     * 
     * @param is Included input
     * @param cs Character set
     * @param source Description of the source
     * @throws IOException 
     */
    void include(InputStream is, String cs, String source) throws IOException;
    /**
     * Include InputStream at current input. InputStream is read as part of 
     * input. When InputStream ends, input continues using current input.
     * 
     * <p>Included stream is closed at eof
     * 
     * <p>Optional method. Use @ParserFeature.UseInclude in @ParseMethod
     * @see org.vesalainen.parser.ParserFeature#UseInclude
     * 
     * @param is Incuded input
     * @param cs Character set
     * @param source Description of the source
     * @throws IOException 
     */
    void include(InputStream is, Charset cs, String source) throws IOException;
    /**
     * Include Reader at current input. Reader is read as part of 
     * input. When Reader ends, input continues using current input.
     * 
     * <p>Included reader is closed at eof
     * 
     * <p>Optional method. Use @ParserFeature.UseInclude in @ParseMethod
     * @see org.vesalainen.parser.ParserFeature#UseInclude
     * 
     * @param in
     * @param source
     * @throws IOException 
     */
    void include(Readable in, String source) throws IOException;
    /**
     * Reuse text as new input.
     * 
     * <p>This method is optional and implemented only with original text input.
     * @param text 
     * @see org.vesalainen.parser.util.Input#getInstance(java.lang.CharSequence) 
     */
    void reuse(CharSequence text);
    /**
     * Returns backing array or null.
     * @return 
     */
    Object getArray();
    /**
     * Set Checksum class. Class will be updated with the parsed data.
     * @param checksum 
     * @param lookaheadLength 
     */
    void setChecksum(Checksum checksum, int lookaheadLength);
    /**
     * Returns checksum. Only getValue and reset methods are usable.
     * @return 
     */
    Checksum getChecksum();
}
