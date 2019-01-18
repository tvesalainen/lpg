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

import java.util.zip.Checksum;

/**
 * ChecksumWrapper delays checksum calculation upto lookahead characters.
 * If parser has lookahead states or is using input insert, the character stream
 * might go back and continue with another characters.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
final class ChecksumWrapper implements Checksum
{
    private InputReader input;
    private Checksum checksum;
    private int[] lookahead;
    private int size;
    private long lo;
    private long hi;
    /**
     * Creates new ChecksumWrapper
     * @param input
     * @param checksum
     * @param lookaheadLength Lookahead length. Can be 0.
     */
    ChecksumWrapper(InputReader input, Checksum checksum, int lookaheadLength)
    {
        this.input = input;
        this.checksum = checksum;
        this.size = lookaheadLength;
        if (lookaheadLength > 0)
        {
            this.lookahead = new int[size];
        }
    }
    /**
     * Update with character index.
     * @param index
     * @param b 
     */
    void update(long index, int b)
    {
        if (size > 0)
        {
            assert index <= hi;
            if (index < lo)
            {
                throw new IllegalStateException("lookaheadLength() too small in ChecksumProvider implementation");
            }
            hi = index;
            if ((lo == hi - size))
            {
                checksum.update(lookahead[(int)(lo % size)]);
                lo++;
            }
            lookahead[(int)(hi++ % size)] = b;
        }
        else
        {
            if (index == hi)
            {
                checksum.update(b);
                hi = index+1;
            }
            else
            {
                if (index != hi -1)
                {
                    throw new IllegalStateException("lookahead needed for checksum");
                }
            }
        }
    }
    /**
     * Throws UnsupportedOperationException
     * @param b 
     */
    @Override
    public void update(int b)
    {
        throw new UnsupportedOperationException("Not supported.");
    }
    /**
     * Throws UnsupportedOperationException
     * @param b
     * @param off
     * @param len 
     */
    @Override
    public void update(byte[] b, int off, int len)
    {
        throw new UnsupportedOperationException("Not supported.");
    }
    /**
     * Returns checksum value after synchronizing lookahead
     * @return 
     */
    @Override
    public long getValue()
    {
        sync();
        return checksum.getValue();
    }
    /**
     * Resets checksum after synchronizing lookahead
     */
    @Override
    public void reset()
    {
        sync();
        checksum.reset();
    }
    
    private void sync()
    {
        long end = input.getEnd();
        if (end < lo || end >= hi)
        {
            throw new IllegalStateException("lookaheadLength() too small in ChecksumProvider implementation");
        }
        for (;lo<end;lo++)
        {
            checksum.update(lookahead[(int)(lo % size)]);
        }
    }
}
