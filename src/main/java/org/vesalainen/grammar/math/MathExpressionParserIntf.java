/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.grammar.math;

import org.vesalainen.parser.annotation.MathExpression;

/**
 *
 * @author tkv
 */
public interface MathExpressionParserIntf
{
    void parse(MathExpression me, MethodExpressionHandler handler) throws ReflectiveOperationException;
}
