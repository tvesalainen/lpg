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

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

/**
 *
 * @author Timo Vesalainen
 */
public class CaseChangePushbackByteChannelReadable extends PushbackByteChannelReadable
{
    private boolean upper;
    
    public CaseChangePushbackByteChannelReadable(ReadableByteChannel channel, Charset cs, int sz, boolean direct, boolean fixedCharset, boolean upper)
    {
        super(channel, cs, sz, direct, fixedCharset);
        this.upper = upper;
    }

    @Override
    public int read(CharBuffer cb) throws IOException
    {
        // TODO8 use lambda
        int rc = super.read(cb);
        if (rc > 0)
        {
            int pos = cb.position();
            if (upper)
            {
                for (int ii=0;ii<rc;ii++)
                {
                    int index = pos-ii;
                    cb.put(index, Character.toUpperCase(cb.get(index)));
                }
            }
            else
            {
                for (int ii=0;ii<rc;ii++)
                {
                    int index = pos-ii;
                    cb.put(index, Character.toLowerCase(cb.get(index)));
                }
            }
        }
        return rc;
    }
    
}
