package org.vesalainen.regex;

import org.vesalainen.parser.util.InputReader;
import org.vesalainen.grammar.state.DFAState;
import org.vesalainen.grammar.state.DFA;
import java.io.IOException;
/**
 * This class compiles find methods using DFA
 * @author tkv
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
            try
            {
                c.tload("reader");
                c.iconst(s.getAcceptStartLength());
                c.invokevirtual(InputReader.class.getMethod("setAcceptStart", int.class));
            }

            catch (NoSuchMethodException | SecurityException ex)
            {
                throw new IOException(ex);
            }
        }
    }

    @Override
    protected void accepting(DFAState<T> s) throws IOException, NoSuchMethodException
    {
        if (s.isAccepting())
        {
            c.tconst(s.getToken());
            c.tstore("accepted");
            c.tload("reader");
            c.invokevirtual(InputReader.class.getMethod("findAccept"));
        }
    }

    protected void error() throws IOException, NoSuchMethodException
    {
        c.tload("accepted");
        c.tconst(errorToken);
        c.if_tcmpne(tokenClass, "pushback");

        c.tload("reader");
        c.invokevirtual(InputReader.class.getMethod("findRecover"));
        c.goto_n("start");
    }

    protected void pushback() throws IOException, NoSuchMethodException
    {
        c.tload("reader");
        c.invokevirtual(InputReader.class.getMethod("findPushback"));
        c.goto_n("exit");
    }

    protected void exit() throws IOException, NoSuchMethodException
    {
        c.tload("accepted");
        c.treturn();
    }

}
