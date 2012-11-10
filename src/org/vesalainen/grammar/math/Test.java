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

package org.vesalainen.grammar.math;

import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.MathExpression;

/**
 * @author Timo Vesalainen
 */
@GenClassname("org.vesalainen.grammar.math.TestImpl")
public abstract class Test
{
    enum E {A, B, C };
    
    @MathExpression("3*-i[A]^2")
    public double func(double [] i)
    {
        throw new UnsupportedOperationException("not supported");
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            MathCompiler mc = new MathCompiler(Test.class);
            mc.compile();
            Test t = (Test) mc.newInstance();
            System.err.println(t.func(new double [] {1, 1}));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
