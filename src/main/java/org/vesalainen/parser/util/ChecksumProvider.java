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

import java.util.zip.Checksum;

/**
 * Parser class having feature UseChecksum should implement this interface.
 * @author Timo Vesalainen
 */
public interface ChecksumProvider
{
    /**
     * Returns number of look-ahead characters. Default returns 0.
     * @return 
     */
    default int lookaheadLength()
    {
        return 0;
    }
    /**
     * This method should create a new Checksum instance.
     * Access to it, is through InputReader getChecksum method
     * @return 
     */
    Checksum createChecksum();
    /**
     * @deprecated Use InputReader getChecksum method
     * @return 
     * @throws UnsupportedOperationException
     */
    default Checksum getChecksum()
    {
        throw new UnsupportedOperationException("not supported!");
    }
}
