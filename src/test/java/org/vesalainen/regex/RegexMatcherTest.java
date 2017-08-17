/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.regex.Regex.Option;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RegexMatcherTest
{
    
    public RegexMatcherTest()
    {
    }

    @Test
    public void testMatch1()
    {
        RegexMatcher rm = new WildcardMatcher("http://www.domain.com/*.jpg*", 1);
        rm.compile();
        assertEquals(1, rm.match("http://www.domain.com/picture.jpg?foo=bar"));
        assertNull(rm.match("http://www.domain.com/picture.gif"));
    }
    @Test
    public void testMatch2()
    {
        RegexMatcher rm = new WildcardMatcher("*://www.domain.com/*.jpg*", 1);
        rm.compile();
        assertEquals(1, rm.match("http://www.domain.com/picture.jpg?foo=bar"));
        assertNull(rm.match("http://www.domain.com/picture.gif"));
    }
    @Test
    public void testMatch3()
    {
        RegexMatcher rm = new WildcardMatcher("http://passageweather.com/*.png", 1);
        rm.compile();
        assertEquals(1, rm.match("http://passageweather.com/maps/windward/press/003.png"));
    }
    @Test
    public void testMatchEmpty()
    {
        RegexMatcher rm = new WildcardMatcher();
        rm.compile();
        assertNull(rm.match("http://www.domain.com/picture.gif"));
    }
    @Test
    public void testSplit()
    {
        String s1 = ",   audio/*; q=0.2, audio/basic,  ";
        String regex = "\\,[ ]*";
        List<CharSequence> list = RegexMatcher.split(s1, regex).collect(Collectors.toList());
        assertEquals(2, list.size());
        assertTrue("audio/*; q=0.2".contentEquals(list.get(0)));
        assertTrue("audio/basic".contentEquals(list.get(1)));
    }
    
}
