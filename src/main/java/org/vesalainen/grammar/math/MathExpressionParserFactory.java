/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.grammar.math;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MathExpressionParserFactory
{
    public static final String MathExpressionParserClass = "org.vesalainen.grammar.impl.MathExpressionParserImpl";
    
    public static <T,M,F,P> MathExpressionParserIntf<T,M,F,P> getInstance()
    {
        try
        {
            Class<?> cls = Class.forName(MathExpressionParserClass);
            return (MathExpressionParserIntf) cls.newInstance();
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
}
