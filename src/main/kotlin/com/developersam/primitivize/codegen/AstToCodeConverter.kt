package com.developersam.primitivize.codegen

import com.developersam.primitivize.ast.decorated.DecoratedTopLevelMember
import com.developersam.primitivize.ast.decorated.DecoratedExpression
import com.developersam.primitivize.ast.decorated.DecoratedProgram

/**
 * [AstToCodeConverter] defines a set of methods that helps the conversion from AST to target code.
 * This interface is designed to be target-code independent, so it can be used both for pretty
 * print and compilation.
 */
interface AstToCodeConverter {

    /**
     * [convert] converts the given [node] to target code by recording well-indented code info.
     */
    fun convert(node: DecoratedProgram)

    /**
     * [convert] converts the given [node] to target code by recording well-indented code info.
     */
    fun convert(node: DecoratedTopLevelMember.Variable)

    /**
     * [convert] converts the given [node] to target code by recording well-indented code info.
     */
    fun convert(node: DecoratedTopLevelMember.Function)

    /**
     * [convert] converts the given [node] to target code by recording well-indented code info.
     */
    fun convert(node: DecoratedExpression.Literal)

    /**
     * [convert] converts the given [node] to target code by recording well-indented code info.
     */
    fun convert(node: DecoratedExpression.VariableIdentifier)

    /**
     * [convert] converts the given [node] to target code by recording well-indented code info.
     */
    fun convert(node: DecoratedExpression.Not)

    /**
     * [convert] converts the given [node] to target code by recording well-indented code info.
     */
    fun convert(node: DecoratedExpression.Binary)

    /**
     * [convert] converts the given [node] to target code by recording well-indented code info.
     */
    fun convert(node: DecoratedExpression.IfElse)

    /**
     * [convert] converts the given [node] to target code by recording well-indented code info.
     */
    fun convert(node: DecoratedExpression.FunctionApplication)

    /**
     * [convert] converts the given [node] to target code by recording well-indented code info.
     */
    fun convert(node: DecoratedExpression.Assign)

    /**
     * [convert] converts the given [node] to target code by recording well-indented code info.
     */
    fun convert(node: DecoratedExpression.Chain)

}
