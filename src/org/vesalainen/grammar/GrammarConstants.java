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
package org.vesalainen.grammar;

/**
 *
 * @author Timo Vesalainen
 */
public interface GrammarConstants
{
    /**
     * Precedes pseudo choice nonterminal generated for (A|B|C) expression
     */
    public static final char SIGMA = 0x3a3;
    /**
     * Precedes pseudo sequence nonterminal generated for (A B C) expression
     */
    public static final char PHI = 0x3d5;
    public static final char CIRCLED_PLUS = 0x2295;
    public static final char CIRCLED_ASTERISK = 0x229b;
    public static final char INVERTED_QUESTION_MARK = 0xbf;
    
}
