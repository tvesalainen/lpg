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
import java.io.PushbackInputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;

/**
 * @version $Id$
 * @author Timo Vesalainen
 */
public final class StreamReader extends Reader
{
    private Decoder decoder;
    private PushbackInputStream in;
    private Charset charset;

    public StreamReader(InputStream in)
    {
        this(in, Charset.defaultCharset());
    }
    public StreamReader(InputStream in, String cs)
    {
        this(in, Charset.forName(cs));
    }
    public StreamReader(InputStream in, Charset cs)
    {
        this.in = new PushbackInputStream(in, 4);
        setCharset(cs);
    }

    public void setCharset(Charset cs)
    {
        if (charset == null || !charset.contains(cs))
        {
            charset = cs;
            if (StandardCharsets.US_ASCII.contains(cs))
            {
                decoder = new US_ASCIIDecoder();
            }
            else
            {
                if (StandardCharsets.ISO_8859_1.contains(cs))
                {
                    decoder = new ISO_8859_1Decoder();
                }
                else
                {
                    if (StandardCharsets.UTF_8.contains(cs))
                    {
                        decoder = new UTF8Decoder();
                    }
                    else
                    {
                        decoder = new DefaultDecoder(cs);
                    }
                }
            }
        }
    }
    @Override
    public int read() throws IOException
    {
        return decoder.decode(in);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws IOException
    {
        in.close();
    }

    Charset getCharset()
    {
        return charset;
    }

    private abstract class Decoder
    {
        public abstract int decode(PushbackInputStream in) throws IOException;
    }
    private class ISO_8859_1Decoder extends Decoder
    {
        private int pushback;
        @Override
        public int decode(PushbackInputStream in) throws IOException
        {
            pushback = in.read();
            return pushback;
        }

    }
    private class US_ASCIIDecoder extends ISO_8859_1Decoder
    {

        @Override
        public int decode(PushbackInputStream in) throws IOException
        {
            int cc = super.decode(in);
            if (cc != -1 && cc < 0)
            {
                throw new IOException("input not us-ascii");
            }
            return cc;
        }
        
    }
    private class UTF8Decoder extends Decoder
    {
        private byte[] b = new byte[4];
        private int pushbackLength;
        private int lowSurrogate;
        private boolean hasLowSurrogate;
        
        @Override
        public int decode(PushbackInputStream in) throws IOException
        {
            if (hasLowSurrogate)
            {
                hasLowSurrogate = false;
                return lowSurrogate;
            }
            int b1 = in.read();
            if (b1 == -1)
            {
                return -1;
            }
            b[0] = (byte) b1;
            if ((b1 >> 7) == 0)
            {
                pushbackLength = 1;
                return b1;
            }
            else
            {
                if ((b1 >> 5) == 0b110)
                {
                    int b2 = in.read();
                    if (b2 == -1)
                    {
                        throw new IOException("unexpected eof");
                    }
                    if ((b2>>6) != 0b10)
                    {
                        throw new IOException("malformed input");
                    }
                    b[1] = (byte) b2;
                    pushbackLength = 2;
                    return (char) (((b1 << 6) ^ b2) ^ (((byte) 0xC0 << 6) ^ ((byte) 0x80)));
                }
                else
                {
                    if ((b1 >> 4) == 0b1110)
                    {
                        int b2 = in.read();
                        if (b2 == -1)
                        {
                            throw new IOException("unexpected eof");
                        }
                        if ((b2>>6) != 0b10)
                        {
                            throw new IOException("malformed input");
                        }
                        int b3 = in.read();
                        if (b3 == -1)
                        {
                            throw new IOException("unexpected eof");
                        }
                        if ((b3>>6) != 0b10)
                        {
                            throw new IOException("malformed input");
                        }
                        b[1] = (byte) b2;
                        b[2] = (byte) b3;
                        pushbackLength = 3;
                        return (char) ((b1 << 12)
                                ^ (b2 << 6)
                                ^ (b3
                                ^ (((byte) 0xE0 << 12)
                                ^ ((byte) 0x80 << 6)
                                ^ ((byte) 0x80))));
                    }
                    else
                    {
                        if ((b1 >> 3) == 0b11110)
                        {
                            int b2 = in.read();
                            if (b2 == -1)
                            {
                                throw new IOException("unexpected eof");
                            }
                            if ((b2>>6) != 0b10)
                            {
                                throw new IOException("malformed input");
                            }
                            int b3 = in.read();
                            if (b3 == -1)
                            {
                                throw new IOException("unexpected eof");
                            }
                            if ((b3>>6) != 0b10)
                            {
                                throw new IOException("malformed input");
                            }
                            int b4 = in.read();
                            if (b4 == -1)
                            {
                                throw new IOException("unexpected eof");
                            }
                            if ((b4>>6) != 0b10)
                            {
                                throw new IOException("malformed input");
                            }
                            int uc = ((b1 << 18)
                                    ^ (b2 << 12)
                                    ^ (b3 << 6)
                                    ^ (b4
                                    ^ (((byte) 0xF0 << 18)
                                    ^ ((byte) 0x80 << 12)
                                    ^ ((byte) 0x80 << 6)
                                    ^ ((byte) 0x80))));
                            if (!Character.isSupplementaryCodePoint(uc))
                            {
                                throw new IOException("malformed input");
                            }
                            hasLowSurrogate = true;
                            lowSurrogate = Character.lowSurrogate(uc);
                            b[1] = (byte) b2;
                            b[2] = (byte) b3;
                            b[3] = (byte) b4;
                            pushbackLength = 4;
                            return Character.highSurrogate(uc);
                        }
                        else
                        {
                            throw new IOException("malformed input");
                        }
                    }
                }
            }
        }

    }
    private class DefaultDecoder extends Decoder
    {
        private static final int BUFFERSIZE = 8192;
        private CharsetDecoder decoder;
        private byte[] buffer;
        private ByteBuffer byteBuffer;
        private CharBuffer charBuffer;
        
        public DefaultDecoder(Charset cs)
        {
            decoder = cs.newDecoder();
            buffer = new byte[BUFFERSIZE];
            byteBuffer = ByteBuffer.wrap(buffer);
            byteBuffer.flip();
            charBuffer = CharBuffer.allocate(1);
            charBuffer.flip();
        }
        public int decode(PushbackInputStream in) throws IOException
        {
            if (charBuffer.hasRemaining())
            {
                return charBuffer.get();
            }
            if (!byteBuffer.hasRemaining())
            {
                int rc = in.read(buffer);
                if (rc == -1)
                {
                    return -1;
                }
                byteBuffer.position(0);
                byteBuffer.limit(rc);
            }
            charBuffer.clear();
            CoderResult res = decoder.decode(byteBuffer, charBuffer, false);
            while (res.isUnderflow())
            {
                assert !byteBuffer.hasRemaining();
                int rc = in.read(buffer);
                byteBuffer.position(0);
                if (rc != -1)
                {
                    byteBuffer.limit(rc);
                    res = decoder.decode(byteBuffer, charBuffer, false);
                }
                else
                {
                    byteBuffer.limit(0);
                    decoder.decode(byteBuffer, charBuffer, true);
                    decoder.flush(charBuffer);
                    break;
                }
            }
            if (res.isError())
            {
                throw new IOException(res.toString());
            }
            charBuffer.flip();
            if (!charBuffer.hasRemaining())
            {
                return -1;
            }
            return charBuffer.get();
        }

    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            // TODO code application logic here
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
