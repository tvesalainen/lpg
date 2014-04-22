/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.grammar.math;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tkv
 */
public class MathExpressionParserFactory
{
    public static final String MathExpressionParserClass = "org.vesalainen.grammar.impl.MathExpressionParserImpl";
    
    public static MathExpressionParserIntf getInstance()
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
