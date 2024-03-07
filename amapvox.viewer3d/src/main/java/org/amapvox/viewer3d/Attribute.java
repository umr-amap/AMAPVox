/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.viewer3d;

import java.util.Set;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Attribute {
    
    private final String name;
    private final String expressionString;
    private final Expression expression;
    
    public Attribute(String name, String expression, Set<String> variableNames){
        
        this.expressionString = expression;
        this.name = name;
        this.expression = new ExpressionBuilder(expression).variables(variableNames).build();
        //this.expression = new ExpressionBuilder(expressionString).variables(variablesNames).build();
    }

    public Expression getExpression() {
        return expression;
    }

    public String getName() {
        return name;
    }

    public String getExpressionString() {
        return expressionString;
    }
    
    
}
