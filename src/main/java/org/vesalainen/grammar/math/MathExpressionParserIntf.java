/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.grammar.math;

import java.io.IOException;
import org.vesalainen.parser.annotation.MathExpression;

/**
 *
 * @author tkv
 */
public interface MathExpressionParserIntf<T,M,V>
{
    void parse(MathExpression me, ExpressionHandler<T,M,V> handler) throws IOException;
    DEH parse(String me, ExpressionHandler<T,M,V> handler) throws IOException;
}
