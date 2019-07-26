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
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackReader;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Checksum;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.Typ;
import org.vesalainen.grammar.GTerminal;
import org.vesalainen.io.Pushbackable;
import org.vesalainen.io.Rewindable;
import org.vesalainen.lang.Primitives;
import org.vesalainen.parser.ParserConstants;
import org.vesalainen.parser.ParserFeature;
import static org.vesalainen.parser.ParserFeature.*;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.regex.CharRange;
import org.vesalainen.regex.SyntaxErrorException;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.function.IOBooleanSupplier;
import org.xml.sax.InputSource;

/**
 * A base class for parser input
 * <p>Use getInstance method for getting InputReader instance
 * <p>Arrays with offset and length are not directly supported for byte[] and 
 * char[] and must be wrapped with Byte- or CharBuffer.
 * @author Timo Vesalainen
 * @param <I> Input type. CharSequence, ByteBuffer, Path, File, Reader, byte[], char[], URL, URI, ReadableByteChannel, InputStream and InputSource
 * @param <B>
 * @see java.lang.CharSequence
 * @see java.io.InputStream
 * @see java.io.File
 * @see java.io.Reader
 * @see java.nio.ByteBuffer
 * @see java.nio.file.Path
 * @see java.nio.channels.ReadableByteChannel
 * @see java.net.URL
 * @see java.net.URI
 * @see org.xml.sax.InputSource
 */
public abstract class Input<I,B extends Buffer> implements InputReader
{
    private static final Set<ParserFeature> NO_FEATURES = Collections.EMPTY_SET;
    private static final int BUFFER_SIZE = 8192;
    private static final long FILE_LENGTH_LIMIT = 100000;
    private static final Map<Class<?>,MethodHandle> inputMap = new HashMap<>();
    
