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
 * 
 * @author Timo Vesalainen
 */
public final class StreamReader extends Reader implements Recoverable
{
    private Decoder decoder;
    private InputStream in;
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
        this.in = new RecoverableInputStream(in);
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
                    decoder = new DefaultDecoder(cs);
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
        if (len == 0)
        {
            return 0;
        }
        int rc = decoder.decode(in);
        if (rc == -1)
        {
            return -1;
        }
        else
        {
            cbuf[off] = (char) rc;
        }
        return 1;
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

    /**
     * Checks if underlying stream implements Recoverable interface. 
     * If it does it's recover method is called.
     * @return 
     */
    @Override
    public boolean recover()
    {
        if (in instanceof Recoverable)
        {
            Recoverable recoverable = (Recoverable) in;
            return recoverable.recover();
        }
        return false;
    }
    
    private abstract class Decoder
    {
        public abstract int decode(InputStream in) throws IOException;
    }
    private class ISO_8859_1Decoder extends Decoder
    {
        private int pushback;
        @Override
        public int decode(InputStream in) throws IOException
        {
            pushback = in.read();
            return pushback;
        }

    }
    private class US_ASCIIDecoder extends ISO_8859_1Decoder
    {

        @Override
        public int decode(InputStream in) throws IOException
        {
            int cc = super.decode(in);
            if (cc != -1 && cc < 0)
            {
                throw new IOException("input not us-ascii");
            }
            return cc;
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
        public int decode(InputStream in) throws IOException
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
}
