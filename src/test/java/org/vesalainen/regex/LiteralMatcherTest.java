/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LiteralMatcherTest
{
    
    public LiteralMatcherTest()
    {
    }

    @Test
    public void test1()
    {
        LiteralMatcher<Integer> lm = new LiteralMatcher<>();
        lm.addExpression("foo", 1);
        lm.addExpression("bar", 2);
        lm.addExpression("goo?", 3);
        lm.addExpression("*A*", 4);
        lm.compile();
        assertEquals(1, (long)lm.match("foo"));
        assertEquals(2, (long)lm.match("bar"));
        assertEquals(3, (long)lm.match("goo?"));
        assertEquals(4, (long)lm.match("*A*"));
        assertNull(lm.match("***"));
    }
    
}
