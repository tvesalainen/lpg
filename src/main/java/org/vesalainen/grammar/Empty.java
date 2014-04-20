package org.vesalainen.grammar;

public class Empty extends GTerminal
{

    public Empty()
    {
        super(1, "Empty");
    }

    @Override
    public boolean isEmpty()
    {
        return true;
    }

}
