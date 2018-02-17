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

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PrefixMapTest
{
    
    public PrefixMapTest()
    {
    }

    @Test
    public void test1()
    {
        Map<String, Integer> m = new HashMap<>();
        m.put("foo", 1);
        m.put("bar", 2);
        PrefixMap<Integer> pm = new PrefixMap<>(m);
        assertNull(pm.get(""));
        assertEquals(Integer.valueOf(1), pm.get("f"));
        assertEquals(Integer.valueOf(1), pm.get("fo"));
        assertEquals(Integer.valueOf(1), pm.get("foo"));
        assertEquals(Integer.valueOf(1), pm.get("fooo"));
        assertEquals(Integer.valueOf(2), pm.get("b"));
        assertEquals(Integer.valueOf(2), pm.get("ba"));
        assertEquals(Integer.valueOf(2), pm.get("bar"));
        assertEquals(Integer.valueOf(2), pm.get("bar1"));
    }
    
}
