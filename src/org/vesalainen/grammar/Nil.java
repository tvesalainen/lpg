package org.vesalainen.grammar;

public class Nil extends Nonterminal
{

    public Nil()
    {
        super(4, "Nil");
    }

    @Override
    public boolean isNil()
    {
        return true;
    }
}
