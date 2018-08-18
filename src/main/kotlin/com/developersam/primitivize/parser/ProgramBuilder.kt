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
    private fun getFunctionDeclaration(ctx: FunctionDeclarationContext): TopLevelMember.Function =
            TopLevelMember.Function(
                    identifierLineNo = ctx.LowerIdentifier().symbol.line,
                    identifier = ctx.LowerIdentifier().text,
                    returnType = ctx.type().accept(TypeBuilder),
                    body = ctx.expression().accept(ExprBuilder)
            )

    /**
     * Visit program.
     */
    override fun visitProgram(ctx: ProgramContext): RawProgram {
        val variables = ctx.variableDeclaration().map { getVariableDeclaration(it) }
        val functions = ArrayList<TopLevelMember.Function>()
        functions.addAll(elements = ctx.functionDeclaration().map { getFunctionDeclaration(it) })
        functions.add(element = TopLevelMember.Function(
                category = FunctionCategory.USER_DEFINED, identifierLineNo = ctx.FUN().symbol.line,
                identifier = "main", returnType = ExprType.Void,
                body = ctx.expression().accept(ExprBuilder)
        ))
        return RawProgram(variables = variables, functions = functions)
    }

}
