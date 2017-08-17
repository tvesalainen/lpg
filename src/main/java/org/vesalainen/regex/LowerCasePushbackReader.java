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

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LowerCasePushbackReader extends PushbackReader
{
    public LowerCasePushbackReader(Reader in)
    {
        super(in);
    }

    public LowerCasePushbackReader(Reader in, int size)
    {
        super(in, size);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException
    {
        int rc = super.read(cbuf, off, len);
        if (rc == -1)
        {
            return -1;
        }
        for (int ii=0;ii<rc;ii++)
        {
            cbuf[off+ii] = Character.toLowerCase(cbuf[off+ii]);
        }
        return rc;
    }

}
