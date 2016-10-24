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
public interface MathExpressionParserIntf<T,M,F,P>
{
    void parse(MathExpression me, ExpressionHandler<T,M,F,P> handler) throws Exception;
    DEH parse(String me, boolean degrees, ExpressionHandler<T,M,F,P> handler) throws Exception;
    DEH parseBoolean(String me, boolean degrees, ExpressionHandler<T,M,F,P> handler) throws Exception;
}
