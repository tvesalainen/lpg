/*
 * Copyright (C) 2016 tkv
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
 * @author tkv
 */
public class RegexReplacerTest
{
    
    public RegexReplacerTest()
    {
    }

    @Test
    public void test1()
    {
        RegexReplacer rr = new RegexReplacer("[0-9]+", (sb,c,s,e)->
        {
            sb.append("["+c.subSequence(s, e)+"]");
        });
        assertEquals("asdf[1234]sert", rr.replace("asdf1234sert"));
    }
    
    @Test
    public void test2()
    {
        RegexReplacer rr = new RegexReplacer("1234", (sb,c,s,e)->
        {
            sb.append("["+c.subSequence(s, e)+"]");
        });
        assertEquals("asdf[1234]sert", rr.replace("asdf1234sert"));
    }
    
    @Test
    public void test3()
    {
        RegexReplacer rr = new RegexReplacer("124", (sb,c,s,e)->
        {
            sb.append("["+c.subSequence(s, e)+"]");
        });
        assertEquals("asdf1234sert", rr.replace("asdf1234sert"));
    }
    
    @Test
    public void test4()
    {
        RegexReplacer rr = new RegexReplacer("[0-9]+", (sb,c,s,e)->
        {
            sb.append("["+c.subSequence(s, e)+"]");
        });
        assertEquals("asdf[1234]", rr.replace("asdf1234"));
    }
    
    @Test
    public void test5()
    {
        RegexReplacer rr = new RegexReplacer();
        rr.addExpression("[1-2]+", (sb,c,s,e)->
        {
            sb.append("["+c.subSequence(s, e)+"]");
        });
        rr.addExpression("sdf", (sb,c,s,e)->
        {
            sb.append("{"+c.subSequence(s, e)+"}");
        });
        assertEquals("a{sdf}[12]34", rr.replace("asdf1234"));
    }
    
    @Test
    public void test6()
    {
        RegexReplacer rr = new RegexReplacer();
        rr.addExpression("%[1-2]", (sb,c,s,e)->
        {
            sb.append("*"+c.subSequence(s+1, e)+"*");
        });
        rr.addExpression("%[1-2]%[3-4]", (sb,c,s,e)->
        {
            sb.append("#"+c.subSequence(s+1, e)+"#");
        });
        assertEquals("q*1*#1%4#b", rr.replace("q%1%1%4b"));
    }
    
}
