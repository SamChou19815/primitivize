package com.developersam.primitivize.ast.decorated

import com.developersam.primitivize.ast.common.BinaryOperator
import com.developersam.primitivize.ast.type.ExprType
import com.developersam.primitivize.codegen.AstToCodeConverter
import com.developersam.primitivize.codegen.CodeConvertible
import com.developersam.primitivize.lowering.VariableRenamingService
import com.developersam.primitivize.ast.common.Literal as CommonLiteral

/**
 * [DecoratedExpression] is an expression with a correct decorated type.
 *
 * @param precedenceLevel smaller this number, higher the precedence.
 */
sealed class DecoratedExpression(private val precedenceLevel: Int) : CodeConvertible {

    /**
     * [type] is the type decoration.
     */
    abstract val type: ExprType

    /**
     * [replaceVariable] replaces variables from [from] to [to] inside this expression.
     */
    internal abstract fun replaceVariable(from: String, to: String): DecoratedExpression

    /**
     * [rename] returns a new expression with variable renamed with the help of [service].
     */
    internal abstract fun rename(service: VariableRenamingService): DecoratedExpression

    /**
     * [hasLowerPrecedence] returns whether this expression has lower precedence than [parent].
     */
    fun hasLowerPrecedence(parent: DecoratedExpression): Boolean =
            if (this is Binary && parent is Binary) {
                op.precedenceLevel >= parent.op.precedenceLevel
            } else {
                precedenceLevel > parent.precedenceLevel
            }

    /**
     * [Dummy] represents a dummy decorated expression used as a placeholder.
     * It should be used with primitive and provided runtime functions.
     */
    object Dummy : DecoratedExpression(precedenceLevel = -1) {

        override val type: ExprType get() = throw UnsupportedOperationException()

        /**
         * @see CodeConvertible.acceptConversion
         */
        override fun acceptConversion(converter: AstToCodeConverter) {
            throw UnsupportedOperationException()
        }

        /**
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedExpression = this

        /**
         * @see DecoratedExpression.rename
         */
        override fun rename(service: VariableRenamingService): DecoratedExpression = this

    }

    /**
     * [Literal] with correct [type] represents a [literal] as an expression.
     *
     * @property literal the literal object.
     */
    data class Literal(
            val literal: CommonLiteral, override val type: ExprType
    ) : DecoratedExpression(precedenceLevel = 0) {

        /**
         * @see CodeConvertible.acceptConversion
         */
        override fun acceptConversion(converter: AstToCodeConverter): Unit =
                converter.convert(node = this)

        /**
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedExpression = this

        /**
         * @see DecoratedExpression.rename
         */
        override fun rename(service: VariableRenamingService): DecoratedExpression = this

    }

    /**
     * [VariableIdentifier] with correct [type] represents a [variable] identifier as an
     * expression.
     *
     * @property variable the variable to refer to.
     */
    data class VariableIdentifier(
            val variable: String, override val type: ExprType
    ) : DecoratedExpression(precedenceLevel = 1) {

        /**
         * @see CodeConvertible.acceptConversion
         */
        override fun acceptConversion(converter: AstToCodeConverter): Unit =
                converter.convert(node = this)

        /**
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedExpression =
                if (variable == from) VariableIdentifier(variable = to, type = type) else this

        /**
         * @see DecoratedExpression.rename
         */
        override fun rename(service: VariableRenamingService): DecoratedExpression = this

    }

    /**
     * [Not] with correct [type] represents the logical inversion of expression [expr].
     *
     * @property expr the expression to invert.
     */
    data class Not(
            val expr: DecoratedExpression
    ) : DecoratedExpression(precedenceLevel = 4) {

        override val type: ExprType = ExprType.Bool

        /**
         * @see CodeConvertible.acceptConversion
         */
        override fun acceptConversion(converter: AstToCodeConverter): Unit =
                converter.convert(node = this)

        /**
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedExpression =
                Not(expr = expr.replaceVariable(from, to))

        /**
         * @see DecoratedExpression.rename
         */
        override fun rename(service: VariableRenamingService): DecoratedExpression =
                Not(expr = expr.rename(service = service))

    }

