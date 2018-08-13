package com.developersam.primitivize.parser

import com.developersam.primitivize.antlr.PLBaseVisitor
import com.developersam.primitivize.antlr.PLParser.ConstantDeclarationContext
import com.developersam.primitivize.antlr.PLParser.FunctionDeclarationContext
import com.developersam.primitivize.ast.raw.TopLevelMember

/**
 * [TopLevelMemberBuilder] tries to build the top level member AST node.
 */
internal object TopLevelMemberBuilder : PLBaseVisitor<TopLevelMember>() {

    override fun visitConstantDeclaration(ctx: ConstantDeclarationContext): TopLevelMember =
            TopLevelMember.Constant(
                    identifierLineNo = ctx.LowerIdentifier().symbol.line,
                    identifier = ctx.LowerIdentifier().text,
                    expr = ctx.expression().accept(ExprBuilder)
            )

    override fun visitFunctionDeclaration(ctx: FunctionDeclarationContext): TopLevelMember =
            TopLevelMember.Function(
                    identifierLineNo = ctx.LowerIdentifier().symbol.line,
                    identifier = ctx.LowerIdentifier().text,
                    arguments = ctx.argumentDeclarations().accept(ArgumentDeclarationsBuilder),
                    returnType = ctx.typeAnnotation().type().accept(TypeBuilder),
                    body = ctx.expression().accept(ExprBuilder)
            )

}
