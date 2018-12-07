package com.developersam.primitivize.ast.decorated

import com.developersam.primitivize.ast.common.BinaryOperator
import com.developersam.primitivize.ast.type.ExprType
import com.developersam.primitivize.codegen.AstToCodeConverter
import com.developersam.primitivize.codegen.CodeConvertible
import com.developersam.primitivize.exceptions.IdentifierError
import java.util.LinkedList
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
     * [toMainIfElseBlock] returns the main expression as a chain of if else block.
     */
    fun toMainIfElseBlock(): List<IfElseBlockItem> {
        val list = LinkedList<IfElseBlockItem>()
        var expr = this
        while (expr is DecoratedExpression.IfElse) {
            val conditionExpr = expr.condition
            val actionExpr = expr.e1
            if (actionExpr is DecoratedExpression.IfElse) {
                actionExpr.toMainIfElseBlock().map { nestedIfElseItem ->
                    nestedIfElseItem.copy(condition = DecoratedExpression.Binary(
                            left = conditionExpr,
                            op = BinaryOperator.AND,
                            right = nestedIfElseItem.condition,
                            type = ExprType.Void
                    ))
                }.let(block = list::addAll)
            } else {
                list.add(element = IfElseBlockItem(condition = conditionExpr, action = actionExpr))
            }
            expr = expr.e2
        }
        val finalExpr = expr
        if (finalExpr is DecoratedExpression.IfElse) {
            list.addAll(elements = finalExpr.toMainIfElseBlock())
        } else {
            val item = IfElseBlockItem(
                    condition = DecoratedExpression.Literal(value = true), action = finalExpr
            )
            list.add(element = item)
        }
        return list
    }

    /**
     * Return a decorated expression where each expression inside it is mapped by [f].
     * This function is designed to reduce boilterplate.
     */
    protected abstract fun map(f: (DecoratedExpression) -> DecoratedExpression): DecoratedExpression

    /**
     * [replaceVariable] replaces variables from [from] to [to] inside this expression.
     */
    internal open fun replaceVariable(from: String, to: String): DecoratedExpression =
            map { it.replaceVariable(from, to) }

    /**
     * [replaceVariable] replaces variables from [from] to [to] expression inside this expression.
     */
    protected open fun replaceVariable(from: String, to: DecoratedExpression): DecoratedExpression =
            map { it.replaceVariable(from, to) }

    /**
     * [inlineFunction] returns an expression with the given function [f] inlined.
     */
    internal open fun inlineFunction(f: DecoratedTopLevelMember.Function): DecoratedExpression =
            map { it.inlineFunction(f) }

    /**
     * [replaceFunctionApplicationWithExpr] returns an expression with a function application
     * of [f] replaced with [expr].
     */
    internal open fun replaceFunctionApplicationWithExpr(
            f: DecoratedTopLevelMember.Function, expr: DecoratedExpression
    ): DecoratedExpression = map { it.replaceFunctionApplicationWithExpr(f, expr) }

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
         * @see DecoratedExpression.map
         */
        override fun map(f: (DecoratedExpression) -> DecoratedExpression): DecoratedExpression =
                this

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
         * Construct by a single int [value].
         */
        constructor(value: Int) : this(
                literal = CommonLiteral.Int(value = value), type = ExprType.Int
        )

        /**
         * Construct by a single boolean [value].
         */
        constructor(value: Boolean) : this(
                literal = CommonLiteral.Bool(value = value), type = ExprType.Bool
        )

        /**
         * @see CodeConvertible.acceptConversion
         */
        override fun acceptConversion(converter: AstToCodeConverter): Unit =
                converter.convert(node = this)

        /**
         * @see DecoratedExpression.map
         */
        override fun map(f: (DecoratedExpression) -> DecoratedExpression): DecoratedExpression =
                this

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
         * @see DecoratedExpression.map
         */
        override fun map(f: (DecoratedExpression) -> DecoratedExpression): DecoratedExpression =
                this

        /**
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedExpression =
                if (variable == from) copy(variable = to) else this

        /**
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: DecoratedExpression): DecoratedExpression =
                if (variable == from) to else this

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
         * @see DecoratedExpression.map
         */
        override fun map(f: (DecoratedExpression) -> DecoratedExpression): DecoratedExpression =
                Not(expr = f(expr))

    }

    /**
     * [FunctionApplication] with correct [type] is the function application expression,
     * with [identifier] as the function expression.
     *
     * @property identifier the function identifier to apply.
     * @property arguments a list of arguments.
     */
    data class FunctionApplication(
            val identifier: String, val arguments: List<DecoratedExpression>,
            override val type: ExprType
    ) : DecoratedExpression(precedenceLevel = 5) {

        /**
         * @see CodeConvertible.acceptConversion
         */
        override fun acceptConversion(converter: AstToCodeConverter): Unit =
                converter.convert(node = this)

        /**
         * @see DecoratedExpression.map
         */
        override fun map(f: (DecoratedExpression) -> DecoratedExpression): DecoratedExpression =
                this

        /**
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedExpression {
            val newArguments = arguments.map { it.replaceVariable(from, to) }
            val newIdentifier = if (identifier == from) to else identifier
            return copy(identifier = newIdentifier, arguments = newArguments)
        }

        /**
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: DecoratedExpression): DecoratedExpression {
            val newArguments = arguments.map { it.replaceVariable(from, to) }
            return copy(arguments = newArguments)
        }

        /**
         * @see DecoratedExpression.inlineFunction
         */
        override fun inlineFunction(f: DecoratedTopLevelMember.Function): DecoratedExpression =
                if (f.identifier != identifier) this else {
                    f.arguments.map { it.first }.zip(arguments)
                            .fold(initial = f.expr) { expr, (from, to) ->
                                expr.replaceVariable(from = from, to = to)
                            }
                }

        /**
         * @see DecoratedExpression.replaceFunctionApplicationWithExpr
         */
        override fun replaceFunctionApplicationWithExpr(
                f: DecoratedTopLevelMember.Function, expr: DecoratedExpression
        ): DecoratedExpression = if (f.identifier == identifier) expr else this

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
    ) : DecoratedExpression(precedenceLevel = 6) {

        /**
         * @see CodeConvertible.acceptConversion
         */
        override fun acceptConversion(converter: AstToCodeConverter): Unit =
                converter.convert(node = this)

        /**
         * @see DecoratedExpression.map
         */
        override fun map(f: (DecoratedExpression) -> DecoratedExpression): DecoratedExpression =
                copy(left = f(left), right = f(right))

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
         * @see DecoratedExpression.map
         */
        override fun map(f: (DecoratedExpression) -> DecoratedExpression): DecoratedExpression =
                IfElse(condition = f(condition), e1 = f(e1), e2 = f(e2), type = type)

    }

    /**
     * [Assign] with correct [type] represents the let expression of the form
     * [identifier] `=` [expr].
     *
     * @property identifier new identifier to name.
     * @property expr the expression for the identifier.
     */
    data class Assign(
            val identifier: String, val expr: DecoratedExpression
    ) : DecoratedExpression(precedenceLevel = 8) {

        override val type: ExprType = ExprType.Void

        /**
         * @see CodeConvertible.acceptConversion
         */
        override fun acceptConversion(converter: AstToCodeConverter): Unit =
                converter.convert(node = this)

        /**
         * @see DecoratedExpression.map
         */
        override fun map(f: (DecoratedExpression) -> DecoratedExpression): DecoratedExpression =
                copy(expr = f(expr))

        /**
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedExpression =
                copy(
                        identifier = if (identifier == from) to else identifier,
                        expr = expr.replaceVariable(from = from, to = to)
                )

        /**
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: DecoratedExpression): DecoratedExpression =
                copy(
                        identifier = if (identifier != from) identifier else {
                            throw IdentifierError.NotAssignableIdentifier(badIdentifier = from)
                        },
                        expr = expr.replaceVariable(from = from, to = to)
                )

    }

    /**
     * [Chain] represents the chaining expression with correct [type] of the form [e1] `;` [e2].
     *
     * @property e1 the first expression.
     * @property e2 the second expression.
     */
    data class Chain(val e1: DecoratedExpression, val e2: DecoratedExpression) :
            DecoratedExpression(precedenceLevel = 9) {

        override val type: ExprType = ExprType.Void

        /**
         * @see CodeConvertible.acceptConversion
         */
        override fun acceptConversion(converter: AstToCodeConverter) =
                converter.convert(node = this)

        /**
         * @see DecoratedExpression.map
         */
        override fun map(f: (DecoratedExpression) -> DecoratedExpression): DecoratedExpression =
                Chain(e1 = f(e1), e2 = f(e2))

    }

}
