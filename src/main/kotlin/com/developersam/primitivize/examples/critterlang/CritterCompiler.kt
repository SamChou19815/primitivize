package com.developersam.primitivize.examples.critterlang

import com.developersam.primitivize.ast.common.BinaryOperator
import com.developersam.primitivize.ast.common.Literal
import com.developersam.primitivize.ast.decorated.DecoratedExpression
import com.developersam.primitivize.ast.decorated.DecoratedTopLevelMember
import com.developersam.primitivize.ast.decorated.IfElseBlockItem
import com.developersam.primitivize.ast.processed.ProcessedProgram
import com.developersam.primitivize.ast.type.ExprType
import com.developersam.primitivize.codegen.AstToCodeConverter
import com.developersam.primitivize.codegen.CodeConvertible
import com.developersam.primitivize.codegen.IdtQueue
import com.developersam.primitivize.codegen.PrettyPrinter

/**
 * [CritterCompiler] is an example printer that takes the AST and compiles it to critter programs.
 */
class CritterCompiler private constructor() {

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
         * [String.toMem] returns the string variable in memory primitive format.
         */
        private fun String.toMem(): String = "mem[${substring(startIndex = 3).toInt() + 9}]"

        /**
         * [toFirstIfElseBlock] returns the first var assigning if-else block from a list of
         * variable declarations.
         */
        private fun List<DecoratedTopLevelMember.Variable>.toFirstIfElseBlock(): IfElseBlockItem {
            val condition = DecoratedExpression.Binary(
                    left = DecoratedExpression.VariableIdentifier(
                            // -1 hack
                            variable = "var-1", type = ExprType.Int
                    ),
                    op = BinaryOperator.EQ,
                    right = DecoratedExpression.Literal.ZERO,
                    type = ExprType.Bool
            )
            var action: DecoratedExpression = DecoratedExpression.Assign(
                    identifier = "var-1",
                    expr = DecoratedExpression.Literal.ONE
            )
            for (v in this) {
                action = DecoratedExpression.Chain(
                        e1 = action,
                        e2 = DecoratedExpression.Assign(identifier = v.identifier, expr = v.expr)
                )
            }
            return IfElseBlockItem(condition = condition, action = action)
        }

        /**
         * Convert the main if else block item.
         */
        private fun convert(node: IfElseBlockItem) {
            val conditionCode = node.condition.toOneLineCode()
            q.addLine(line = "$conditionCode -->")
            q.indentAndApply {
                node.action.acceptConversion(converter = this@Visitor)
                addLine(line = ";")
            }
        }

        /*
         * --------------------------------------------------------------------------------
         * Part 3: Converters
         * --------------------------------------------------------------------------------
         */

        override fun convert(node: ProcessedProgram) {
            node.apply {
                variables.toFirstIfElseBlock().let(block = ::convert)
                mainExpr.toMainIfElseBlock().forEach(action = ::convert)
            }
        }

        override fun convert(node: DecoratedTopLevelMember.Variable): Unit =
                q.addLine(line = "${node.identifier.toMem()} = ${node.expr.toOneLineCode()}")

        override fun convert(node: DecoratedExpression.Literal) {
            val literalString = when (node.literal) {
                is Literal.Bool -> if ((node.literal).value) "1 = 1" else "1 = 0"
                else -> node.literal.toString()
            }
            q.addLine(line = literalString)
        }

        override fun convert(node: DecoratedExpression.VariableIdentifier): Unit =
                q.addLine(line = node.variable.toMem())

        override fun convert(node: DecoratedExpression.Not): Unit =
                q.addLine(line = "!${node.expr.toOneLineCode(parent = node)}")

        override fun convert(node: DecoratedExpression.Binary) {
            val leftCode = node.left.toOneLineCode(parent = node)
            val rightCode = node.right.toOneLineCode(parent = node)
            val opCode = when (node.op.symbol) {
                "==" -> "="
                "&&" -> "and"
                "||" -> "or"
                "%" -> "mod"
                else -> node.op.symbol
            }
            q.addLine(line = "$leftCode $opCode $rightCode")
        }

        override fun convert(node: DecoratedExpression.IfElse) {
            val nodeString = PrettyPrinter.prettyPrint(node)
            throw UnsupportedOperationException(
                    "Only top level if-else blocks are supported!\nNode:\n$nodeString"
            )
        }

        override fun convert(node: DecoratedExpression.FunctionApplication) {
            val identifierString = when (node.identifier) {
                "waitFor" -> "wait" // Limitation of JVM
                "memsize", "defense", "offense", "energy", "size", "pass", "tag", "posture" ->
                    node.identifier.toUpperCase()
                else -> node.identifier
            }
            val argumentsString = node.arguments.takeIf { it.isNotEmpty() }?.joinToString(
                    separator = ", ", prefix = "[", postfix = "]"
            ) { it.toOneLineCode() } ?: ""
            val functionString = identifierString + argumentsString
            q.addLine(line = functionString)
        }

        override fun convert(node: DecoratedExpression.Assign): Unit =
                q.addLine(line = "${node.identifier.toMem()} := ${node.expr.toOneLineCode(node)}")

        override fun convert(node: DecoratedExpression.Chain) {
            node.apply {
                e1.acceptConversion(converter = this@Visitor)
                e2.acceptConversion(converter = this@Visitor)
            }
        }

    }

    companion object {

        /**
         * [toPrimitiveString] returns the [processedProgram] as a string of indented code.
         */
        fun toPrimitiveString(processedProgram: ProcessedProgram): String {
            return Visitor().apply { convert(node = processedProgram) }.q.toIndentedCode()
        }

    }

}
