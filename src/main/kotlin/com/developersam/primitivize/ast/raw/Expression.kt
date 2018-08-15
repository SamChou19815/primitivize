package com.developersam.primitivize.ast.raw

import com.developersam.primitivize.ast.common.BinaryOperator
import com.developersam.primitivize.ast.common.BinaryOperator.AND
import com.developersam.primitivize.ast.common.BinaryOperator.DIV
import com.developersam.primitivize.ast.common.BinaryOperator.EQ
import com.developersam.primitivize.ast.common.BinaryOperator.GE
import com.developersam.primitivize.ast.common.BinaryOperator.GT
import com.developersam.primitivize.ast.common.BinaryOperator.LE
import com.developersam.primitivize.ast.common.BinaryOperator.LT
import com.developersam.primitivize.ast.common.BinaryOperator.MINUS
import com.developersam.primitivize.ast.common.BinaryOperator.MOD
import com.developersam.primitivize.ast.common.BinaryOperator.MUL
import com.developersam.primitivize.ast.common.BinaryOperator.NE
import com.developersam.primitivize.ast.common.BinaryOperator.OR
import com.developersam.primitivize.ast.common.BinaryOperator.PLUS
import com.developersam.primitivize.ast.common.Literal
import com.developersam.primitivize.ast.decorated.DecoratedExpression
import com.developersam.primitivize.ast.type.ExprType
import com.developersam.primitivize.environment.TypeEnv
import com.developersam.primitivize.exceptions.IdentifierError
import com.developersam.primitivize.exceptions.WrongNumberOfArgsError
import com.developersam.primitivize.exceptions.UnexpectedTypeError

/**
 * [Expression] represents a set of supported expression.
 */
sealed class Expression {

    /**
     * [lineNo] reports the line number of the expression.
     */
    abstract val lineNo: Int

    /**
     * [typeCheck] returns the decorated expression with the inferred type  under the given
     * [environment].
     *
     * If the type checking failed, it should throw [UnexpectedTypeError] to indicate what's wrong.
     */
    internal abstract fun typeCheck(environment: TypeEnv): DecoratedExpression

}

/**
 * [LiteralExpr] represents a [literal] as an expression at [lineNo].
 *
 * @property literal the literal object.
 */
data class LiteralExpr(override val lineNo: Int, val literal: Literal) : Expression() {

    /**
     * @see Expression.typeCheck
     */
    override fun typeCheck(environment: TypeEnv): DecoratedExpression =
            DecoratedExpression.Literal(literal = literal, type = literal.inferredType)

}

/**
 * [VariableIdentifierExpr] represents a [variable] identifier as an expression at [lineNo].
 *
 * @property variable the variable to refer to.
 */
data class VariableIdentifierExpr(
        override val lineNo: Int, val variable: String
) : Expression() {

    /**
     * @see Expression.typeCheck
     */
    override fun typeCheck(environment: TypeEnv): DecoratedExpression =
            environment[variable]?.let { typeExpr ->
                DecoratedExpression.VariableIdentifier(variable = variable, type = typeExpr)
            } ?: throw IdentifierError.UndefinedIdentifier(lineNo, variable)
}

/**
 * [NotExpr] represents the logical inversion of expression [expr] at [lineNo].
 *
 * @property expr the expression to invert.
 */
data class NotExpr(override val lineNo: Int, val expr: Expression) : Expression() {

    /**
     * @see Expression.typeCheck
     */
    override fun typeCheck(environment: TypeEnv): DecoratedExpression {
        val e = expr.typeCheck(environment = environment)
        UnexpectedTypeError.check(
                lineNo = expr.lineNo, expectedType = ExprType.Bool, actualType = e.type
        )
        return DecoratedExpression.Not(expr = e)
    }

}

/**
 * [BinaryExpr] represents a binary expression with operator [op] between [left] and [right] at
 * [lineNo].
 *
 * @property left left part.
 * @property op the operator.
 * @property right right part.
 */
data class BinaryExpr(
        override val lineNo: Int,
        val left: Expression, val op: BinaryOperator, val right: Expression
) : Expression() {

    /**
     * @see Expression.typeCheck
     */
    override fun typeCheck(environment: TypeEnv): DecoratedExpression {
        val leftExpr = left.typeCheck(environment = environment)
        val leftType = leftExpr.type
        val rightExpr = right.typeCheck(environment = environment)
        val rightType = rightExpr.type
        val type = when (op) {
            MUL, DIV, MOD, PLUS, MINUS -> {
                // int binary operators
                UnexpectedTypeError.check(
                        lineNo = left.lineNo, expectedType = ExprType.Int, actualType = leftType
                )
                UnexpectedTypeError.check(
                        lineNo = right.lineNo, expectedType = ExprType.Int, actualType = rightType
                )
                ExprType.Bool
            }
            LT, LE, GT, GE -> {
                // comparison type operator
                UnexpectedTypeError.check(
                        lineNo = left.lineNo, expectedType = ExprType.Int,
                        actualType = leftType
                )
                UnexpectedTypeError.check(
                        lineNo = right.lineNo, expectedType = ExprType.Int,
                        actualType = rightType
                )
                ExprType.Bool
            }
            EQ, NE -> {
                // equality operator
                UnexpectedTypeError.check(
                        lineNo = right.lineNo, expectedType = leftType, actualType = rightType
                )
                ExprType.Bool
            }
            AND, OR -> {
                // conjunction and disjunction
                UnexpectedTypeError.check(
                        lineNo = left.lineNo, expectedType = ExprType.Bool, actualType = leftType
                )
                UnexpectedTypeError.check(
                        lineNo = right.lineNo, expectedType = ExprType.Bool, actualType = rightType
                )
                ExprType.Bool
            }
        }
        return DecoratedExpression.Binary(left = leftExpr, op = op, right = rightExpr, type = type)
    }

}

