package com.developersam.primitivize.ast.decorated

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
import com.developersam.primitivize.ast.type.ExprType
import com.developersam.primitivize.codegen.AstToCodeConverter
import com.developersam.primitivize.codegen.CodeConvertible
import com.developersam.primitivize.exceptions.IdentifierError
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

    /*
     * --------------------------------------------------------------------------------
     * Section 0: Higher Order Functions
     * --------------------------------------------------------------------------------
     */

    /**
     * Return a decorated expression where each expression inside it is mapped by [f].
     * This function is designed to reduce boilerplate.
     */
    protected abstract fun map(f: (DecoratedExpression) -> DecoratedExpression): DecoratedExpression

    /*
     * --------------------------------------------------------------------------------
     * Section 1: Functions to help inlining
     * --------------------------------------------------------------------------------
     */

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

    /*
     * --------------------------------------------------------------------------------
     * Section 2: Functions & Classes to help turning functions into if-else blocks.
     * --------------------------------------------------------------------------------
     */

    /**
     * Return the current expression with the form of either if else or the original expression,
     * which means it has no nested internal if-else structure.
     * This function should convert the expression AST as deep as possible.
     */
    protected open fun asIfElse(): DecoratedExpression = this

    /**
     * [toMainIfElseBlock] returns the main expression as a chain of if else block.
     */
    fun toMainIfElseBlock(): List<IfElseBlockItem> {
        val ifElseForm = asIfElse()
        if (ifElseForm !is IfElse) {
            return listOf(element = IfElseBlockItem(
                    condition = Literal.TRUE, action = ifElseForm
            ))
        }
        val condition = ifElseForm.condition
        val e1List = ifElseForm.e1.toMainIfElseBlock()
        val e2List = ifElseForm.e2.toMainIfElseBlock()
        val list = ArrayList<IfElseBlockItem>(e1List.size + e2List.size)
        for ((itemCondition, action) in e1List) {
            list.add(IfElseBlockItem(
                    condition = Binary.and(left = condition, right = itemCondition),
                    action = action
            ))
        }
        list.addAll(elements = e2List)
        return list
    }

    /*
     * --------------------------------------------------------------------------------
     * Section 3: Expression Simplification
     * --------------------------------------------------------------------------------
     */

    /**
     * [simplify] returns the simplified expression with compile time known knowledge.
     */
    internal open fun simplify(): DecoratedExpression = this

    /*
     * --------------------------------------------------------------------------------
     * Section 4: Precedence Determination
     * --------------------------------------------------------------------------------
     */

    /**
     * [hasLowerPrecedence] returns whether this expression has lower precedence than [parent].
     */
    fun hasLowerPrecedence(parent: DecoratedExpression): Boolean =
            if (this is Binary && parent is Binary) {
                op.precedenceLevel >= parent.op.precedenceLevel
            } else {
                precedenceLevel > parent.precedenceLevel
            }

    /*
     * --------------------------------------------------------------------------------
     * Section 5: Subclasses
     * --------------------------------------------------------------------------------
     */

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
                throw UnsupportedOperationException()

        /**
         * @see DecoratedExpression.asIfElse
         */
        override fun asIfElse(): DecoratedExpression = throw UnsupportedOperationException()

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

        companion object {
            /**
             * The false literal value.
             */
            val FALSE: Literal = Literal(value = false)
            /**
             * The true literal value.
             */
            val TRUE: Literal = Literal(value = true)
            /**
             * The zero literal value.
             */
            val ZERO: Literal = Literal(value = 0)
            /**
             * The one literal value.
             */
            val ONE: Literal = Literal(value = 1)
        }

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

        /**
         * @see DecoratedExpression.asIfElse
         */
        override fun asIfElse(): DecoratedExpression {
            val exprIfElse = expr.asIfElse()
            return if (exprIfElse is IfElse) {
                exprIfElse.copy(
                        e1 = Not(expr = exprIfElse.e1).asIfElse(),
                        e2 = Not(expr = exprIfElse.e2).asIfElse()
                )
            } else copy(expr = exprIfElse)
        }

        /**
         * @see DecoratedExpression.simplify
         */
        override fun simplify(): DecoratedExpression =
                if (expr !is Literal) this else {
                    if (expr.literal !is CommonLiteral.Bool) {
                        error(message = "Corrupted AST!")
                    }
                    if (expr.literal.value) Literal.TRUE else Literal.TRUE
                }

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

        /**
         * @see DecoratedExpression.asIfElse
         */
        override fun asIfElse(): DecoratedExpression {
            val leftIfElse = left.asIfElse()
            val rightIfElse = right.asIfElse()
            return if (leftIfElse is IfElse && rightIfElse is IfElse) {
                val e1Bool = leftIfElse.condition
                val e2Bool = rightIfElse.condition
                val e1e1 = leftIfElse.e1
                val e1e2 = leftIfElse.e2
                val e2e1 = rightIfElse.e1
                val e2e2 = rightIfElse.e2
                IfElse(
                        condition = Binary.and(left = e1Bool, right = e2Bool), type = type,
                        e1 = copy(left = e1e1, right = e2e1).asIfElse(),
                        e2 = IfElse(
                                condition = e1Bool, type = type,
                                e1 = copy(left = e1e1, right = e2e2).asIfElse(),
                                e2 = IfElse(
                                        condition = e2Bool, type = type,
                                        e1 = copy(left = e1e2, right = e2e1).asIfElse(),
                                        e2 = copy(left = e1e2, right = e2e2).asIfElse()
                                )
                        )
                )
            } else if (leftIfElse is IfElse) {
                leftIfElse.copy(
                        e1 = copy(left = leftIfElse.e1, right = rightIfElse).asIfElse(),
                        e2 = copy(left = leftIfElse.e2, right = rightIfElse).asIfElse()
                )
            } else if (rightIfElse is IfElse) {
                rightIfElse.copy(
                        e1 = copy(left = leftIfElse, right = rightIfElse.e1).asIfElse(),
                        e2 = copy(left = leftIfElse, right = rightIfElse.e2).asIfElse()
                )
            } else copy(left = leftIfElse, right = rightIfElse)
        }

        /**
         * @see DecoratedExpression.simplify
         */
        override fun simplify(): DecoratedExpression {
            val simpleLeft = left.simplify()
            val simpleRight = right.simplify()
            if (!(simpleLeft is Literal && simpleRight is Literal)) {
                return copy(left = simpleLeft, right = simpleRight)
            }
            val leftVal = simpleLeft.literal
            val rightVal = simpleRight.literal
            return if (leftVal is CommonLiteral.Int && rightVal is CommonLiteral.Int) {
                val leftInt = leftVal.value
                val rightInt = rightVal.value
                val commonLiteral = when (op) {
                    MUL -> CommonLiteral.Int(value = leftInt * rightInt)
                    DIV -> CommonLiteral.Int(value = leftInt / rightInt)
                    MOD -> CommonLiteral.Int(value = leftInt % rightInt)
                    PLUS -> CommonLiteral.Int(value = leftInt + rightInt)
                    MINUS -> CommonLiteral.Int(value = leftInt - rightInt)
                    LT -> CommonLiteral.Bool(value = leftInt < rightInt)
                    LE -> CommonLiteral.Bool(value = leftInt <= rightInt)
                    GT -> CommonLiteral.Bool(value = leftInt > rightInt)
                    GE -> CommonLiteral.Bool(value = leftInt >= rightInt)
                    EQ -> CommonLiteral.Bool(value = leftInt == rightInt)
                    NE -> CommonLiteral.Bool(value = leftInt != rightInt)
                    else -> error(message = "Corrupted AST!")
                }
                Literal(literal = commonLiteral, type = type)
            } else if (leftVal is CommonLiteral.Bool && rightVal is CommonLiteral.Bool) {
                val leftBool = leftVal.value
                val rightBool = rightVal.value
                if (op == AND) {
                    if (leftBool && rightBool) Literal.TRUE else Literal.FALSE
                } else if (op == OR) {
                    if (leftBool || rightBool) Literal.TRUE else Literal.FALSE
                } else {
                    error(message = "Corrupted AST!")
                }
            } else error(message = "Corrupted AST!")
        }

        companion object {

            /**
             * [and] returns an and expression for convenience.
             */
            fun and(left: DecoratedExpression, right: DecoratedExpression): Binary = Binary(
                    left = left, op = AND, right = right, type = ExprType.Bool
            )

        }

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

        /**
         * @see DecoratedExpression.asIfElse
         */
        override fun asIfElse(): DecoratedExpression = copy(e1 = e1.asIfElse(), e2 = e2.asIfElse())

        /**
         * @see DecoratedExpression.simplify
         */
        override fun simplify(): DecoratedExpression {
            val simpleCondition = condition.simplify()
            return if (simpleCondition is Literal) {
                val literal = simpleCondition.literal
                        as? CommonLiteral.Bool
                        ?: error(message = "Corrupted AST!")
                if (literal.value) e1.simplify() else e2.simplify()
            } else {
                copy(condition = condition, e1 = e1.simplify(), e2 = e2.simplify())
            }
        }

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

        /**
         * @see DecoratedExpression.asIfElse
         */
        override fun asIfElse(): DecoratedExpression {
            val exprIfElse = expr.asIfElse()
            return if (exprIfElse is IfElse) {
                exprIfElse.copy(
                        e1 = Assign(identifier = identifier, expr = exprIfElse.e1).asIfElse(),
                        e2 = Assign(identifier = identifier, expr = exprIfElse.e2).asIfElse()
                )
            } else copy(expr = exprIfElse)
        }

        /**
         * @see DecoratedExpression.simplify
         */
        override fun simplify(): DecoratedExpression = copy(expr = expr.simplify())

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

        /**
         * @see DecoratedExpression.asIfElse
         */
        override fun asIfElse(): DecoratedExpression {
            val e1IfElse = e1.asIfElse()
            val e2IfElse = e2.asIfElse()
            return if (e1IfElse is IfElse && e2IfElse is IfElse) {
                val e1Bool = e1IfElse.condition
                val e2Bool = e2IfElse.condition
                val e1e1 = e1IfElse.e1
                val e1e2 = e1IfElse.e2
                val e2e1 = e2IfElse.e1
                val e2e2 = e2IfElse.e2
                IfElse(
                        condition = Binary.and(left = e1Bool, right = e2Bool),
                        type = type,
                        e1 = Chain(e1 = e1e1, e2 = e2e1).asIfElse(),
                        e2 = IfElse(
                                condition = e1Bool, type = type,
                                e1 = Chain(e1 = e1e1, e2 = e2e2).asIfElse(),
                                e2 = IfElse(
                                        condition = e2Bool, type = type,
                                        e1 = Chain(e1 = e1e2, e2 = e2e1).asIfElse(),
                                        e2 = Chain(e1 = e1e2, e2 = e2e2).asIfElse()
                                )
                        )
                )
            } else if (e1IfElse is IfElse) {
                e1IfElse.copy(
                        e1 = Chain(e1 = e1IfElse.e1, e2 = e2IfElse).asIfElse(),
                        e2 = Chain(e1 = e1IfElse.e2, e2 = e2IfElse).asIfElse()
                )
            } else if (e2IfElse is IfElse) {
                e2IfElse.copy(
                        e1 = Chain(e1 = e1IfElse, e2 = e2IfElse.e1).asIfElse(),
                        e2 = Chain(e1 = e1IfElse, e2 = e2IfElse.e2).asIfElse()
                )
            } else copy(e1 = e1IfElse, e2 = e2IfElse)
        }

        /**
         * @see DecoratedExpression.simplify
         */
        override fun simplify(): DecoratedExpression = copy(e1 = e1.simplify(), e2 = e2.simplify())

    }

}
