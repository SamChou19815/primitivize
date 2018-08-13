package com.developersam.primitivize.parser

import com.developersam.primitivize.antlr.PLBaseVisitor
import com.developersam.primitivize.ast.type.ExprType
import com.developersam.primitivize.antlr.PLParser.ArgumentDeclarationsContext as C

/**
 * [ArgumentDeclarationsBuilder] builds argument declarations into AST.
 */
internal object ArgumentDeclarationsBuilder : PLBaseVisitor<List<Pair<String, ExprType>>>() {

    /**
     * Visit ArgumentDeclarations.
     */
    override fun visitArgumentDeclarations(ctx: C): List<Pair<String, ExprType>> =
            ctx.annotatedVariable().map { c ->
                val text: String = c.LowerIdentifier().text
                val type = c.typeAnnotation().type().accept(TypeBuilder)
                text to type
            }

}
