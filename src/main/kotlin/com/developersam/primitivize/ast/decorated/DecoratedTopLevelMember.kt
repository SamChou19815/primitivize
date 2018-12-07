package com.developersam.primitivize.ast.decorated

import com.developersam.primitivize.ast.common.FunctionCategory
import com.developersam.primitivize.ast.type.ExprType
import com.developersam.primitivize.codegen.AstToCodeConverter
import com.developersam.primitivize.codegen.CodeConvertible
import com.developersam.primitivize.lowering.VariableRenamingService

/**
 * [DecoratedTopLevelMember] contains a set of top-level members with type decorations.
 */
sealed class DecoratedTopLevelMember {

    /**
     * Identifier of the member.
     */
    abstract val identifier: String
    /**
     * Expression/content of the member.
     */
    abstract val expr: DecoratedExpression
    /**
     * Type of the top-level member.
     */
    abstract val type: ExprType

    /**
     * [replaceVariable] replaces variables from [from] to [to] inside this member.
     */
    internal abstract fun replaceVariable(from: String, to: String): DecoratedTopLevelMember

    /**
     * [rename] returns a new member with variable renamed with the help of [service].
     */
    internal abstract fun rename(service: VariableRenamingService): DecoratedTopLevelMember

    /**
     * [Variable] represents a constant declaration of the form:
     * `var` [identifier] `=` [expr].
     * It has an additional [type] field.
     *
     * @property expr expression of the constant.
     */
    data class Variable(
            override val identifier: String, override val expr: DecoratedExpression
    ) : DecoratedTopLevelMember(), CodeConvertible {

        override val type: ExprType = ExprType.Int

        /**
         * @see CodeConvertible.acceptConversion
         */
        override fun acceptConversion(converter: AstToCodeConverter): Unit =
                converter.convert(node = this)

        /**
         * @see DecoratedTopLevelMember.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedTopLevelMember.Variable =
                Variable(
                        identifier = if (identifier == from) to else identifier,
                        expr = expr.replaceVariable(from = from, to = to)
                )

        /**
         * @see DecoratedTopLevelMember.rename
         */
        override fun rename(service: VariableRenamingService): DecoratedTopLevelMember.Variable =
                copy(identifier = service.nextVariableName)

    }

    /**
     * [Function] represents a function declaration of the form:
     * [recursiveHeader]? `fun` [identifier] () `:` [returnType] `=` [expr].
     * The function [category] defines its behavior during type checking, interpretation, and code
     * generation.
     * It has an additional [type] field.
     *
     * @property category category of the function.
     * @property recursiveHeader the recursive header of the form (depth, default value)
     * @property identifier the identifier for the function.
     * @property arguments a list of arguments.
     * @property returnType type of the return value.
     * @property expr expr part of the function.
     * @property type of the entire function.
     */
    data class Function(
            val category: FunctionCategory,
            val recursiveHeader: Pair<Int, DecoratedExpression>?,
            override val identifier: String, val arguments: List<Pair<String, ExprType>>,
            val returnType: ExprType, override val expr: DecoratedExpression
    ) : DecoratedTopLevelMember() {

        override val type: ExprType = ExprType.Function(
                argumentTypes = arguments.map { it.second },
                returnType = returnType
        )

        /**
         * @see DecoratedTopLevelMember.replaceVariable
         */
        override fun replaceVariable(from: String, to: String): DecoratedTopLevelMember.Function =
                copy(expr = expr.replaceVariable(from = from, to = to))

        /**
         * @see DecoratedTopLevelMember.rename
         */
        override fun rename(service: VariableRenamingService): DecoratedTopLevelMember.Function =
                this

        /**
         * Inline and expand itself when it's a recursive function, or just return itself if it's
         * not.
         */
        fun selfInline(): DecoratedTopLevelMember.Function {
            if (recursiveHeader == null) {
                return this
            }
            val (depth, defaultExpr) = recursiveHeader
            var currentExpr = expr
            repeat(times = depth) {
                currentExpr = currentExpr.inlineFunction(f=this)
            }
            currentExpr = currentExpr.replaceFunctionApplicationWithExpr(
                    f = this, expr = defaultExpr
            )
            return copy(recursiveHeader = null, expr = currentExpr)
        }

    }

}