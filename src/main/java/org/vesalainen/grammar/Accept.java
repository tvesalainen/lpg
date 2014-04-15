package org.vesalainen.grammar;

public class Accept extends Nonterminal
{

    public Accept()
    {
        super(3, "Accept");
    }

    public boolean isStart()
    {
        return true;
    }
}
