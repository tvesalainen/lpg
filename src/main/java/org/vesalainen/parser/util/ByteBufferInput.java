/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import org.vesalainen.parser.ParserFeature;
import static org.vesalainen.parser.ParserFeature.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ByteBufferInput extends CharInput<ByteBuffer>
{
    private final CharsetDecoder decoder;
    private IntUnaryOperator op;

    public ByteBufferInput(ByteBuffer input, int size, Charset cs, Set<ParserFeature> features)
    {
        super(size, features);
        includeLevel.in = input;
        this.decoder = cs.newDecoder();
        if (features.contains(UpperCase))
        {
            op = Character::toUpperCase;
        }
        else
        {
            if (features.contains(LowerCase))
            {
                op = Character::toLowerCase;
            }
        }
    }

    @Override
    protected int fill(ByteBuffer input, CharBuffer[] array) throws IOException
    {
        if (input.hasRemaining())
        {
            int count = 0;
            for (CharBuffer cb : array)
            {
                int remaining = cb.remaining();
                int position = cb.position();
                CoderResult res = decoder.decode(input, cb, false);
                if (res.isUnderflow())
                {
                    res = decoder.decode(input, cb, true);
                }
                if (res.isError())
                {
                    res.throwException();
                }
                int len = remaining - cb.remaining();
                count += len;
                if (op != null)
                {
                    for (int ii=position;ii<len;ii++)
                    {
                        cb.put(ii, (char) op.applyAsInt(cb.get(ii)));
                    }
                }
            }
            return count;
        }
        else
        {
            return -1;
        }
    }

    @Override
    protected void unread(ByteBuffer input) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void close(ByteBuffer input) throws IOException
    {
    }

    @Override
    public void include(InputStream is, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void include(InputStream is, String cs, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void include(InputStream is, Charset cs, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void include(Readable in, String source) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
