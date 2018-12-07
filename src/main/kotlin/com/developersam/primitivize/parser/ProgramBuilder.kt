package com.developersam.primitivize.parser

import com.developersam.primitivize.antlr.PLBaseVisitor
import com.developersam.primitivize.antlr.PLParser.FunctionDeclarationContext
import com.developersam.primitivize.antlr.PLParser.ProgramContext
import com.developersam.primitivize.antlr.PLParser.VariableDeclarationContext
import com.developersam.primitivize.ast.common.FunctionCategory
import com.developersam.primitivize.ast.raw.RawProgram
import com.developersam.primitivize.ast.raw.TopLevelMember
import com.developersam.primitivize.ast.type.ExprType

/**
 * [ProgramBuilder] builds a [RawProgram].
 */
internal object ProgramBuilder : PLBaseVisitor<RawProgram>() {

    /**
     * Returns variable declaration from parse tree.
     */
    private fun getVariableDeclaration(ctx: VariableDeclarationContext): TopLevelMember.Variable =
            TopLevelMember.Variable(
                    identifierLineNo = ctx.LowerIdentifier().symbol.line,
                    identifier = ctx.LowerIdentifier().text,
                    expr = ctx.expression().accept(ExprBuilder)
            )

    /**
     * Returns function declaration from parse tree.
     */
    private fun getFunctionDeclaration(ctx: FunctionDeclarationContext): TopLevelMember.Function {
        val recursiveHeader = ctx.recursiveFunctionHeader()?.let {
            val depth = it.IntegerLiteral().symbol.text.toInt()
            val expr = it.expression().accept(ExprBuilder)
            depth to expr
        }
        val argsIdentifiers: List<String> = ctx
                .LowerIdentifier()
                .let { it.subList(fromIndex = 1, toIndex = it.size) }
                .map { it.text }
        val argsTypes: List<ExprType> = ctx.type()
                .let { it.subList(fromIndex = 0, toIndex = it.size - 1) }
                .map { it.accept(TypeBuilder) }
        val args = argsIdentifiers.zip(argsTypes)
        val returnType = ctx.type().let { it[it.size - 1] }.accept(TypeBuilder)
        return TopLevelMember.Function(
                recursiveHeader = recursiveHeader,
                identifierLineNo = ctx.start.line,
                identifier = ctx.LowerIdentifier(0).text,
                arguments = args, returnType = returnType,
                body = ctx.expression().accept(ExprBuilder)
        )
    }

    /**
     * Visit program.
     */
    override fun visitProgram(ctx: ProgramContext): RawProgram {
        val variables = ctx.variableDeclaration().map { getVariableDeclaration(it) }
        val functions = ArrayList<TopLevelMember.Function>()
        functions.addAll(elements = ctx.functionDeclaration().map { getFunctionDeclaration(it) })
        functions.add(element = TopLevelMember.Function(
                category = FunctionCategory.USER_DEFINED, identifierLineNo = ctx.FUN().symbol.line,
                identifier = "main", arguments = emptyList(), returnType = ExprType.Void,
                body = ctx.expression().accept(ExprBuilder)
        ))
        return RawProgram(variables = variables, functions = functions)
    }

}
