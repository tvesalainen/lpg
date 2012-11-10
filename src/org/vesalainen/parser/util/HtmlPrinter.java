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

package org.vesalainen.parser.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Locale;
import org.vesalainen.bcc.type.Generics;

/**
 * @author Timo Vesalainen
 */
public class HtmlPrinter extends AppendablePrinter implements AutoCloseable
{
    private int level;
    public HtmlPrinter(File srcDir, Type thisClass, String filename) throws IOException
    {
        super(createWriter(srcDir, thisClass, filename));
        level = getPackageDepth(thisClass);
        super.println("<html>");
        super.println("<body>");
    }

    private static FileWriter createWriter(File srcDir, Type thisClass, String filename) throws IOException
    {
        File file = Generics.getFileForClass(thisClass, srcDir, "");
        File docFilesDir = new File(file.getParentFile(), "doc-files");
        if (!docFilesDir.exists())
        {
            docFilesDir.mkdirs();
        }
        File out = new File(docFilesDir, filename);
        return new FileWriter(out);
    }
    private int getPackageDepth(Type thisClass)
    {
        int depth = 1;
        String f = Generics.getInternalForm(thisClass);
        int indexOf = f.indexOf('/');
        while (indexOf != -1)
        {
            depth++;
            indexOf = f.indexOf('/', indexOf+1);
        }
        return depth;
    }

    public int getLevel()
    {
        return level;
    }
    
    public HtmlPrinter(Appendable out)
    {
        super(out);
    }
    public void span(String clazz, String text) throws IOException
    {
        super.println("<span class=\""+clazz+"\">"+escape(text)+"</span>");
    }
    public void div(String clazz, String text) throws IOException
    {
        super.println("<div class=\""+clazz+"\">"+escape(text)+"</div>");
    }
    public void linkDestination(String name) throws IOException
    {
        super.println("<a name=\""+name+"\"/>");
    }
    public void linkSource(String name, String text) throws IOException
    {
        super.print("<a href=\""+name+"\">"+escape(text)+"</a>");
    }
    public void p() throws IOException
    {
        super.println("<p>");
    }
    public void h1(String title) throws IOException
    {
        h(title, 1);
    }
    public void h2(String title) throws IOException
    {
        h(title, 2);
    }
    public void h3(String title) throws IOException
    {
        h(title, 3);
    }
    public void h4(String title) throws IOException
    {
        h(title, 4);
    }
    public void h(String title, int level) throws IOException
    {
        super.println("<h"+level+">"+escape(title)+"</h"+level+">");
    }

    @Override
    public void print(char c) throws IOException
    {
        super.print(escape(c));
    }

    @Override
    public void print(String s) throws IOException
    {
        super.print(escape(s));
    }

    @Override
    public void println(char c) throws IOException
    {
        super.println(escape(c));
    }

    @Override
    public void println(String s) throws IOException
    {
        super.println(escape(s));
    }

    @Override
    public void close()// throws Exception
    {
        try
        {
            super.println("</body>");
            super.println("</html>");
            if (out instanceof AutoCloseable)
            {
                AutoCloseable ac = (AutoCloseable) out;
                ac.close();
            }
        }
        catch (Exception ex)
        {
        }
    }

    private String escape(String text)
    {
        return text.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");
    }

    private String escape(char c)
    {
        switch (c)
        {
            case '&':
                return "&amp;";
            case '<':
                return "&lt;";
            case '>':
                return "&gt;";
            default:
                return String.valueOf(c);
        }
    }

}
