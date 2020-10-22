package com.developersam.primitivize.parser

import com.developersam.primitivize.antlr.PLBaseVisitor
import com.developersam.primitivize.antlr.PLParser
import com.developersam.primitivize.ast.type.ExprType

/**
 * [TypeBuilder] builds expression type AST from parse tree.
 */
object TypeBuilder : PLBaseVisitor<ExprType>() {

    override fun visitExprType(ctx: PLParser.ExprTypeContext): ExprType = when {
        ctx.VOID() != null -> ExprType.Void
        ctx.INT() != null -> ExprType.Int
        ctx.BOOL() != null -> ExprType.Bool
        else -> error(message = "Impossible")
    }

}
