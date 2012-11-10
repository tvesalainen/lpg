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
package org.vesalainen.parser;

import org.vesalainen.bcc.MethodCompiler;
import org.vesalainen.parser.util.InputReader;
import java.io.IOException;

/**
 * Compiles a method which only checks end of input condition
 * @author tkv
 */
public class EofCompiler
{
    private MethodCompiler c;

    public EofCompiler(MethodCompiler c)
    {
        this.c = c;
    }

    public void compile() throws IOException, NoSuchMethodException
    {
        c.nameArgument("reader", 1);
        c.tload("reader");
        c.invokevirtual(InputReader.class.getMethod("read"));
        c.iflt("eof");
        c.iconst(-1);
        c.treturn();
        c.fixAddress("eof");
        c.iconst(0);
        c.treturn();
        c.end();
    }

}
