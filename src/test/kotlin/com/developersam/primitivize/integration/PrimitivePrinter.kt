package com.developersam.primitivize.integration

import com.developersam.primitivize.ast.common.Literal
import com.developersam.primitivize.ast.decorated.DecoratedExpression
import com.developersam.primitivize.ast.decorated.DecoratedTopLevelMember
import com.developersam.primitivize.ast.processed.ProcessedProgram
import com.developersam.primitivize.ast.type.ExprType
import com.developersam.primitivize.codegen.AstToCodeConverter
import com.developersam.primitivize.codegen.CodeConvertible
import com.developersam.primitivize.codegen.IdtQueue

/**
 * [PrimitivePrinter] is an example printer that takes the AST and prints some elementary messages.
 */
class PrimitivePrinter private constructor() {

    /**
     * [Visitor] is used to visit the AST and populate [q].
     */
    private class Visitor : AstToCodeConverter {

        /*
         * --------------------------------------------------------------------------------
         * Part 1: Instance Variables
         * --------------------------------------------------------------------------------
         */

        /**
         * [q] is the only indentation queue used in this class.
         */
        val q: IdtQueue = IdtQueue()

        /*
         * --------------------------------------------------------------------------------
         * Part 2: Helper Methods
         * --------------------------------------------------------------------------------
         */

        /**
         * [CodeConvertible.toOneLineCode] returns the one-liner form of the [CodeConvertible].
         */
        private fun CodeConvertible.toOneLineCode(): String =
                Visitor().apply { acceptConversion(converter = this) }.q.toOneLineCode()

        /**
         * [DecoratedExpression.toOneLineCode] returns the one-liner form of [DecoratedExpression].
         *
         * This method is expression node specific. It will consider the precedence between this
         * node [parent] to decide whether to add parenthesis.
         */
        private fun DecoratedExpression.toOneLineCode(parent: DecoratedExpression): String =
                toOneLineCode().let { code ->
                    if (!hasLowerPrecedence(parent = parent)) code else {
                        if (type == ExprType.Int) "($code)" else "{$code}"
                    }
                }

        /**
         * The if-else block item with a [condition] and [action].
         */
        private data class IfElseBlockItem(
                val condition: DecoratedExpression,
                val action: DecoratedExpression
        )

        /**
         * Returns the main expression as a chain of if else block.
         */
        private fun DecoratedExpression.toMainIfElseBlock(): List<IfElseBlockItem> {
            val list = arrayListOf<IfElseBlockItem>()
            var expr = this
            while (true) {
                val e = expr
                if (e is DecoratedExpression.IfElse) {
                    val item = IfElseBlockItem(condition = e.condition, action = e.e1)
                    list.add(element = item)
                    expr = e.e2
                } else {
                    val item = IfElseBlockItem(
                            condition = DecoratedExpression.Literal(
                                    literal = Literal.Bool(value = true), type = ExprType.Bool)
                            , action = expr
                    )
                    list.add(element = item)
                    break
                }
            }
            return list
        }

        /**
         * Convert the main if else block item.
         */
        private fun convert(node: IfElseBlockItem) {
            val conditionCode = node.condition.toOneLineCode()
            q.addLine(line = "$conditionCode -->")
            q.indentAndApply { node.action.acceptConversion(converter = this@Visitor) }
        }

        /*
         * --------------------------------------------------------------------------------
         * Part 3: Converters
         * --------------------------------------------------------------------------------
         */

        override fun convert(node: ProcessedProgram) {
            // TODO process variables
            node.mainExpr.toMainIfElseBlock().forEach(action = ::convert)
        }

        override fun convert(node: DecoratedTopLevelMember.Variable) {
            val variableId = node.identifier.substring(startIndex = 4).toInt()
            q.addLine(line = "mem[${variableId + 9}] = ${node.expr.toOneLineCode()}")
        }

        override fun convert(node: DecoratedExpression.Literal) {
            val literalString = when (node.literal) {
                is Literal.Bool -> if ((node.literal as Literal.Bool).value) "1 = 1" else "1 = 0"
                else -> node.literal.toString()
            }
            q.addLine(line = literalString)
        }

        override fun convert(node: DecoratedExpression.VariableIdentifier) {
            val variableId = node.variable.substring(startIndex = 4).toInt()
            q.addLine(line = "mem[${variableId + 8}]")
        }

        override fun convert(node: DecoratedExpression.Not): Unit =
                q.addLine(line = "!${node.expr.toOneLineCode(parent = node)}")

        override fun convert(node: DecoratedExpression.Binary) {
            val leftCode = node.left.toOneLineCode(parent = node)
            val rightCode = node.right.toOneLineCode(parent = node)
            q.addLine(line = "$leftCode ${node.op.symbol} $rightCode")
        }

        override fun convert(node: DecoratedExpression.IfElse) {
            throw UnsupportedOperationException("Only top level if-else blocks are supported!")
        }

        override fun convert(node: DecoratedExpression.FunctionApplication) {
            val functionString = "FooBar"
            q.addLine(line = functionString)
        }

        override fun convert(node: DecoratedExpression.Assign) {
            val variableId = node.identifier.substring(startIndex = 4).toInt()
            val exprCode = node.expr.toOneLineCode(parent = node)
            q.addLine(line = "mem[${variableId + 9}] = $exprCode")
        }

        override fun convert(node: DecoratedExpression.Chain): Unit =
                q.addLine(line = "${node.e1.toOneLineCode(node)}; ${node.e2.toOneLineCode(node)}")

    }

    companion object {

        /**
         * [toPrimitiveString] returns the [processedProgram] as a string of indented code.
         */
        fun toPrimitiveString(processedProgram: ProcessedProgram): String =
                Visitor().apply { convert(node = processedProgram) }.q.toIndentedCode()

    }

}