/**
 * [IfElseExpr] represents the if else expression, guarded by [condition] and having two
 * branches [e1] and [e2] at [lineNo].
 *
 * @property condition the condition to check.
 * @property e1 expression of the first branch.
 * @property e2 expression of the second branch.
 */
data class IfElseExpr(
        override val lineNo: Int, val condition: Expression, val e1: Expression, val e2: Expression
) : Expression() {

    /**
     * @see Expression.typeCheck
     */
    override fun typeCheck(environment: TypeEnv): DecoratedExpression {
        val conditionExpr = condition.typeCheck(environment = environment)
        UnexpectedTypeError.check(
                lineNo = condition.lineNo, expectedType = ExprType.Bool,
                actualType = conditionExpr.type
        )
        val decoratedE1 = e1.typeCheck(environment = environment)
        val t1 = decoratedE1.type
        val decoratedE2 = e2.typeCheck(environment = environment)
        val t2 = decoratedE2.type
        UnexpectedTypeError.check(
                lineNo = e2.lineNo, expectedType = t1, actualType = t2
        )
        return DecoratedExpression.IfElse(
                condition = conditionExpr, e1 = decoratedE1, e2 = decoratedE2, type = t1
        )
    }

}

/**
 * [FunctionApplicationExpr] is the function application expression, with [functionExpr] as the
 * function and [arguments] as arguments of the function at [lineNo].
 *
 * @property functionExpr the function expression to apply.
 * @property arguments arguments to supply.
 */
data class FunctionApplicationExpr(
        override val lineNo: Int, val functionExpr: Expression, val arguments: List<Expression>
) : Expression() {

    /**
     * @see Expression.typeCheck
     */
    override fun typeCheck(environment: TypeEnv): DecoratedExpression {
        val decoratedArguments = arguments.map { it.typeCheck(environment = environment) }
        val decoratedFunctionExpr = functionExpr.typeCheck(environment = environment)
        val functionTypeOpt = decoratedFunctionExpr.type
        val functionType = functionTypeOpt as? ExprType.Function
                ?: throw UnexpectedTypeError(
                        lineNo = functionExpr.lineNo, expectedType = "<function>",
                        actualType = functionTypeOpt
                )
        val expectedArgumentTypes = functionType.argumentTypes
        val expectedArgsSize = expectedArgumentTypes.size
        val actualArgsSize = decoratedArguments.size
        if (expectedArgsSize != actualArgsSize) {
            throw WrongNumberOfArgsError(
                    lineNo = lineNo, expected = expectedArgsSize, actual = actualArgsSize
            )
        }
        for (i in decoratedArguments.indices) {
            val decoratedExpr = decoratedArguments[i]
            val expType = expectedArgumentTypes[i]
            val exprType = decoratedExpr.type
            UnexpectedTypeError.check(
                    lineNo = arguments[i].lineNo, expectedType = expType, actualType = exprType
            )
        }
        return DecoratedExpression.FunctionApplication(
                functionExpr = decoratedFunctionExpr, arguments = decoratedArguments,
                type = functionType.returnType
        )
    }

}

/**
 * [AssignExpr] represents the assign expression at [lineNo] of the form [identifier] `=` [expr].
 *
 * @property identifier new identifier to name.
 * @property expr the expression for the identifier.
 */
data class AssignExpr(
        override val lineNo: Int, val identifier: String, val expr: Expression
) : Expression() {

    /**
     * @see Expression.typeCheck
     */
    override fun typeCheck(environment: TypeEnv): DecoratedExpression {
        if (environment[identifier] != null) {
            throw IdentifierError.ShadowedName(lineNo, identifier)
        }
        val decoratedExpr = expr.typeCheck(environment = environment)
        return DecoratedExpression.Assign(identifier = identifier, expr = decoratedExpr)
    }

}

/**
 * [ChainExpr] represents the chaining expression at [lineNo] of the form [e1] `;` [e2].
 *
 * @property e1 the first expression.
 * @property e2 the second expression.
 */
data class ChainExpr(
        override val lineNo: Int, val e1: Expression, val e2: Expression
) : Expression() {

    /**
     * @see Expression.typeCheck
     */
    override fun typeCheck(environment: TypeEnv): DecoratedExpression =
            DecoratedExpression.Chain(
                    e1 = e1.typeCheck(environment = environment),
                    e2 = e2.typeCheck(environment = environment)
            )

}
