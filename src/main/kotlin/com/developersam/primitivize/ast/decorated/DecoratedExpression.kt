package com.developersam.primitivize.ast.decorated

import com.developersam.primitivize.ast.common.BinaryOperator
import com.developersam.primitivize.ast.type.ExprType
import com.developersam.primitivize.codegen.AstToCodeConverter
import com.developersam.primitivize.codegen.CodeConvertible
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
            val item =
                    IfElseBlockItem(condition = expr.condition, action = expr.e1)
            list.add(element = item)
            expr = expr.e2
        }
        val item = IfElseBlockItem(
                condition = DecoratedExpression.Literal(value = true), action = expr
        )
        list.add(element = item)
        return list
    }

    /**
     * [replaceVariable] replaces variables from [from] to [to] inside this expression.
     */
    internal abstract fun replaceVariable(from: String, to: String): DecoratedExpression

    /**
     * [inlineFunction] returns the expression with the given function [f] inlined.
     */
    internal abstract fun inlineFunction(f: DecoratedTopLevelMember.Function): DecoratedExpression

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
         * @see DecoratedExpression.inlineFunction
         */
        override fun inlineFunction(f: DecoratedTopLevelMember.Function): DecoratedExpression = this

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
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedExpression = this

        /**
         * @see DecoratedExpression.inlineFunction
         */
        override fun inlineFunction(f: DecoratedTopLevelMember.Function): DecoratedExpression = this

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
                if (variable == from) copy(variable = to) else this

        /**
         * @see DecoratedExpression.inlineFunction
         */
        override fun inlineFunction(f: DecoratedTopLevelMember.Function): DecoratedExpression = this

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
         * @see DecoratedExpression.inlineFunction
         */
        override fun inlineFunction(f: DecoratedTopLevelMember.Function): DecoratedExpression =
                Not(expr = expr.inlineFunction(f = f))

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
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedExpression {
            val newArguments = arguments.map { it.replaceVariable(from, to) }
            val newIdentifier = if (identifier == from) to else identifier
            return copy(identifier = newIdentifier, arguments = newArguments)
        }

        /**
         * @see DecoratedExpression.inlineFunction
         */
        override fun inlineFunction(f: DecoratedTopLevelMember.Function): DecoratedExpression =
                if (f.identifier == identifier) f.expr else this

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
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedExpression =
                Binary(
                        left = left.replaceVariable(from, to), op = op,
                        right = right.replaceVariable(from, to), type = type
                )

        /**
         * @see DecoratedExpression.inlineFunction
         */
        override fun inlineFunction(f: DecoratedTopLevelMember.Function): DecoratedExpression =
                copy(left = left.inlineFunction(f = f), right = right.inlineFunction(f = f))

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
         * @see DecoratedExpression.inlineFunction
         */
        override fun inlineFunction(f: DecoratedTopLevelMember.Function): DecoratedExpression =
                copy(
                        condition = condition.inlineFunction(f = f),
                        e1 = e1.inlineFunction(f = f),
                        e2 = e2.inlineFunction(f = f)
                )

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
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedExpression =
                copy(
                        identifier = if (identifier == from) to else identifier,
                        expr = expr.replaceVariable(from = from, to = to)
                )

        /**
         * @see DecoratedExpression.inlineFunction
         */
        override fun inlineFunction(f: DecoratedTopLevelMember.Function): DecoratedExpression =
                copy(expr = expr.inlineFunction(f = f))

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
         * @see DecoratedExpression.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedExpression =
                Chain(e1 = e1.replaceVariable(from, to), e2 = e2.replaceVariable(from, to))

        /**
         * @see DecoratedExpression.inlineFunction
         */
        override fun inlineFunction(f: DecoratedTopLevelMember.Function): DecoratedExpression =
                Chain(
                        e1 = e1.inlineFunction(f = f), e2 = e2.inlineFunction(f = f)
                )

    }

}
