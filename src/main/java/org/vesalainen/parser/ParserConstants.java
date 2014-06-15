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
package org.vesalainen.parser;

/**
 * Parser constants which can be used to load parser local variables for example
 * in recover method.
 * Example
 * <code>
     @RecoverMethod
    public void recover(
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext(ParserConstants.InputReader) InputReader reader,
            @ParserContext(ParserConstants.Exception) Exception ex
            ) throws IOException
    {
      ...
* </code>
 * @author Timo Vesalainen
 */
public interface ParserConstants
{
    /**
     * Name of local variable 0 = this
     */
    static final String This = "this";
    /**
     * Old name preserved for compatibility
     */
    static final String THIS = This;
    /**
     * Name of local variable containing Input
     */
    static final String InputSource = "$inputReader";
    /**
     * Old name preserved for compatibility
     */
    static final String INPUTREADER = InputSource;
    /**
     * Name of local variable containing stack pointer
     */
    static final String SP = "$sp";
    /**
     * Name of local variable containing current token number
     */
    static final String CurrentToken = "$curTok";
    /**
     * Old name preserved for compatibility
     */
    static final String CURTOK = CurrentToken;
    /**
     * Name of local variable containing read token number
     */
    static final String Token = "$token";
    /**
     * Old name preserved for compatibility
     */
    static final String TOKEN = Token;
    /**
     * Name of local variable containing current type (= ObjectType ordinal)
     */
    static final String CurrentType = "$curType";
    /**
     * Old name preserved for compatibility
     */
    static final String CURTYPE = CurrentType;
    /**
     * Name of local variable containing state stack (=int[])
     */
    static final String StateStack = "$stateStack";
    /**
     * Old name preserved for compatibility
     */
    static final String STATESTACK = StateStack;
    /**
     * Name of local variable containing type stack (=int[])
     */
    static final String TypeStack = "$typeStack";
    /**
     * Old name preserved for compatibility
     */
    static final String TYPESTACK = TypeStack;
    /**
     * Name of local variable containing value stack (= Object[])
     */
    static final String ValueStack = "$valueStack";
    /**
     * Old name preserved for compatibility
     */
    static final String VALUESTACK = ValueStack;
    /**
     * Name of prefix for current type (= ObjectType toString())
     */
    static final String CUR = "$cur";
    /**
     * Input method prefix
     */
    static final String INPUT = "$input";
    /**
     * Lookahead input method prefix
     */
    static final String LAINPUT = "$laInput";
    /**
     * Name of local variable containing lookahead state
     */
    static final String LaState = "$laState";
    /**
     * Old name preserved for compatibility
     */
    static final String LASTATE = LaState;
    /**
     * Name of local variable containing lookahead token number
     */
    static final String LaToken = "$laToken";
    /**
     * Old name preserved for compatibility
     */
    static final String LATOKEN = LaToken;
    /**
     * Name of local variable containing lookahead length
     */
    static final String LaLength = "$laLength";
    /**
     * Old name preserved for compatibility
     */
    static final String LALENGTH = LaLength;
    /**
     * Error token number
     */
    static final int ERROR = -1;
    /**
     * Eof token number
     */
    static final int EOF = 0;
    
    /**
     * Name of local variable 1. The original input object from which the InputReader is constructed.
     */
    static final String IN = "$in";
    /**
     * Prefix of generated parse method
     */
    static final String ParseMethodPrefix = "$parse-";
    /**
     * Old name preserved for compatibility
     */
    static final String PARSEMETHODPREFIX = ParseMethodPrefix;

    /**
     * Name of getToken method. Available only if parser implements ParserInfo.
     */
    static final String GetToken = "getToken";
    /**
     * Old name preserved for compatibility
     */
    static final String GETTOKEN = GetToken;
    /**
     * Name of the getRule parameter containing rule number. Available only if parser implements ParserInfo. 
     */
    static final String Rule = "$rule";
    /**
     * Old name preserved for compatibility
     */
    static final String RULE = Rule;
    /**
     * Name of getRule method. Available only if parser implements ParserInfo.
     */
    static final String GetRule = "getRule";
    /**
     * Old name preserved for compatibility
     */
    static final String GETRULE = GetRule;
    /**
     * Name of getExpected method. Available only if parser implements ParserInfo.
     */
    static final String GetExpected = "getExpected";
    /**
     * Old name preserved for compatibility
     */
    static final String GETEXPECTED = GetExpected;
    /**
     * Name of the local variable containing description of rule
     * at the current parse state. Available only if parser implements ParserInfo.
     */
    static final String RuleDescription = "$ruleDescription";
    /**
     * Name of the local variable containing description of expected input tokens
     * at the current parse state. Available only if parser implements ParserInfo.
     */
    static final String ExpectedDescription = "$expectedDescription";
    /**
     * Name of the local variable containing description of last read input token
     * at the current parse state. Available only if parser implements ParserInfo.
     */
    static final String LastToken = "$lastToken";
    /**
     * Old name preserved for compatibility
     */
    static final String ARG = "$arg";
    /**
     * Name of the local variable containing source stack. Available only if 
     * one of reducer types implements ParserLineLocator or ParserOffsetLocator.
     */
    static final String SourceStack = "$sourceStack";
    /**
     * Old name preserved for compatibility
     */
    static final String SOURCESTACK = SourceStack;
    /**
     * Name of the local variable containing line stack. Available only if 
     * one of reducer types implements ParserLineLocator.
     */
    static final String LineStack = "$lineStack";
    /**
     * Old name preserved for compatibility
     */
    static final String LINESTACK = LineStack;
    /**
     * Name of the local variable containing column stack. Available only if 
     * one of reducer types implements ParserLineLocator.
     */
    static final String ColumnStack = "$columnStack";
    /**
     * Old name preserved for compatibility
     */
    static final String COLUMNSTACK = ColumnStack;
    /**
     * Name of the local variable containing offset stack. Available only if 
     * one of reducer types implements ParserOffsetLocator.
     */
    static final String OffsetStack = "$offsetStack";
    /**
     * Old name preserved for compatibility
     */
    static final String OFFSETSTACK = OffsetStack;
    /**
     * Name of the local variable containing thrown exception
     */
    static final String Exception = "$exception";
    /**
     * Old name preserved for compatibility
     */
    static final String THROWABLE = Exception;
}
