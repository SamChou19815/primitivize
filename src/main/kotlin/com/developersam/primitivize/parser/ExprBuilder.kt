package com.developersam.primitivize.parser

import com.developersam.primitivize.antlr.PLBaseVisitor
import com.developersam.primitivize.antlr.PLParser
import com.developersam.primitivize.ast.common.BinaryOperator
import com.developersam.primitivize.ast.common.Literal
import com.developersam.primitivize.ast.raw.AssignExpr
import com.developersam.primitivize.ast.raw.BinaryExpr
import com.developersam.primitivize.ast.raw.ChainExpr
import com.developersam.primitivize.ast.raw.Expression
import com.developersam.primitivize.ast.raw.FunctionApplicationExpr
import com.developersam.primitivize.ast.raw.IfElseExpr
import com.developersam.primitivize.ast.raw.LiteralExpr
import com.developersam.primitivize.ast.raw.NotExpr
import com.developersam.primitivize.ast.raw.VariableIdentifierExpr
import com.developersam.primitivize.exceptions.InvalidLiteralError

/**
 * [ExprBuilder] builds expression AST from parse tree.
 */
internal object ExprBuilder : PLBaseVisitor<Expression>() {

    override fun visitNestedExpr(ctx: PLParser.NestedExprContext): Expression =
            ctx.expression().accept(this)

    override fun visitLiteralExpr(ctx: PLParser.LiteralExprContext): Expression {
        val literalObj: PLParser.LiteralContext = ctx.literal()
        // Case INT
        literalObj.IntegerLiteral()?.let { node ->
            val lineNo = node.symbol.line
            val text = node.text
            val intValue = text.toIntOrNull()
                    ?: throw InvalidLiteralError(lineNo = lineNo, invalidLiteral = text)
            return LiteralExpr(lineNo = lineNo, literal = Literal.Int(value = intValue))
        }
        // Case BOOL
        literalObj.BooleanLiteral()?.let { node ->
            val lineNo = node.symbol.line
            val text = node.text
            return when (text) {
                "true" -> LiteralExpr(lineNo = lineNo, literal = Literal.Bool(value = true))
                "false" -> LiteralExpr(lineNo = lineNo, literal = Literal.Bool(value = false))
                else -> throw InvalidLiteralError(lineNo = lineNo, invalidLiteral = text)
            }
        }
        throw InvalidLiteralError(lineNo = ctx.literal().start.line, invalidLiteral = ctx.text)
    }

    override fun visitIdentifierExpr(ctx: PLParser.IdentifierExprContext): Expression {
        val lineNo = ctx.start.line
        val variable = ctx.LowerIdentifier().text
        return VariableIdentifierExpr(lineNo = lineNo, variable = variable)
    }

    override fun visitNotExpr(ctx: PLParser.NotExprContext): Expression =
            NotExpr(lineNo = ctx.start.line, expr = ctx.expression().accept(this))

    override fun visitFactorExpr(ctx: PLParser.FactorExprContext): Expression =
            BinaryExpr(
                    lineNo = ctx.start.line,
                    left = ctx.expression(0).accept(this),
                    op = BinaryOperator.fromRaw(text = ctx.factorOperator().text),
                    right = ctx.expression(1).accept(this)
            )

    override fun visitTermExpr(ctx: PLParser.TermExprContext): Expression =
            BinaryExpr(
                    lineNo = ctx.start.line,
                    left = ctx.expression(0).accept(this),
                    op = BinaryOperator.fromRaw(text = ctx.termOperator().text),
                    right = ctx.expression(1).accept(this)
            )

    override fun visitComparisonExpr(ctx: PLParser.ComparisonExprContext): Expression =
            BinaryExpr(
                    lineNo = ctx.start.line,
                    left = ctx.expression(0).accept(this),
                    op = BinaryOperator.fromRaw(text = ctx.comparisonOperator().text),
                    right = ctx.expression(1).accept(this)
            )

    override fun visitConjunctionExpr(ctx: PLParser.ConjunctionExprContext): Expression =
            BinaryExpr(
                    lineNo = ctx.start.line,
                    left = ctx.expression(0).accept(this),
                    op = BinaryOperator.AND,
                    right = ctx.expression(1).accept(this)
            )

    override fun visitDisjunctionExpr(ctx: PLParser.DisjunctionExprContext): Expression =
            BinaryExpr(
                    lineNo = ctx.start.line,
                    left = ctx.expression(0).accept(this),
                    op = BinaryOperator.OR,
                    right = ctx.expression(1).accept(this)
            )

    override fun visitIfElseExpr(ctx: PLParser.IfElseExprContext): Expression =
            IfElseExpr(
                    lineNo = ctx.start.line,
                    condition = ctx.expression(0).accept(this),
                    e1 = ctx.expression(1).accept(this),
                    e2 = ctx.expression(2).accept(this)
            )

    override fun visitFunAppExpr(ctx: PLParser.FunAppExprContext): Expression =
            FunctionApplicationExpr(
                    lineNo = ctx.start.line, identifier = ctx.LowerIdentifier().text,
                    arguments = ctx.expression().map { it.accept(this) }
            )

    override fun visitAssignExpr(ctx: PLParser.AssignExprContext): Expression =
            AssignExpr(
                    lineNo = ctx.start.line, identifier = ctx.LowerIdentifier().text,
                    expr = ctx.expression().accept(this)
            )

    override fun visitChainExpr(ctx: PLParser.ChainExprContext): Expression =
            ChainExpr(
                    lineNo = ctx.start.line, e1 = ctx.expression(0).accept(this),
                    e2 = ctx.expression(1).accept(this)
            )

}
