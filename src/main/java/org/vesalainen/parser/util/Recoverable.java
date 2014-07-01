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

import org.vesalainen.io.Rewindable;

/**
 * Recoverable is an interface used by the parser. In case of syntax error or
 * other problem while parsing, the parser check the existence of method 
 * annotated with @RecoverMethod. If the method exist it is called. 
 * 
 * <p>If @RecoverMethod
 * doesn't exist, InputReader searches for InputStream or Reader class implementing
 * Recoverable interface. If found it calls it's recover method. If the method return's
 * true the parser continues at the start of the grammar. If it return's false it throws
 * exception.
 * 
 * <p>Note that parser often encloses input which practically hides the Recoverable input. If
 * that happens, a warning is printed to stderr during compilation.
 * 
 * <p>In most cases using @RecoverMethod is easier. 
 * 
 * @author Timo Vesalainen
 */
public interface Recoverable
{
    /**
     * If returns true, the parser is able to continue parsing in grammar start.
     * @return 
     */
   boolean recover(); 
}
