package org.vesalainen.regex;

import org.vesalainen.parser.util.InputReader;
import org.vesalainen.grammar.state.DFAState;
import org.vesalainen.grammar.state.DFA;
import java.io.IOException;
/**
 * This class compiles match methods using DFA
 * @author tkv
 */
public class MatchCompiler<T> extends DFACompiler<T>
{
    public MatchCompiler(DFA<T> dfa, T errorToken, T eofToken)
    {
        super(dfa, errorToken, eofToken);
        repeats = dfa.RemoveRepeatingTransitions();
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
            c.tconst(s.getToken());
            c.tstore("accepted");
            int fixedEndLength = s.getFixedEndLength();
            if (fixedEndLength != 0)
            {
                c.tload("reader");
                c.iconst(fixedEndLength);
                c.invokevirtual(InputReader.class.getMethod("rewind", int.class));
            }
        }
        else
        {
            c.tconst(errorToken);
            c.tstore("accepted");
        }
    }

    protected void error() throws IOException, NoSuchMethodException
    {
        c.tload("accepted");
        c.tconst(errorToken);
        c.if_tcmpne(tokenClass, "pushback");

        c.tload("accepted");
        c.treturn();
    }

    protected void pushback() throws IOException, NoSuchMethodException
    {
        c.tload("reader");
        c.tload("cc");
        c.invokevirtual(InputReader.class.getMethod("unread", int.class));
        c.goto_n("exit");
    }

    protected void exit() throws IOException, NoSuchMethodException
    {
        c.tload("accepted");
        c.treturn();
    }


}
