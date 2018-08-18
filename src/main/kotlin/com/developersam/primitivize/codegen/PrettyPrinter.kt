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

    override fun convert(node: DecoratedExpression.Literal): Unit =
            q.addLine(line = node.literal.toString())

    override fun convert(node: DecoratedExpression.VariableIdentifier): Unit =
            q.addLine(line = node.variable)

    override fun convert(node: DecoratedExpression.Not): Unit =
            q.addLine(line = "!${node.expr.toOneLineCode(parent = node)}")

    override fun convert(node: DecoratedExpression.Binary) {
        val leftCode = node.left.toOneLineCode(parent = node)
        val rightCode = node.right.toOneLineCode(parent = node)
        q.addLine(line = "$leftCode ${node.op.symbol} $rightCode")
    }

    override fun convert(node: DecoratedExpression.IfElse) {
        val items = node.toMainIfElseBlock()
        val first = items[0]
        q.addLine(line = "if ${first.condition.toOneLineCode(node)} then (")
        q.indentAndApply { first.action.acceptConversion(converter = this@PrettyPrinter) }
        val len = items.size
        for (i in 1 until (len - 1)) {
            val mid = items[i]
            q.addLine(line = ") else if ${mid.condition.toOneLineCode(node)} then (")
            q.indentAndApply { mid.action.acceptConversion(converter = this@PrettyPrinter) }
        }
        val last = items[len - 1]
        q.addLine(line = ") else (")
        q.indentAndApply { last.action.acceptConversion(converter = this@PrettyPrinter) }
        q.addLine(line = ")")
    }

    override fun convert(node: DecoratedExpression.FunctionApplication) {
        val arguments = node.arguments.joinToString(separator = ",") { it.toOneLineCode() }
        q.addLine(line = "${node.identifier}($arguments)")
    }

    override fun convert(node: DecoratedExpression.Assign): Unit =
            q.addLine(line = "${node.identifier} = ${node.expr.toOneLineCode(parent = node)}")

    override fun convert(node: DecoratedExpression.Chain) {
        node.apply {
            e1.acceptConversion(converter = this@PrettyPrinter)
            q.addLine(line = ";")
            e2.acceptConversion(converter = this@PrettyPrinter)
        }
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
