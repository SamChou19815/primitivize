package com.developersam.primitivize.lowering

import com.developersam.primitivize.ast.decorated.DecoratedExpression
import com.developersam.primitivize.ast.decorated.DecoratedProgram
import com.developersam.primitivize.ast.decorated.DecoratedTopLevelMember

/**
 * [AstLoweringVisitor] is responsible for lowering AST by visiting them.
 */
interface AstLoweringVisitor {

    /**
     * [convert] converts the given [node] to lowered node.
     */
    fun convert(node: DecoratedProgram): DecoratedProgram

    /**
     * [convert] converts the given [node] to lowered node.
     */
    fun convert(node: DecoratedTopLevelMember.Constant): DecoratedTopLevelMember

    /**
     * [convert] converts the given [node] to lowered node.
     */
    fun convert(node: DecoratedTopLevelMember.Function): DecoratedTopLevelMember

    /**
     * [convert] converts the given [node] to lowered node.
     */
    fun convert(node: DecoratedExpression.Literal): DecoratedExpression

    /**
     * [convert] converts the given [node] to lowered node.
     */
    fun convert(node: DecoratedExpression.VariableIdentifier): DecoratedExpression

    /**
     * [convert] converts the given [node] to lowered node.
     */
    fun convert(node: DecoratedExpression.Not): DecoratedExpression

    /**
     * [convert] converts the given [node] to lowered node.
     */
    fun convert(node: DecoratedExpression.Binary): DecoratedExpression

    /**
     * [convert] converts the given [node] to lowered node.
     */
    fun convert(node: DecoratedExpression.IfElse): DecoratedExpression

    /**
     * [convert] converts the given [node] to lowered node.
     */
    fun convert(node: DecoratedExpression.FunctionApplication): DecoratedExpression

    /**
     * [convert] converts the given [node] to lowered node.
     */
    fun convert(node: DecoratedExpression.Let): DecoratedExpression

}
