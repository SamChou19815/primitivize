package com.developersam.primitivize.parser

import com.developersam.primitivize.antlr.PLBaseVisitor
import com.developersam.primitivize.antlr.PLParser
import com.developersam.primitivize.ast.type.ExprType

/**
 * [TypeBuilder] builds expression type AST from parse tree.
 */
object TypeBuilder : PLBaseVisitor<ExprType>() {

    override fun visitType(ctx: PLParser.TypeContext): ExprType = when {
        ctx.UNIT() != null -> ExprType.Unit
        ctx.INT() != null -> ExprType.Int
        ctx.BOOL() != null -> ExprType.Bool
        ctx.ENUM() != null -> ExprType.Bool
        else -> error(message = "Impossible")
    }

}
