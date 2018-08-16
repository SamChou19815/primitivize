package com.developersam.primitivize.ast.raw

import com.developersam.primitivize.ast.decorated.DecoratedProgram
import com.developersam.primitivize.ast.decorated.DecoratedTopLevelMember
import com.developersam.primitivize.ast.processed.ProcessedProgram
import com.developersam.primitivize.ast.raw.TopLevelMember.Companion.typeCheck
import com.developersam.primitivize.ast.type.ExprType
import com.developersam.primitivize.environment.TypeEnv
import com.developersam.primitivize.exceptions.CompileTimeError
import com.developersam.primitivize.exceptions.IdentifierError
import com.developersam.primitivize.runtime.RuntimeLibrary
import com.developersam.primitivize.runtime.withInjectedRuntime

/**
 * [RawProgram] represents the top-level not-type-checked program with a list of [variables] and
 * [functions].
 *
 * @property variables a list of top-level variables in a program.
 * @property functions a list of top-level functions in a program.
 */
data class RawProgram(
        val variables: List<TopLevelMember.Variable>,
        val functions: List<TopLevelMember.Function>
) {

    /**
     * [noNameShadowingValidation] validates that the members collection has no name shadowing by
     * checking whether there is a name conflict with a name in [set], which stores used type and
     * class names.
     *
     * @throws IdentifierError.ShadowedName if there is a detected shadowed name.
     */
    private fun noNameShadowingValidation() {
        val nameSet = hashSetOf<String>()
        for (member in variables) {
            val name = member.identifier
            if (!nameSet.add(element = name)) {
                throw IdentifierError.ShadowedName(
                        lineNo = member.identifierLineNo, shadowedName = name
                )
            }
        }
        for (member in functions) {
            val name = member.identifier
            if (!nameSet.add(element = name)) {
                throw IdentifierError.ShadowedName(
                        lineNo = member.identifierLineNo, shadowedName = name
                )
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
    private fun typeCheck(providedRuntimeLibrary: RuntimeLibrary?): DecoratedProgram {
        noNameShadowingValidation()
        var env = TypeEnv.empty<String, ExprType>().withInjectedRuntime(providedRuntimeLibrary)
        val decoratedVar = ArrayList<DecoratedTopLevelMember.Variable>(variables.size)
        val decoratedFun = ArrayList<DecoratedTopLevelMember.Function>(functions.size)
        for (variable in variables) {
            val (v, newEnv) = variable.typeCheck(env = env)
            env = newEnv
            decoratedVar.add(element = v as DecoratedTopLevelMember.Variable)
        }
        for (func in functions) {
            val (f, newEnv) = func.typeCheck(env = env)
            env = newEnv
            decoratedFun.add(element = f as DecoratedTopLevelMember.Function)
        }
        return DecoratedProgram(
                variables = decoratedVar, functions = decoratedFun,
                providedRuntimeLibrary = providedRuntimeLibrary
        )
    }

    /**
     * [processWith] returns the fully processed program with the optionally given
     * [providedRuntimeLibrary].
     */
    internal fun processWith(providedRuntimeLibrary: RuntimeLibrary? = null): ProcessedProgram =
            typeCheck(providedRuntimeLibrary = providedRuntimeLibrary).process()

}
