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
import java.io.Reader;

/**
 * This Reader converts input to upper case
 * @author tkv
 */
public class CaseChangeReader extends Reader
{
    private Reader in;
    private boolean upper;

    public CaseChangeReader(Reader in, boolean upper)
    {
        this.in = in;
        this.upper = upper;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException
    {
        int rc = in.read(cbuf, off, len);
        if (rc == -1)
        {
            return -1;
        }
        if (upper)
        {
            for (int ii=0;ii<rc;ii++)
            {
                cbuf[off+ii] = Character.toUpperCase(cbuf[off+ii]);
            }
        }
        else
        {
            for (int ii=0;ii<rc;ii++)
            {
                cbuf[off+ii] = Character.toLowerCase(cbuf[off+ii]);
            }
        }
        return rc;
    }

    @Override
    public void close() throws IOException
    {
        in.close();
    }
}
