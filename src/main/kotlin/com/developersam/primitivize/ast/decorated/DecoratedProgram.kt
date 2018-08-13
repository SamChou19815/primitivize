package com.developersam.primitivize.ast.decorated

import com.developersam.primitivize.codegen.AstToCodeConverter
import com.developersam.primitivize.codegen.CodeConvertible
import com.developersam.primitivize.lowering.VariableRenamingService
import com.developersam.primitivize.runtime.RuntimeLibrary

/**
 * [DecoratedProgram] node contains a set of members [members].
 * It contains decorated ASTs.
 *
 * @property members a list of class members.
 * @property providedRuntimeLibrary the provided library for execution at runtime.
 */
data class DecoratedProgram(
        val members: List<DecoratedTopLevelMember>, val providedRuntimeLibrary: RuntimeLibrary?
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
        val newMembers = ArrayList<DecoratedTopLevelMember>(members)
        val service = VariableRenamingService()
        val l = newMembers.size
        for (i in 0 until l) {
            val m = newMembers[i]
            val oldName = m.identifier
            val newMember = m.rename(service = service)
            if (newMember is DecoratedTopLevelMember.Constant) {
                val newName = newMember.identifier
                for (j in (i + 1) until l) {
                    newMembers[j] = newMembers[j].replaceVariable(from = oldName, to = newName)
                }
            }
            newMembers[i] = newMember
        }
        return copy(members = newMembers)
    }

}
