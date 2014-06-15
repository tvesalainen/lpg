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

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * RecoverableInputStream is a PushbackInputStream which checks it's underlying
 InputStream if it implements Recoverable interface, when recover method is called. 
 * If it does calls it's recover method.
 * 
 * @author Timo Vesalainen
 */
public class RecoverableInputStream extends FilterInputStream implements Recoverable
{

    public RecoverableInputStream(InputStream in)
    {
        super(in);
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
    
}
