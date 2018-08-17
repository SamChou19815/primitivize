package com.developersam.primitivize.ast.processed

import com.developersam.primitivize.ast.decorated.DecoratedExpression
import com.developersam.primitivize.ast.decorated.DecoratedTopLevelMember
import com.developersam.primitivize.codegen.AstToCodeConverter
import com.developersam.primitivize.codegen.CodeConvertible
import com.developersam.primitivize.runtime.RuntimeLibrary

/**
 * [ProcessedProgram] represents the program that has been fully processed and reduced to primitive
 * one.
 *
 * @property variables a list of top-level variables in a program.
 * @property mainExpr the processed main program.
 * @property providedRuntimeLibrary the provided library for execution at runtime.
 */
data class ProcessedProgram(
        val variables: List<DecoratedTopLevelMember.Variable>,
        val mainExpr: DecoratedExpression,
        val providedRuntimeLibrary: RuntimeLibrary?
) : CodeConvertible {

    /**
     * @see CodeConvertible.acceptConversion
     */
    override fun acceptConversion(converter: AstToCodeConverter) : Unit =
            converter.convert(node = this)

}
