package org.vesalainen.regex;

import org.vesalainen.parser.util.InputReader;
import org.vesalainen.grammar.state.DFAState;
import org.vesalainen.grammar.state.DFA;
import java.io.IOException;
/**
 * This class compiles match methods using DFA
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MatchCompiler<T> extends DFACompiler<T>
{
    public MatchCompiler(DFA<T> dfa, T errorToken, T eofToken)
    {
        super(dfa, errorToken, eofToken);
        //repeats = dfa.RemoveRepeatingTransitions();
    }

    @Override
    protected MatchCompiler<T> copy(DFA<T> ddfa)
    {
        return new MatchCompiler<>(ddfa, errorToken, eofToken);
    }
    
    @Override
    protected void accepting(DFAState<T> s) throws IOException, NoSuchMethodException
    {
        if (s.isAccepting())
        {
            tconst(s.getToken());
            tstore("accepted");
            int fixedEndLength = s.getFixedEndLength();
            if (fixedEndLength != 0)
            {
                tload("reader");
                iconst(fixedEndLength);
                invokevirtual(InputReader.class, "rewind", int.class);
            }
        }
        else
        {
            tconst(errorToken);
            tstore("accepted");
        }
    }

    /**
     *
     * @throws IOException
     * @throws NoSuchMethodException
     */
    protected void error() throws IOException, NoSuchMethodException
    {
        tload("accepted");
        tconst(errorToken);
        if_tcmpne(tokenType, "pushback");

        tload("accepted");
        treturn();
    }

    @Override
    protected void pushback() throws IOException, NoSuchMethodException
    {
        tload("reader");
        tload("cc");
        invokevirtual(InputReader.class, "unread", int.class);
        goto_n("exit");
    }

    @Override
    protected void exit() throws IOException, NoSuchMethodException
    {
        tload("accepted");
        treturn();
    }


}