    static
    {
        try
        {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodType mt = MethodType.methodType(InputReader.class, CharSequence.class, int.class, Charset.class, Set.class);
            MethodHandle mh = lookup.findStatic(Input.class, "getInput", mt);
            inputMap.put(mt.parameterType(0), mh);
            inputMap.put(String.class, mh);
            for (Class<?> type : new Class<?>[]{ByteBuffer.class, File.class, URI.class, URL.class, Path.class, InputSource.class, InputStream.class, Reader.class, char[].class, byte[].class, ReadableByteChannel.class})
            {
                mt = mt.changeParameterType(0, type);
                mh = lookup.findStatic(Input.class, "getInput", mt);
                inputMap.put(mt.parameterType(0), mh);
            }
        }
        catch (NoSuchMethodException | IllegalAccessException ex)
        {
            Logger.getLogger(Input.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected B buffer1;
    protected B buffer2;
    protected B[] array1;  // 1 length buffer
    protected B[] array2;  // 2 length buffer
    protected int size;           // size of ring buffer (=buffer.length)
    protected long end;            // position of last actual read char
    protected long cursor;         // position of current input
    protected IncludeLevel includeLevel = new IncludeLevel();
    protected Deque<IncludeLevel> includeStack;
    protected int length;         // length of current input
    protected int findSkip;       // number of characters the find can skip after unsucces
    protected long findMark = -1;  // position where find could have last accessed the string
    protected long waterMark = 0;  // lowest position where buffer can be reused
    protected Set<ParserFeature> features;
    protected ChecksumWrapper checksum;
    private IOBooleanSupplier eofFunc = ()->peek(1)==-1;
    
    protected abstract void set(long index, int value);
    protected abstract int fill(I input, B[] array) throws IOException;
    protected abstract void unread(I input) throws IOException;
    protected abstract void close(I input) throws IOException;
    /**
     * Makes room in buffer for insert. 
     * @param ln
     */
    protected abstract void makeRoom(int ln);
            
    protected Input(Set<ParserFeature> features)
    {
        this.features = features;
    }
    /**
     * Returns supported input types.
     * @return 
     */
    public static Set<Class<?>> getSupportedInputTypes()
    {
        return Collections.unmodifiableSet(inputMap.keySet());
    }
    /**
     * Returns InputReader for input
     * @param <T>
     * @param input Any supported input type
     * @return
     * @throws IOException 
     */
    public static <T> InputReader getInstance(T input) throws IOException
    {
        return getInstance(input, -1, UTF_8, NO_FEATURES);
    }
    /**
     * Returns InputReader for input
     * @param <T>
     * @param input Any supported input type
     * @param size Ring-buffer size
     * @return
     * @throws IOException 
     */
    public static <T> InputReader getInstance(T input, int size) throws IOException
    {
        return getInstance(input, size, UTF_8, NO_FEATURES);
    }
    /**
     * Returns InputReader for input
     * @param <T>
     * @param input Any supported input type
     * @param size Ring-buffer size
     * @param cs Character set
     * @return
     * @throws IOException 
     */
    public static <T> InputReader getInstance(T input, int size, String cs) throws IOException
    {
        return getInstance(input, size, Charset.forName(cs), NO_FEATURES);
    }
    /**
     * Returns InputReader for input
     * @param <T>
     * @param input Any supported input type
     * @param size Ring-buffer size
     * @param cs Character set
     * @return
     * @throws IOException 
     */
    public static <T> InputReader getInstance(T input, int size, Charset cs) throws IOException
    {
        return getInstance(input, size, cs, NO_FEATURES);
    }
    /**
     * Returns InputReader for input
     * @param <T>
     * @param input Any supported input type
     * @param size Ring-buffer size
     * @param features Needed features
     * @return
     * @throws IOException 
     */
    public static <T> InputReader getInstance(T input, int size, Set<ParserFeature> features) throws IOException
    {
        return getInstance(input, size, UTF_8, features);
    }
    /**
     * Returns InputReader for input
     * @param <T>
     * @param input Any supported input type
     * @param features Needed features
     * @return
     * @throws IOException 
     */
    public static <T> InputReader getInstance(T input, Set<ParserFeature> features) throws IOException
    {
        return getInstance(input, -1, UTF_8, features);
    }
    /**
     * Returns InputReader for input
     * @param <T>
     * @param input Any supported input type
     * @param cs Character set
     * @param features Needed features
     * @return
     * @throws IOException 
     */
    public static <T> InputReader getInstance(T input, String cs, Set<ParserFeature> features) throws IOException
    {
        return getInstance(input, -1, Charset.forName(cs), features);
    }
    /**
     * Returns InputReader for input
     * @param <T>
     * @param input Any supported input type
     * @param cs Character set
     * @param features Needed features
     * @return
     * @throws IOException 
     */
    public static <T> InputReader getInstance(T input, Charset cs, Set<ParserFeature> features) throws IOException
    {
        return getInstance(input, -1, cs, features);
    }
    /**
     * Returns InputReader for input
     * @param <T>
     * @param input Any supported input type
     * @param size Ring-buffer size
     * @param cs Character set
     * @param features Needed features
     * @return
     * @throws IOException 
     */
    public static <T> InputReader getInstance(T input, int size, String cs, Set<ParserFeature> features) throws IOException
    {
        return getInstance(input, size, Charset.forName(cs), features);
    }
    /**
     * Returns InputReader for input
     * @param <T>
     * @param input Any supported input type
     * @param size Ring-buffer size
     * @param cs Character set
     * @param features Needed features
     * @return
     * @throws IOException 
     */
    public static <T> InputReader getInstance(T input, int size, Charset cs, Set<ParserFeature> features) throws IOException
    {
        try
        {
            Class<?> inputClass = input.getClass();
            MethodHandle mh = inputMap.get(inputClass);
            if (mh != null)
            {
                return (InputReader) mh.invoke(input, size, cs, features);
            }
            else
            {
                for (Entry<Class<?>,MethodHandle> e : inputMap.entrySet())
                {
                    if (e.getKey().isAssignableFrom(inputClass))
                    {
                        mh = e.getValue();
                        break;
                    }
                }
                if (mh == null)
                {
                    throw new IOException(input+" not assignable to any of "+inputMap.keySet());
                }
            }
            return (InputReader) mh.invoke(input, size, cs, features);
        }
        catch (Throwable ex)
        {
            if (ex instanceof IOException)
            {
                throw (IOException)ex;
            }
            else
            {
                throw new IOException(ex);
            }
        }
    }
    public static InputReader getInstance(CharSequence input)
    {
        try
        {
            return getInstance(input, -1, UTF_8, NO_FEATURES);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    /**
     * Creates an InputReader
     * @param in
     * @param shared Shared ringbuffer.
     * @return 
     */
    public static InputReader getInstance(Reader in, char[] shared)
    {
        Set<ParserFeature> features = NO_FEATURES;
        return new ReadableInput(getFeaturedReader(in, shared.length, features), shared, features);
    }
    
    protected static InputReader getInput(URI uri, int size, Charset cs, Set<ParserFeature> features) throws IOException
    {
        return getInput(uri.toURL(), size, cs, features);
    }
    protected static InputReader getInput(URL url, int size, Charset cs, Set<ParserFeature> features) throws IOException
    {
        return getInput(url.openStream(), size, cs, features);
    }
    protected static InputReader getInput(File file, int size, Charset cs, Set<ParserFeature> features) throws IOException
    {
        return getInput(file.toPath(), size, cs, features);
    }
    protected static InputReader getInput(Path path, int size, Charset cs, Set<ParserFeature> features) throws IOException
    {
        return getInput(Files.newByteChannel(path), size==-1?BUFFER_SIZE:size, cs, features);
    }
    protected static InputReader getInput(InputStream is, int size, Charset cs, Set<ParserFeature> features) throws IOException
    {
        return getInput(Channels.newChannel(is), size==-1?BUFFER_SIZE:size, cs, features);
    }
    protected static InputReader getInput(Reader in, int size, Charset cs, Set<ParserFeature> features)
    {
        return new ReadableInput(getFeaturedReader(in, size==-1?BUFFER_SIZE:size, features), size==-1?BUFFER_SIZE:size, features);
    }
    protected static InputReader getInput(CharSequence text, int size, Charset cs, Set<ParserFeature> features)
    {
        if (features.contains(UsePushback))
        {
            return new ReadableInput(text, size==-1?text.length()*2:size, features);
        }
        else
        {
            if (features.contains(LowerCase))
            {
                return new ReadableInput(CharSequences.toLower(text), size==-1?text.length():size, features);
            }
            else
            {
                if (features.contains(UpperCase))
                {
                    return new ReadableInput(CharSequences.toUpper(text), size==-1?text.length():size, features);
                }
                else
                {
                    if (size == -1)
                    {
                        return new ReadableInput(text, features);
                    }
                    else
                    {
                        return new ReadableInput(text, size, features);
                    }
                }
            }
        }
    }
    protected static InputReader getInput(char[] array, int size, Charset cs, Set<ParserFeature> features)
    {
        return getInput(CharBuffer.wrap(array), size, cs, features);
    }
    protected static InputReader getInput(byte[] array, int size, Charset cs, Set<ParserFeature> features)
    {
        if (canUseUsAscii(cs, features))
        {
            return getInput(CharSequences.getAsciiCharSequence(array), size, cs, features);
        }
        else
        {
            return getInput(new String(array, cs), size, cs, features);
        }
    }
    protected static InputReader getInput(ByteBuffer bb, int size, Charset cs, Set<ParserFeature> features)
    {
        if (canUseUsAscii(cs, features))
        {
            return new ScatteringByteChannelInput(bb, features);
        }
        else
        {
            return new ByteBufferInput(bb, size==-1?BUFFER_SIZE:size, cs, features);
        }
    }
    protected static InputReader getInput(ReadableByteChannel input, int size, Charset cs, Set<ParserFeature> features) throws IOException
    {
        if (input instanceof FileChannel)
        {
            FileChannel fc = (FileChannel) input;
            if (fc.size()> FILE_LENGTH_LIMIT)
            {
                MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                return getInput(mbb, size, cs, features);
            }
        }
        if (input instanceof ScatteringByteChannel)
        {
            if (canUseUsAscii(cs, features))
            {
                ScatteringByteChannel sbc = (ScatteringByteChannel) input;
                return new ScatteringByteChannelInput(sbc, size==-1?BUFFER_SIZE:size, features);
            }
        }
        return new ReadableInput(getFeaturedReadable(input, cs, features), size==-1?BUFFER_SIZE:size, features);
    }
    /**
     * Creates an InputReader
     * @param input
     * @param size Ringbuffer size
     * @return
     * @throws IOException 
     */
    protected static InputReader getInput(InputSource input, int size, Charset cs, Set<ParserFeature> fea) throws IOException
    {
        EnumSet<ParserFeature> features = EnumSet.of(UseInclude, UsePushback, UseModifiableCharset);
        InputReader inputReader = null;
        Reader reader = input.getCharacterStream();
        if (reader != null)
        {
            inputReader = new ReadableInput(getFeaturedReader(reader, size, features), size, features);
        }
        else
        {
            InputStream is = input.getByteStream();
            String encoding = input.getEncoding();
            if (is != null)
            {
                if (encoding != null)
                {
                    inputReader = getInstance(is, size, encoding, features);
                }
                else
                {
                    inputReader = getInstance(is, size, StandardCharsets.US_ASCII, features);
                }
            }
            else
            {
                String sysId = input.getSystemId();
                try
                {
                    URI uri = new URI(sysId);
                    if (encoding != null)
                    {
                        inputReader = getInstance(uri, size, encoding, features);
                    }
                    else
                    {
                        inputReader = getInstance(uri, size, StandardCharsets.US_ASCII, features);
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
    private static boolean canUseUsAscii(Charset cs, Set<ParserFeature> features)
    {
        return (    StandardCharsets.US_ASCII.contains(cs) && 
                !(
                features.contains(UseModifiableCharset) ||
                features.contains(UpperCase) ||
                features.contains(LowerCase) ||
                features.contains(UsePushback) ||
                features.contains(UseInclude)
                )
                );
    }

    protected static Readable getFeaturedReadable(ReadableByteChannel channel, Charset cs, Set<ParserFeature> features)
    {
        if (features.contains(UpperCase) || features.contains(LowerCase))
        {
            return new CaseChangePushbackByteChannelReadable(channel, cs, BUFFER_SIZE, features.contains(UseDirectBuffer), !features.contains(UseModifiableCharset), features.contains(UpperCase));
        }
        else
        {
            if (features.contains(UsePushback))
            {
                return new PushbackByteChannelReadable(channel, cs, BUFFER_SIZE, features.contains(UseDirectBuffer), !features.contains(UseModifiableCharset));
            }
            else
            {
                return new ByteChannelReadable(channel, cs, BUFFER_SIZE, features.contains(UseDirectBuffer), !features.contains(UseModifiableCharset));
            }
        }
    }
    protected static Reader getFeaturedReader(Reader reader, int size, Set<ParserFeature> features)
    {
        if (features.contains(UpperCase) || features.contains(LowerCase))
        {
            checkRecoverable(reader);
            reader = new CaseChangeReader(reader, features.contains(UpperCase));
        }
        if (features.contains(UsePushback) || features.contains(UseInclude))
        {
            checkRecoverable(reader);
            reader = new PushbackReader(reader, size);
        }
        return reader;
    }
    /**
     * Set current character set. Only supported with byte input!
     * @param cs
     * @param fixedCharset
     * @see org.vesalainen.parser.ParserFeature#UseModifiableCharset
     */
    @Override
    public void setCharset(String cs, boolean fixedCharset)
    {
        setCharset(Charset.forName(cs), fixedCharset);
    }
    /**
     * Set current character set. Only supported with byte input!
     * @param cs 
     * @param fixedCharset 
     * @see org.vesalainen.parser.ParserFeature#UseModifiableCharset
     */
    @Override
    public void setCharset(Charset cs, boolean fixedCharset)
    {
        if (includeLevel.in instanceof ModifiableCharset)
        {
            ModifiableCharset sr = (ModifiableCharset) includeLevel.in;
            sr.setCharset(cs, fixedCharset);
        }
        else
        {
            throw new UnsupportedOperationException("setting charset not supported with current input "+includeLevel.in);
        }
    }

    /**
     * Set's the source of current input
     * @param source A string describing the input source, like filename.
     */
    @Override
    public void setSource(String source)
    {
        includeLevel.source = source;
    }
    /**
     * Get's the source of current input 
     * @return A string describing the input source, like filename.
     */
    @Override
    public String getSource()
    {
        return includeLevel.source;
    }
    
    private static void checkRecoverable(Object in)
    {
        if (in instanceof Recoverable)
        {
            throw new UnsupportedOperationException("Recoverable not supported with current features.");
        }
    }
    
    @Override
    public void recover() throws SyntaxErrorException, IOException
    {
        if (! tryRecover())
        {
            throwSyntaxErrorException(null);
        }
    }
    @Override
    public void recover(@ParserContext(ParserConstants.THROWABLE) Throwable thr) throws SyntaxErrorException, IOException
    {
        if (! tryRecover())
        {
            throwSyntaxErrorException(thr);
        }
    }
    @Override
    public void recover(            
            @ParserContext(ParserConstants.ExpectedDescription) String expecting, 
            @ParserContext(ParserConstants.LastToken) String token) throws SyntaxErrorException, IOException

    {
        if (! tryRecover())
        {
            throwSyntaxErrorException(expecting, token);
        }
    }
    private boolean tryRecover() throws IOException
    {
        if (includeLevel.in instanceof Recoverable)
        {
            Recoverable recoverable = (Recoverable) includeLevel.in;
            if (recoverable.recover(
                                getErrorMessage(),
                                getSource(),
                                getLineNumber(),
                                getColumnNumber()))
            {
                clear();
                end = cursor;
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
        String source = includeLevel.source;
        if (features.contains(UseOffsetLocatorException))
        {
            throw new OffsetLocatorException("syntax error", source, getStart(), getEnd(), includeLevel.lastChar, thr);
        }
        else
        {
            int line = getLineNumber();
            int column = getColumnNumber();
            throw new LineLocatorException(
                    getErrorMessage(),
                    source,
                    line,
                    column,
                    includeLevel.lastChar, 
                    thr
                    );
        }
    }

    private String getErrorMessage()
    {
        return "source: "+includeLevel.source+"\n"+
                    "syntax error at line "+includeLevel.line+": pos "+includeLevel.column+
                    "\n"+
                    getLine()+
                    "\n"+
                    pointer(getColumnNumber());
    }
    @Override
    public void throwSyntaxErrorException(
            @ParserContext(ParserConstants.ExpectedDescription) String expecting, 
            @ParserContext(ParserConstants.LastToken) String token) throws SyntaxErrorException
    {
        String source = includeLevel.source;
        if (features.contains(UseOffsetLocatorException))
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
    public final boolean isEof() throws IOException
    {
        return eofFunc.getAsBoolean();
    }
    /**
     * Sets function for eof. This function is called when parsing is in accepting
     * state to check if there are more input.
     * @param eofFunc 
     */
    @Override
    public final void setEof(IOBooleanSupplier eofFunc)
    {
        this.eofFunc = eofFunc;
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
            if (end % size < cursor % size)
            {
                buffer2.position(0);
                buffer2.limit((int)(end % size));
                buffer1.position((int)(cursor % size));
                buffer1.limit(size);
            }
            else
            {
                buffer2.position((int)(cursor % size));
                buffer2.limit((int)(end % size));
                buffer1.position(size);
            }
            if (features.contains(UsePushback))
            {
                if (includeLevel.in instanceof Pushbackable)
                {
                    Pushbackable p = (Pushbackable) includeLevel.in;
                    p.pushback(array2);
                }
                else
                {
                    unread(includeLevel.in);
                }
            }
            else
            {
                if (includeLevel.in instanceof Rewindable)
                {
                    Rewindable rewindable = (Rewindable) includeLevel.in;
                    rewindable.rewind((int)(end-cursor));
                }
                else
                {
                    unread(includeLevel.in);
                }
            }
            buffer1.clear();
            buffer2.clear();
            end = cursor;
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
    public long getStart()
    {
        return cursor-length;
    }
    /**
     * Returns the end position of current input
     * @return
     */
    @Override
    public long getEnd()
    {
        return cursor;
    }
    /**
     * @deprecated Not that feasible
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
        return (int) ((cursor-length) % size + length * 0x10000);
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
        return getString(cursor, (int) (end-cursor));
    }
    /**
     * Returns a CharSequence
     * @param s Start
     * @param l Length
     * @return 
     */
    @Override
    public CharSequence getCharSequence(long s, int l)
    {
        return new CharSequenceImpl(s, l);
    }
    @Override
    public String getLine()
    {
        int c = includeLevel.column;
        if (cursor-c < end-size)
        {
            int len = size / 2;
            return "... "+getString(end-len, len);
        }
        else
        {
            return getString(cursor-c, (int) (end-(cursor-c)));
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
        long target = cursor + offset - 1;
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
     * Unread to the last findMark. Used after successful find.
     * @throws java.io.IOException
     */
    @Override
    public void findPushback() throws IOException
    {
        assert findMark >= 0;
        rewind((int) (cursor-findMark));
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
            int l = includeLevel.line;
            includeLevel.line = l - ld;
            int c = 0;
            long start = Math.max(0, end-size);
            for (long ii=cursor;ii>=start;ii--)
            {
                if (get(ii) == '\n')
                {
                    break;
                }
                c++;
            }
            includeLevel.column = c;
        }
        else
        {
            int c = includeLevel.column;
            includeLevel.column = c - count;
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

    @Override
    public void read(int times) throws IOException
    {
        for (int ii=0;ii<times;ii++)
        {
            read();
        }
    }
    
    /**
     * Reads from ring buffer or from actual reader.
     * @return
     * @throws IOException
     */
    @Override
    public final int read() throws IOException
    {
        assert cursor <= end;
        if (cursor >= end)
        {
            if (includeLevel.in == null)
            {
                return -1;
            }
            int cp = (int)(cursor % size);
            long len = size-(cursor-waterMark);
            int il;
            if (len > size - cp)
            {
                buffer1.position(cp);
                buffer1.limit(size);
                buffer2.position(0);
                buffer2.limit((int)(len-(size-cp)));
                if (!buffer1.hasRemaining() && !buffer2.hasRemaining())
                {
                    throw new UnderflowException("Buffer size="+size+" too small for operation");
                }
                il = fill(includeLevel.in, array2);
            }
            else
            {
                buffer1.position(cp);
                buffer1.limit((int)(cp+len));
                if (!buffer1.hasRemaining())
                {
                    throw new UnderflowException("Buffer size="+size+" too small for operation");
                }
                il = fill(includeLevel.in, array1);
            }
            if (il == -1)
            {
                if (includeStack != null)
                {
                    while (!includeStack.isEmpty() && il == -1)
                    {
                        close(includeLevel.in);
                        includeLevel = includeStack.pop();
                        return read();
                    }
                }
                return -1;
            }
            if (il == 0)
            {
                throw new IOException("No input! Use blocking mode?");
            }
            buffer1.clear();
            buffer2.clear();
            end+=il;
            if (end < 0)
            {
                throw new IOException("end = "+end);
            }
        }
        int rc = get(cursor++);
        if (cursor < 0)
        {
            throw new IOException("cursor = "+cursor);
        }
        includeLevel.forward(rc);
        length++;
        if (length > size)
        {
            throw new IOException("input size "+length+" exceeds buffer size "+size);
        }
        if (checksum != null)
        {
            checksum.update(cursor-1, rc);
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
    /**
     * @deprecated Will be removed
     * @param type
     * @param terminal
     * @return 
     */
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
        return Primitives.parseBoolean(this);
    }
    /**
     * Converts part of input
     * @param s Start position starting at 0
     * @param l Length
     * @return
     */
    @Override
    public boolean parseBoolean(long s, int l)
    {
        return Primitives.parseBoolean(getCharSequence(s, l));
    }
    /**
     * Returns the only character of string
     * @return
     */
    @Override
    public char parseChar()
    {
        return Primitives.parseChar(this);
    }
    /**
     * Converts part of input
     * @param s Start position starting at 0
     * @param l Length
     * @return
     */
    @Override
    public char parseChar(long s, int l)
    {
        return Primitives.parseChar(getCharSequence(s, l));
    }
    /**
     * Parses string content to byte "6" -&gt; 6
     * Minus is allowed as first character
     * @return
     */
    @Override
    public byte parseByte()
    {
        return Primitives.parseByte(this);
    }
    /**
     * Converts part of input
     * @param s Start position starting at 0
     * @param l Length
     * @return
     */
    @Override
    public byte parseByte(long s, int l)
    {
        return Primitives.parseByte(getCharSequence(s, l));
    }
    /**
     * Parses string content to short "123" -&gt; 123
     * Minus is allowed as first character
     * @return
     */
    @Override
    public short parseShort()
    {
        return Primitives.parseShort(this);
    }
    /**
     * Converts part of input
     * @param s Start position starting at 0
     * @param l Length
     * @return
     */
    @Override
    public short parseShort(long s, int l)
    {
        return Primitives.parseShort(getCharSequence(s, l));
    }
    /**
     * Parses string content to int "123" -&gt; 123
     * Minus is allowed as first character
     * @return
     */
    @Override
    public int parseInt()
    {
        return Primitives.parseInt(this);
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
        return Primitives.parseInt(this, 2);
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
        return Primitives.parseInt(this, -2);
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
        return Primitives.parseLong(this, 2);
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
        return Primitives.parseLong(this, -2);
    }
    /**
     * Converts part of input
     * @param s Start position starting at 0
     * @param l Length
     * @return
     */
    @Override
    public int parseInt(long s, int l)
    {
        return Primitives.parseInt(getCharSequence(s, l));
    }
    /**
     * Converts binary to int
     * @param s
     * @param l
     * @param radix
     * @return 
     */
    @Override
    public int parseInt(long s, int l, int radix)
    {
        return Primitives.parseInt(getCharSequence(s, l), radix);
    }
    @Override
    public long parseLong(long s, int l, int radix)
    {
        return Primitives.parseLong(getCharSequence(s, l), radix);
    }
    /**
     * Parses string content to long "123" -&gt; 123
     * Minus is allowed as first character
     * @return
     */
    @Override
    public long parseLong()
    {
        return Primitives.parseLong(this);
    }
    /**
     * Converts part of input
     * @param s Start position starting at 0
     * @param l Length
     * @return
     */
    @Override
    public long parseLong(long s, int l)
    {
        return Primitives.parseLong(getCharSequence(s, l));
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
        return Primitives.parseFloat(this);
    }
    /**
     * Converts part of input
     * @param s Start position starting at 0
     * @param l Length
     * @return
     */
    @Override
    public float parseFloat(long s, int l)
    {
        return Primitives.parseFloat(getCharSequence(s, l));
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
        return Primitives.parseDouble(this);
    }
    /**
     * Converts part of input
     * @param s Start position starting at 0
     * @param l Length
     * @return
     */
    @Override
    public double parseDouble(long s, int l)
    {
        return Primitives.parseDouble(getCharSequence(s, l));
    }

    @Override
    public boolean isAtBoundary(int t) throws IOException
    {
        CharRange.BoundaryType type = CharRange.BoundaryType.values()[t];
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
        return includeLevel.line;
    }

    @Override
    public int getColumnNumber()
    {
        return includeLevel.column;
    }

    @Override
    public String getEncoding()
    {
        if (includeLevel.in instanceof ByteChannelReadable)
        {
            ByteChannelReadable sr = (ByteChannelReadable) includeLevel.in;
            return sr.getCharset().name();
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
    /**
     * @param checksum 
     * @param lookaheadLength 
     */
    @Override
    public void setChecksum(Checksum checksum, int lookaheadLength)
    {
        this.checksum = new ChecksumWrapper(this, checksum, lookaheadLength);
    }

    @Override
    public Checksum getChecksum()
    {
        return checksum;
    }
    
    protected class IncludeLevel
    {
        protected I in;
        protected int line = 1;
        protected int column;
        protected String source = "";
        protected int lastChar;

        protected IncludeLevel()
        {
        }

        protected IncludeLevel(I in, String source)
        {
            this.in = in;
            this.source = source;
        }

        protected void reset()
        {
            in = null;
            line = 1;
            column = 0;
            source = "";
        }
        
        protected boolean startOfLine()
        {
            return column == 0;
        }

        protected void forward(int rc)
        {
            lastChar = rc;
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

    }
    public class CharSequenceImpl implements CharSequence
    {
        private final long s;
        private final int l;
        /**
         * Creates a CharSequence using InputReader array as backing store.
         * @param s Start
         * @param l Length
         */
        public CharSequenceImpl(long s, int l)
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
