package org.vesalainen.grammar;

import java.io.IOException;
import org.vesalainen.parser.util.HtmlPrinter;
import org.vesalainen.regex.Regex.Option;

public class Eof extends GTerminal
{

    public Eof()
    {
        super(0, "Eof");
    }

    /**
     * Makes terminal act like eof. This is used in sub-grammars.
     * @param terminal 
     */
    Eof(int number, String name, String expression, int priority, int base, boolean signed, boolean whiteSpace, Option... options)
    {
        super(number, "Eof("+name+")", expression, priority, base, signed, whiteSpace, "", options);
    }
    
    @Override
    public boolean isEof()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return "Eof";
    }

    @Override
    public void print(HtmlPrinter p) throws IOException
    {
        p.print("Eof");
    }

    
}
