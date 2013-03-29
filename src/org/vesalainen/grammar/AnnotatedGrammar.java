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
package org.vesalainen.grammar;

import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.annotation.Terminals;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.Typ;
import org.vesalainen.parser.annotation.ReservedWords;
import org.vesalainen.regex.SyntaxErrorException;

/**
 * AnnotatedGrammar creates a grammar from annotations in given class and it's
 * super classes.
 * @author tkv
 */
public class AnnotatedGrammar extends Grammar
{
    public AnnotatedGrammar(TypeElement parserClass) throws IOException
    {
        super(parserClass.getAnnotation(GrammarDef.class));

        TypeElement cls = parserClass;
        List<? extends ExecutableElement> methods = El.getEffectiveMethods(cls);
        findTerminals(methods);
        findRules(methods);
        findReservedWords(methods);
        while (cls != null)
        {
        // terminals defined at class
            List<Terminal> terminalList = getTerminals(cls);
            for (Terminal term : terminalList)
            {
                try
                {
                    addTerminal(term.left(), term.expression(), term.priority(), term.radix(), term.options());
                }
                catch (SyntaxErrorException ex)
                {
                    throw new GrammarException(parserClass.toString(), ex);
                }
            }
            // rules defined in class
            List<Rule> ruleList = getRules(cls);
            for (Rule rule : ruleList)
            {
                try
                {
                    addRule(rule.left(), rule.value());
                }
                catch (SyntaxErrorException ex)
                {
                    throw new GrammarException(parserClass.toString(), ex);
                }
            }
            ReservedWords rw = cls.getAnnotation(ReservedWords.class);
            if (rw != null)
            {
                for (String expression : rw.value())
                {
                    if (rw.left().isEmpty())
                    {
                        addTerminal(expression, expression, rw.priority(), 10, rw.options());
                    }
                    else
                    {
                        addTerminal(rw.left(), expression, rw.priority(), 10, rw.options());
                    }
                }
            }
            cls = (TypeElement) Typ.asElement(cls.getSuperclass());
        }
    }
    private void findTerminals(List<? extends ExecutableElement> methods)
    {
        // terminals defined in methods
        for (ExecutableElement method : methods)
        {
            List<Terminal> terminalList = getTerminals(method);
            for (Terminal term : terminalList)
            {
                String name = term.left();
                if (name.isEmpty())
                {
                    name = method.getSimpleName().toString();
                }
                try
                {
                    addTerminal(method, name, term.expression(), term.doc(), term.priority(), term.radix(), term.options());
                }
                catch (SyntaxErrorException ex)
                {
                    throw new GrammarException(method.toString(), ex);
                }
            }
        }
    }
    private void findRules(List<? extends ExecutableElement> methods) throws IOException
    {
        // rules defined in methods
        for (ExecutableElement method : methods)
        {
            List<Rule> ruleList = getRules(method);
            for (Rule rule : ruleList)
            {
                String name = rule.left();
                if (name.isEmpty())
                {
                    name = method.getSimpleName().toString();
                }
                try
                {
                    addRule(method, name, rule.value());
                }
                catch (SyntaxErrorException ex)
                {
                    throw new GrammarException(method.toString(), ex);
                }
            }
        }
    }

    private List<Terminal> getTerminals(Element element)
    {
        List<Terminal> terminalList = new ArrayList<>();
        Terminals terms = element.getAnnotation(Terminals.class);
        if (terms != null)
        {
            terminalList.addAll(Arrays.asList(terms.value()));
        }
        else
        {
            Terminal term = element.getAnnotation(Terminal.class);
            if (term != null)
            {
                terminalList.add(term);
            }
        }
        return terminalList;
    }
    private List<Rule> getRules(Element element)
    {
        List<Rule> statementList = new ArrayList<>();
        Rules statements = element.getAnnotation(Rules.class);
        if (statements != null)
        {
            statementList.addAll(Arrays.asList(statements.value()));
        }
        else
        {
            Rule statement = element.getAnnotation(Rule.class);
            if (statement != null)
            {
                statementList.add(statement);
            }
        }
        return statementList;
    }

    private void findReservedWords(List<? extends ExecutableElement> methods)
    {
        for (ExecutableElement method : methods)
        {
            ReservedWords rw = method.getAnnotation(ReservedWords.class);
            if (rw != null)
            {
                for (String expression : rw.value())
                {
                    if (rw.left().isEmpty())
                    {
                        addTerminal(method, expression, expression, "", rw.priority(), 10, rw.options());
                    }
                    else
                    {
                        addTerminal(method, rw.left(), expression, "", rw.priority(), 10, rw.options());
                    }
                }
            }
        }
    }
}
