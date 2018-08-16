package com.developersam.primitivize.integration

import com.developersam.primitivize.ast.decorated.DecoratedExpression
import com.developersam.primitivize.ast.decorated.DecoratedProgram
import com.developersam.primitivize.ast.decorated.DecoratedTopLevelMember
import com.developersam.primitivize.ast.processed.ProcessedProgram
import com.developersam.primitivize.codegen.AstToCodeConverter
import com.developersam.primitivize.codegen.IdtQueue

/**
 * [PrimitivePrinter] is an example printer that takes the AST and prints some elementary messages.
 *
 * @property program the processed program to print.
 */
class PrimitivePrinter(private val program: ProcessedProgram) {

    /**
     * [q] is the only indentation queue used in this class.
     */
    private val q: IdtQueue = IdtQueue()

    /**
     * [Visitor] is used to visit the AST and populate [q].
     */
    private inner class Visitor : AstToCodeConverter {

        override fun convert(node: DecoratedProgram) {
            throw UnsupportedOperationException()
        }

        override fun convert(node: DecoratedTopLevelMember.Variable) {
            val variableId = node.identifier.substring(startIndex = 4).toInt()
            q.addLine(line = "mem[${variableId + 9}] = ")
        }

        override fun convert(node: DecoratedTopLevelMember.Function) {
            throw UnsupportedOperationException()
        }

        override fun convert(node: DecoratedExpression.Literal) {
            q.addLine(line = node.literal.toString())
        }

        override fun convert(node: DecoratedExpression.VariableIdentifier) {
            val variableId = node.variable.substring(startIndex = 4).toInt()
            q.addLine(line = "mem[${variableId + 8}]")
        }

        override fun convert(node: DecoratedExpression.Not) {
            TODO("not implemented")
        }

        override fun convert(node: DecoratedExpression.Binary) {
            TODO("not implemented")
        }

        override fun convert(node: DecoratedExpression.IfElse) {
            TODO("not implemented")
        }

        override fun convert(node: DecoratedExpression.FunctionApplication) {
            TODO("not implemented")
        }

        override fun convert(node: DecoratedExpression.Assign) {
            TODO("not implemented")
        }

        override fun convert(node: DecoratedExpression.Chain) {
            TODO("not implemented")
        }

    }

}
