package com.repopulse.service;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.WhileStmt;
import org.springframework.stereotype.Service;

@Service
public class ComplexityMetricService {

    public int calculate(MethodDeclaration method) {

        int complexity = 1;

        complexity += method.findAll(IfStmt.class).size();

        complexity += method.findAll(ForStmt.class).size();

        complexity += method.findAll(ForEachStmt.class).size();

        complexity += method.findAll(WhileStmt.class).size();

        complexity += method.findAll(DoStmt.class).size();

        complexity += method.findAll(CatchClause.class).size();

        complexity += method.findAll(ConditionalExpr.class).size();

        complexity += method.findAll(BinaryExpr.class)
                .stream()
                .filter(expression ->
                        expression.getOperator()
                                == BinaryExpr.Operator.AND
                                ||
                                expression.getOperator()
                                        == BinaryExpr.Operator.OR
                )
                .count();

        complexity += method.findAll(SwitchEntry.class).size();

        return complexity;
    }
}