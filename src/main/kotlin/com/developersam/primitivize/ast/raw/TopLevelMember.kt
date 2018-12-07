package com.developersam.primitivize.ast.raw

import com.developersam.primitivize.ast.common.FunctionCategory
import com.developersam.primitivize.ast.decorated.DecoratedExpression
import com.developersam.primitivize.ast.decorated.DecoratedTopLevelMember
import com.developersam.primitivize.ast.type.ExprType
import com.developersam.primitivize.environment.TypeEnv
import com.developersam.primitivize.exceptions.UnexpectedTypeError

/**
 * [TopLevelMember] defines a set of supported top level members.
 */
sealed class TopLevelMember {

    /**
     * [typeCheck] tries to type check this class member under the given [TypeEnv] [env].
     * It returns a decorated class member and a new environment after type check.
     */
    internal abstract fun typeCheck(env: TypeEnv): Pair<DecoratedTopLevelMember, TypeEnv>

    /**
     * [Variable] represents a constant declaration of the form:
     * `var` [identifier] `=` [expr],
     * with [identifier] at [identifierLineNo].
     *
     * @property identifierLineNo identifier line number of the constant.
     * @property identifier identifier of the constant.
     * @property expr expression of the constant.
     */
    data class Variable(
            val identifierLineNo: Int,
            val identifier: String,
            val expr: Expression
    ) : TopLevelMember() {

        override fun typeCheck(env: TypeEnv): Pair<DecoratedTopLevelMember, TypeEnv> {
            val decoratedExpr = expr.typeCheck(environment = env)
            UnexpectedTypeError.check(
                    lineNo = identifierLineNo,
                    expectedType = ExprType.Int,
                    actualType = decoratedExpr.type
            )
            val decoratedConstant = DecoratedTopLevelMember.Variable(
                    identifier = identifier, expr = decoratedExpr
            )
            val e = env.put(key = identifier, value = decoratedExpr.type)
            return decoratedConstant to e
        }

    }

    /**
     * [Function] represents a function declaration of the form:
     * [recursiveHeader]? `fun` [identifier] () `:` [returnType] `=` [body],
     * with [identifier] at [identifierLineNo].
     * The function [category] defines its behavior during type checking, interpretation, and code
     * generation.
     *
     * @property category category of the function.
     * @property recursiveHeader the recursive header of the form (depth, default value)
     * @property identifierLineNo the line number of the identifier for the function.
     * @property identifier the identifier for the function.
     * @property arguments a list of arguments.
     * @property returnType type of the return value.
     * @property body expr part of the function.
     */
    data class Function(
            val category: FunctionCategory = FunctionCategory.USER_DEFINED,
            val recursiveHeader: Pair<Int, Expression>? = null,
            val identifierLineNo: Int, val identifier: String,
            val arguments: List<Pair<String, ExprType>>, val returnType: ExprType,
            val body: Expression
    ) : TopLevelMember() {

        override fun typeCheck(env: TypeEnv): Pair<DecoratedTopLevelMember, TypeEnv> {
            val decoratedRecursiveHeader = recursiveHeader?.let { (depth, defaultValueExpr) ->
                val defaultValueDecoratedExpr = defaultValueExpr.typeCheck(environment = env)
                UnexpectedTypeError.check(
                        lineNo = identifierLineNo,
                        expectedType = returnType,
                        actualType = defaultValueDecoratedExpr.type
                )
                depth to defaultValueDecoratedExpr
            }
            val newEnv = arguments.fold(initial = env) { e, (name, type) ->
                e.put(key = name, value = type)
            }.let { e ->
                if (decoratedRecursiveHeader == null) e else {
                    e.put(key = identifier, value = ExprType.Function(
                            argumentTypes = arguments.map { it.second },
                            returnType = returnType
                    ))
                }
            }
            val bodyExpr: DecoratedExpression = when (category) {
                FunctionCategory.PROVIDED -> DecoratedExpression.Dummy // Don't check given ones
                FunctionCategory.USER_DEFINED -> {
                    val e = body.typeCheck(environment = newEnv)
                    val bodyType = e.type
                    UnexpectedTypeError.check(
                            lineNo = identifierLineNo,
                            expectedType = returnType,
                            actualType = bodyType
                    )
                    e
                }
            }
            val decoratedFunction = DecoratedTopLevelMember.Function(
                    category = category, recursiveHeader = decoratedRecursiveHeader,
                    identifier = identifier, arguments = arguments,
                    returnType = returnType, expr = bodyExpr
            )
            val e = newEnv.put(key = identifier, value = decoratedFunction.type)
            return decoratedFunction to e
        }

    }

    internal companion object {

        /**
         * [typeCheck] type checks all the members with current [env].
         */
        fun <M : TopLevelMember> List<M>.typeCheck(
                env: TypeEnv
        ): Pair<List<DecoratedTopLevelMember>, TypeEnv> {
            val typeCheckedMembers = ArrayList<DecoratedTopLevelMember>(size)
            val newEnv = fold(initial = env) { e, member ->
                val (typeCheckedMember, newE) = member.typeCheck(env = e)
                typeCheckedMembers.add(element = typeCheckedMember)
                newE
            }
            return typeCheckedMembers to newEnv
        }

    }

}

