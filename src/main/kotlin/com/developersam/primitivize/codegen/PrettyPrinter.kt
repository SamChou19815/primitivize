package com.developersam.primitivize.codegen

import com.developersam.primitivize.ast.decorated.DecoratedExpression
import com.developersam.primitivize.ast.decorated.DecoratedTopLevelMember
import com.developersam.primitivize.ast.processed.ProcessedProgram

/**
 * [PrettyPrinter] is responsible for pretty printing a program node.
 */
internal class PrettyPrinter private constructor() : AstToCodeConverter {

    /**
     * [q] is the only indentation queue used in this class.
     */
    private val q: IdtQueue = IdtQueue()

    /**
     * [CodeConvertible.toOneLineCode] returns the one-liner form of the [CodeConvertible].
     */
    private fun CodeConvertible.toOneLineCode(): String =
            PrettyPrinter().apply { acceptConversion(converter = this) }.q.toOneLineCode()

    /**
     * [DecoratedExpression.toOneLineCode] returns the one-liner form of [DecoratedExpression].
     *
     * This method is expression node specific. It will consider the precedence between this node
     * and its [parent] to decide whether to add parenthesis.
     */
    private fun DecoratedExpression.toOneLineCode(parent: DecoratedExpression): String =
            toOneLineCode().let { code ->
                if (hasLowerPrecedence(parent = parent)) "($code)" else code
            }

    override fun convert(node: ProcessedProgram) {
        node.variables.forEach(action = ::convert)
        q.addLine(line = "// Main Expression:")
        node.mainExpr.acceptConversion(converter = this)
    }

    override fun convert(node: DecoratedTopLevelMember.Variable) {
        val header = StringBuilder().append("var ").append(node.identifier).append(" =").toString()
        q.addLine(line = header)
        q.indentAndApply { node.expr.acceptConversion(converter = this@PrettyPrinter) }
    }

    override fun convert(node: DecoratedExpression.Literal) {
        q.addLine(line = node.literal.toString())
    }

    override fun convert(node: DecoratedExpression.VariableIdentifier) {
        q.addLine(line = node.variable)
    }

    override fun convert(node: DecoratedExpression.Not) {
        val exprCode = node.expr.toOneLineCode(parent = node)
        q.addLine(line = "!$exprCode")
    }

    override fun convert(node: DecoratedExpression.Binary) {
        val leftCode = node.left.toOneLineCode(parent = node)
        val rightCode = node.right.toOneLineCode(parent = node)
        q.addLine(line = "$leftCode ${node.op.symbol} $rightCode")
    }

    override fun convert(node: DecoratedExpression.IfElse) {
        q.addLine(line = "if (${node.condition.toOneLineCode()}) then (")
        q.indentAndApply { node.e1.acceptConversion(converter = this@PrettyPrinter) }
        q.addLine(line = ") else (")
        q.indentAndApply { node.e2.acceptConversion(converter = this@PrettyPrinter) }
        q.addLine(line = ")")
    }

    override fun convert(node: DecoratedExpression.FunctionApplication) {
        q.addLine(line = "${node.identifier}()")
    }

    override fun convert(node: DecoratedExpression.Assign) {
        val code = node.expr.toOneLineCode(parent = node)
        val letLine = "${node.identifier} = $code"
        q.addLine(line = letLine)
    }

    override fun convert(node: DecoratedExpression.Chain) {
        val c1 = node.e1.toOneLineCode(parent = node)
        val c2 = node.e2.toOneLineCode(parent = node)
        q.addLine(line = "$c1; $c2")
    }

    companion object {

        /**
         * [prettyPrint] returns the given [node] as well-formatted code in string.
         */
        @JvmStatic
        fun prettyPrint(node: CodeConvertible): String =
                PrettyPrinter().apply { node.acceptConversion(converter = this) }.q.toIndentedCode()

    }

}