    /**
     * [Binary] with correct [type] represents a binary expression with operator [op]
     * between [left] and [right].
     *
     * @property left left part.
     * @property op the operator.
     * @property right right part.
     */
    data class Binary(
            val left: DecoratedExpression, val op: BinaryOperator, val right: DecoratedExpression,
            override val type: ExprType
    ) : DecoratedExpression(precedenceLevel = 5) {

        /**
         * @see CodeConvertible.acceptConversion
         */
        override fun acceptConversion(converter: AstToCodeConverter): Unit =
                converter.convert(node = this)

        /**
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedExpression =
                Binary(
                        left = left.replaceVariable(from, to), op = op,
                        right = right.replaceVariable(from, to), type = type
                )

        /**
         * @see DecoratedExpression.rename
         */
        override fun rename(service: VariableRenamingService): DecoratedExpression =
                Binary(
                        left = left.rename(service = service), op = op,
                        right = right.rename(service = service), type = type
                )

    }

    /**
     * [IfElse] with correct [type] represents the if else expression, guarded by [condition] and
     * having two branches [e1] and [e2].
     *
     * @property condition the condition to check.
     * @property e1 expression of the first branch.
     * @property e2 expression of the second branch.
     */
    data class IfElse(
            val condition: DecoratedExpression, val e1: DecoratedExpression,
            val e2: DecoratedExpression, override val type: ExprType
    ) : DecoratedExpression(precedenceLevel = 7) {

        /**
         * @see CodeConvertible.acceptConversion
         */
        override fun acceptConversion(converter: AstToCodeConverter): Unit =
                converter.convert(node = this)

        /**
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedExpression =
                IfElse(
                        condition = condition.replaceVariable(from, to),
                        e1 = e1.replaceVariable(from, to),
                        e2 = e2.replaceVariable(from, to),
                        type = type
                )

        /**
         * @see DecoratedExpression.rename
         */
        override fun rename(service: VariableRenamingService): DecoratedExpression =
                IfElse(
                        condition = condition.rename(service = service),
                        e1 = e1.rename(service = service),
                        e2 = e2.rename(service = service),
                        type = type
                )

    }

    /**
     * [FunctionApplication] with correct [type] is the function application expression,
     * with [functionExpr] as the function and [arguments] as arguments of the function.
     *
     * @property functionExpr the function expression to apply.
     * @property arguments arguments to supply.
     */
    data class FunctionApplication(
            val functionExpr: DecoratedExpression, val arguments: List<DecoratedExpression>,
            override val type: ExprType
    ) : DecoratedExpression(precedenceLevel = 9) {

        /**
         * @see CodeConvertible.acceptConversion
         */
        override fun acceptConversion(converter: AstToCodeConverter): Unit =
                converter.convert(node = this)

        /**
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedExpression =
                FunctionApplication(
                        functionExpr = functionExpr.replaceVariable(from, to),
                        arguments = arguments.map { it.replaceVariable(from, to) },
                        type = type
                )

        /**
         * @see DecoratedExpression.rename
         */
        override fun rename(service: VariableRenamingService): DecoratedExpression =
                FunctionApplication(
                        functionExpr = functionExpr.rename(service = service),
                        arguments = arguments, type = type
                )

    }

    /**
     * [Let] with correct [type] represents the let expression of the form
     * `let` [identifier] `=` [e1] `;` [e2]
     * If [identifier] is `null`, it means it's a wildcard.
     *
     * @property identifier new identifier to name.
     * @property e1 the expression for the identifier.
     * @property e2 the expression after the let.
     * @property type type of the let expression.
     */
    data class Let(
            val identifier: String?, val e1: DecoratedExpression, val e2: DecoratedExpression,
            override val type: ExprType
    ) : DecoratedExpression(precedenceLevel = 12) {

        /**
         * @see CodeConvertible.acceptConversion
         */
        override fun acceptConversion(converter: AstToCodeConverter): Unit =
                converter.convert(node = this)

        /**
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedExpression =
                Let(
                        identifier = identifier,
                        e1 = e1.replaceVariable(from, to),
                        e2 = e2.replaceVariable(from, to),
                        type = type
                )

        /**
         * @see DecoratedExpression.rename
         */
        override fun rename(service: VariableRenamingService): DecoratedExpression =
                if (identifier == null) this else {
                    val oldName = identifier
                    val newName = service.nextVariableName
                    val newE2 = e2.replaceVariable(from = oldName, to = newName)
                            .rename(service = service)
                    copy(identifier = newName, e2 = newE2)
                }
    }

}
