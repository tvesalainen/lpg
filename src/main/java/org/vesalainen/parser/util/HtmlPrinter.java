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

import org.vesalainen.io.AppendablePrinter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.vesalainen.bcc.model.El;

/**
 * @author Timo Vesalainen
 */
public class HtmlPrinter extends AppendablePrinter implements AutoCloseable
{
    private int level;
    private ProcessingEnvironment env;
    public HtmlPrinter(ProcessingEnvironment env, TypeElement thisClass, String filename) throws IOException
    {
        super(createWriter(env, thisClass, filename));
        level = getPackageDepth(thisClass);
        this.env = env;
        super.println("<html>");
        super.println("<body>");
    }

    private static Appendable createWriter(ProcessingEnvironment env, TypeElement thisClass, String filename) throws IOException
    {
        Filer filer = env.getFiler();
        FileObject resource = filer.createResource(StandardLocation.SOURCE_OUTPUT, El.getPackageOf(thisClass).getQualifiedName(), "doc-files/"+filename);
        return new PrintWriter(resource.openWriter());
    }
    
    private int getPackageDepth(TypeElement thisClass)
    {
        int depth = 1;
        String f = thisClass.getQualifiedName().toString();
        int indexOf = f.indexOf('.');
        while (indexOf != -1)
        {
            depth++;
            indexOf = f.indexOf('.', indexOf+1);
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

    public void b(String text) throws IOException
    {
        format("b", text);
    }
    public void i(String text) throws IOException
    {
        format("i", text);
    }
    public void strong(String text) throws IOException
    {
        format("strong", text);
    }
    public void small(String text) throws IOException
    {
        format("small", text);
    }
    public void em(String text) throws IOException
    {
        format("em", text);
    }
    public void sup(String text) throws IOException
    {
        format("sup", text);
    }
    public void sub(String text) throws IOException
    {
        format("sub", text);
    }
    public void pre(String text) throws IOException
    {
        format("pre", text);
    }
    public void code(String text) throws IOException
    {
        format("code", text);
    }
    public void kbd(String text) throws IOException
    {
        format("kbd", text);
    }
    public void samp(String text) throws IOException
    {
        format("samp", text);
    }
    public void var(String text) throws IOException
    {
        format("var", text);
    }
    public void address(String text) throws IOException
    {
        format("address", text);
    }
    public void abbr(String text) throws IOException
    {
        format("abbr", text);
    }
    public void dfn(String text) throws IOException
    {
        format("dfn", text);
    }
    public void blockquote(String text) throws IOException
    {
        format("blockquote", text);
    }
    private void format(String tag, String text) throws IOException
    {
        super.println("<"+tag+">"+escape(text)+"</"+tag+">");
    }
    @Override
    public void print(char c)
    {
        super.print(escape(c));
    }

    @Override
    public void print(String s)
    {
        super.print(escape(s));
    }

    @Override
    public void println(char c)
    {
        super.println(escape(c));
    }

    @Override
    public void println(String s)
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
