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
package org.vesalainen.grammar.state;

/**
 * AmbiguousExpressionException is thrown when ParserCompiler detects ambiguous
 * regular expression combination.  Given expression must be changed to be unambiguous.
 * Example:
 * <code>
    @Rules({
    @Rule({"'\\&#'", "digit"}),
    @Rule({"'\\&#x'", "hex"})
    })
    protected int character(int cc)
    {
 * </code>
 * Corrected:
 * <code>
    @Rules({
    @Rule({"'\\&#'", "digit"}),
    @Rule({"'\\&#'", "'x'", "hex"})
    })
    protected int character(int cc)
    {
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AmbiguousExpressionException extends RuntimeException
{
    private Object reducer1;
    private Object reducer2;
    public AmbiguousExpressionException(String msg, Object reducer1, Object reducer2)
    {
        super(msg);
        this.reducer1 = reducer1;
        this.reducer2 = reducer2;
    }

    public Object getToken1()
    {
        return reducer1;
    }

    public Object getToken2()
    {
        return reducer2;
    }
    
}
