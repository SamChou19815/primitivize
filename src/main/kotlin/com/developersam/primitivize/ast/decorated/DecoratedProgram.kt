package com.developersam.primitivize.ast.decorated

import com.developersam.primitivize.ast.processed.ProcessedProgram
import com.developersam.primitivize.lowering.VariableRenamingService
import com.developersam.primitivize.runtime.RuntimeLibrary
import java.util.LinkedList

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
) {

    /**
     * [rename] returns the program with variables renamed.
     */
    private fun rename(): DecoratedProgram {
        val l = variables.size + functions.size
        val newMembers = ArrayList<DecoratedTopLevelMember>(l)
        newMembers.addAll(elements = variables)
        newMembers.addAll(elements = functions)
        val service = VariableRenamingService()
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
                is DecoratedTopLevelMember.Variable ->
                    newVariables.add(element = member)
                is DecoratedTopLevelMember.Function ->
                    newFunctions.add(element = member.selfInline())
            }
        }
        return DecoratedProgram(
                variables = newVariables, functions = newFunctions,
                providedRuntimeLibrary = providedRuntimeLibrary
        )
    }

    /**
     * [inline] returns the whole program as an inlined expression.
     */
    private fun inline(): ProcessedProgram {
        var exprAcc = functions[functions.size - 1].expr
        // Don't start from functions.size - 2 due to main function
        for (i in (functions.size - 2) downTo 0) {
            exprAcc = exprAcc.inlineFunction(f = functions[i])
        }
        return ProcessedProgram(
                variables = variables, mainExpr = exprAcc,
                providedRuntimeLibrary = providedRuntimeLibrary
        )
    }

    /**
     * [normalizeVariableName] returns the program that removes the underscore to normalize
     * variables.
     */
    private fun ProcessedProgram.normalizeVariableName(): ProcessedProgram {
        val replacementList = LinkedList<Pair<String, String>>()
        for (v in variables) {
            val id = v.identifier
            val newId = id.substring(1)
            replacementList.add(element = id to newId)
        }
        val newVariables = variables.map { v ->
            replacementList.fold(initial = v) { variable, (from, to) ->
                variable.replaceVariable(from, to)
            }
        }
        val newExpr = replacementList.fold(initial = mainExpr) { e, (from, to) ->
            e.replaceVariable(from, to)
        }
        return copy(variables = newVariables, mainExpr = newExpr)
    }

    /**
     * [process] returns the fully processed program.
     */
    internal fun process(): ProcessedProgram = rename().inline().normalizeVariableName()

}
