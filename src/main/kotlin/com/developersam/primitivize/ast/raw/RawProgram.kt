package com.developersam.primitivize.ast.raw

import com.developersam.primitivize.ast.decorated.DecoratedProgram
import com.developersam.primitivize.ast.raw.TopLevelMember.Companion.typeCheck
import com.developersam.primitivize.environment.TypeEnv
import com.developersam.primitivize.exceptions.CompileTimeError
import com.developersam.primitivize.exceptions.IdentifierError
import com.developersam.primitivize.runtime.RuntimeLibrary
import com.developersam.primitivize.runtime.withInjectedRuntime

/**
 * [RawProgram] represents the top-level not-type-checked program with a list of [members].
 *
 * @property members a list of top-level members is a program.
 */
data class RawProgram(val members: List<TopLevelMember>) {

    /**
     * [noNameShadowingValidation] validates that the members collection has no name shadowing by
     * checking whether there is a name conflict with a name in [set], which stores used type and
     * class names.
     *
     * @return [Unit]
     * @throws IdentifierError.ShadowedName if there is a detected shadowed name.
     */
    private fun List<TopLevelMember>.noNameShadowingValidation() {
        val nameSet = hashSetOf<String>()
        for (member in this) {
            when (member) {
                is TopLevelMember.Constant -> {
                    val name = member.identifier
                    if (!nameSet.add(element = name)) {
                        throw IdentifierError.ShadowedName(
                                lineNo = member.identifierLineNo, shadowedName = name
                        )
                    }
                }
                is TopLevelMember.Function -> {
                    val name = member.identifier
                    if (!nameSet.add(element = name)) {
                        throw IdentifierError.ShadowedName(
                                lineNo = member.identifierLineNo, shadowedName = name
                        )
                    }
                }
            }
        }
    }

    /**
     * [typeCheck] tries to type check this top-level program with an optional
     * [providedRuntimeLibrary] as the type checking context.
     * If it does not type check, it will throw an [CompileTimeError]
     *
     * @return the decorated program after type check.
     */
    internal fun typeCheck(providedRuntimeLibrary: RuntimeLibrary? = null): DecoratedProgram {
        members.noNameShadowingValidation()
        return DecoratedProgram(
                members = members.withInjectedRuntime(providedRuntimeLibrary)
                        .typeCheck(env = TypeEnv.empty()).first,
                providedRuntimeLibrary = providedRuntimeLibrary
        )
    }

}
