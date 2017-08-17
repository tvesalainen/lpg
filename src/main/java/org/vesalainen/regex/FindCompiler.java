package org.vesalainen.regex;

import org.vesalainen.parser.util.InputReader;
import org.vesalainen.grammar.state.DFAState;
import org.vesalainen.grammar.state.DFA;
import java.io.IOException;
/**
 * This class compiles find methods using DFA
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class FindCompiler<T> extends DFACompiler<T>
{
    public FindCompiler(DFA<T> dfa, T errorToken, T eofToken)
    {
        super(dfa, errorToken, eofToken);
        dfa.calculateMaxFindSkip();
    }

    @Override
    protected FindCompiler<T> copy(DFA<T> ddfa)
    {
        return new FindCompiler<>(ddfa, errorToken, eofToken);
    }
    
    @Override
    protected void afterState(DFAState<T> s) throws IOException, NoSuchMethodException
    {
        if (dfa.isAcceptStart())
        {
            tload("reader");
            iconst(s.getAcceptStartLength());
            invokevirtual(InputReader.class, "setAcceptStart", int.class);
        }
    }

    @Override
    protected void accepting(DFAState<T> s) throws IOException, NoSuchMethodException
    {
        if (s.isAccepting())
        {
            tconst(s.getToken());
            tstore("accepted");
            tload("reader");
            invokevirtual(InputReader.class, "findAccept");
        }
    }

    @Override
    protected void error() throws IOException, NoSuchMethodException
    {
        tload("accepted");
        tconst(errorToken);
        if_tcmpne(tokenType, "pushback");

        tload("reader");
        invokevirtual(InputReader.class, "findRecover");
        goto_n("start");
    }

    @Override
    protected void pushback() throws IOException, NoSuchMethodException
    {
        tload("reader");
        invokevirtual(InputReader.class, "findPushback");
        goto_n("exit");
    }

    @Override
    protected void exit() throws IOException, NoSuchMethodException
    {
        tload("accepted");
        treturn();
    }

}
