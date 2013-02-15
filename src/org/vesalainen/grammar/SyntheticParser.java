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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.vesalainen.parser.ParserCompiler;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;

/**
 * SyntheticParser parses synthetic grammar rules
 * @author Timo Vesalainen
 * @see <a href="doc-files/SyntheticParser-expression.html#BNF">BNF Syntax for synthetic expression</a>
 */
@GenClassname("org.vesalainen.grammar.SyntheticParserImpl")
@GrammarDef()
public abstract class SyntheticParser implements GrammarConstants
{
    public static SyntheticParser newInstance()
    {
        SyntheticParser parser = (SyntheticParser) GenClassFactory.getGenInstance(SyntheticParser.class);
        return parser;
    }
    public Type parse(String text, Grammar g)
    {
        try
        {
            return parseIt(text, g);
        }
        catch (Throwable t)
        {
            throw new IllegalArgumentException("Problem with "+text, t);
        }
    }
    /**
     * 
     * @param text
     * @param g
     * @return 
     * @see <a href="doc-files/SyntheticParser-expression.html#BNF">BNF Syntax for synthetic expression</a>
     */
    @ParseMethod(start="expression")
    protected abstract Type parseIt(String text, @ParserContext("GRAMMAR") Grammar g);
    
    @Rule(left="expression", value="symbol")
    protected Type plainSymbol(String symbol, @ParserContext("GRAMMAR") Grammar g)
    {
        return g.getTypeForNonterminal(symbol);
    }
    
    @Rule(left="expression", value={"expression","'"+CIRCLED_ASTERISK+"'"})
    protected Type plainStar(Type type)
    {
        return type;
    }
    
    @Rule(left="expression", value={"expression", "'"+CIRCLED_PLUS+"'"})
    protected Type plainPlus(Type type)
    {
        return type;
    }
    
    @Rule(left="expression", value={"expression", "'"+INVERTED_QUESTION_MARK+"'"})
    protected Type plainOpt(Type type)
    {
        return type;
    }
    
    @Rule(left="expression", value={"'"+SIGMA+"\\{'", "choiseList", "'\\}'"})
    protected Type plainChoise(List<Type> typeList)
    {
        Type type = typeList.get(0);
        for (Type t : typeList)
        {
            if (!t.equals(type))
            {
                throw new IllegalArgumentException("all choise types not the same");
            }
        }
        return type;
    }
    
    @Rule({"expression"})
    protected List<Type> choiseList(Type type)
    {
        List<Type> list = new ArrayList<>();
        list.add(type);
        return list;
    }
    @Rule({"choiseList", "pipe", "expression"})
    protected List<Type> choiseList(List<Type> typeList, Type type)
    {
        typeList.add(type);
        return typeList;
    }
    @Rule(left="expression", value={"'"+PHI+"\\{'", "seqList", "'\\}'"})
    protected Type plainSeq(List<Type> typeList)
    {
        Type type = void.class;
        for (Type t : typeList)
        {
            if (!t.equals(void.class))
            {
                if (!void.class.equals(type))
                {
                    throw new IllegalArgumentException("all one seq type != void allowed");
                }
                type = t;
            }
        }
        return type;
    }
    
    @Rule({"expression"})
    protected List<Type> seqList(Type type)
    {
        List<Type> list = new ArrayList<>();
        list.add(type);
        return list;
    }
    @Rule({"seqList", "comma", "expression"})
    protected List<Type> seqList(List<Type> typeList, Type type)
    {
        typeList.add(type);
        return typeList;
    }
    @Terminal(expression="'[^']+'|`[^´]+´")
    protected String anonymousTerminal(String name, @ParserContext("GRAMMAR") Grammar g)
    {
        name = name.substring(1, name.length()-1);
        g.addAnonymousTerminal(name);
        return "'"+name+"'";
    }
    @Terminal(expression="[\\x21-\\x26\\x2d-\\x3e\\x40-\\x5f\\x61-\\x7b\\x7e-\\x7f\\xC0-\\xD6\\xD8-\\xF6]+")
    protected String symbolName(String name)
    {
        return name;
    }
    
    @Terminal(expression="\\|")
    protected abstract void pipe();
    
    @Terminal(expression="\\,")
    protected abstract void comma();
    
    @Rules({
    @Rule("symbolName"),
    @Rule("anonymousTerminal")
    })
    protected String symbol(String name)
    {
        return name;
    }
    
    public static void main(String[] args)    
    {
        try
        {
            SyntheticParser.newInstance();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
