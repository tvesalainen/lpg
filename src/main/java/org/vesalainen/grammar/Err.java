package org.vesalainen.grammar;

public class Err extends GTerminal
{

    public Err()
    {
        super(2, "Err");
    }

    @Override
    public boolean isError()
    {
        return true;
    }

}
