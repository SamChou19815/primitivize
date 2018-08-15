package com.developersam.primitivize.ast.decorated

import com.developersam.primitivize.codegen.AstToCodeConverter
import com.developersam.primitivize.codegen.CodeConvertible
import com.developersam.primitivize.lowering.VariableRenamingService
import com.developersam.primitivize.runtime.RuntimeLibrary

/**
 * [DecoratedProgram] node contains a set of members [variables] and [functions].
 * It contains decorated ASTs.
 *
 * @property variables a list of top-level variables in a program.
 * @property functions a list of top-level functions in a program.
 * @property providedRuntimeLibrary the provided library for execution at runtime.
 */
data class DecoratedProgram(
        val variables: List<DecoratedTopLevelMember.Variable>,
        val functions: List<DecoratedTopLevelMember.Function>,
        val providedRuntimeLibrary: RuntimeLibrary?
) : CodeConvertible {

    /**
     * @see CodeConvertible.acceptConversion
     */
    override fun acceptConversion(converter: AstToCodeConverter): Unit =
            converter.convert(node = this)

    /**
     * [rename] returns the program with variables renamed.
     */
    internal fun rename(): DecoratedProgram {
        val newMembers = ArrayList<DecoratedTopLevelMember>(variables.size + functions.size)
        newMembers.addAll(elements = variables)
        newMembers.addAll(elements = functions)
        val service = VariableRenamingService()
        val l = newMembers.size
        for (i in 0 until l) {
            val m = newMembers[i]
            val oldName = m.identifier
            val newMember = m.rename(service = service)
            if (newMember is DecoratedTopLevelMember.Variable) {
                val newName = newMember.identifier
                for (j in (i + 1) until l) {
                    newMembers[j] = newMembers[j].replaceVariable(from = oldName, to = newName)
                }
            }
            newMembers[i] = newMember
        }
        val newVariables = ArrayList<DecoratedTopLevelMember.Variable>(variables.size)
        val newFunctions = ArrayList<DecoratedTopLevelMember.Function>(functions.size)
        newMembers.forEach { member ->
            when (member) {
                is DecoratedTopLevelMember.Variable -> newVariables.add(element = member)
                is DecoratedTopLevelMember.Function -> newFunctions.add(element = member)
            }
        }
        return DecoratedProgram(
                variables = newVariables, functions = newFunctions,
                providedRuntimeLibrary = providedRuntimeLibrary
        )
    }

}
