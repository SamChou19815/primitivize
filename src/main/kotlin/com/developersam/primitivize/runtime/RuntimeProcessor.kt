package com.developersam.primitivize.runtime

import com.developersam.primitivize.ast.type.ExprType
import com.developersam.primitivize.environment.TypeEnv
import com.developersam.primitivize.exceptions.DisallowedRuntimeFunctionError
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import com.developersam.primitivize.runtime.RuntimeLibrary as R

/**
 * [toAllowedTypeExpr] returns the equivalent allowed type expression in this
 * programming language for this class.
 * If there is no such correspondence, `null` will be returned.
 */
private fun Type.toAllowedTypeExpr(): ExprType? = when (typeName) {
    "void" -> ExprType.Void
    "int" -> ExprType.Int
    "boolean" -> ExprType.Bool
    else -> null
}

/**
 * [Method.toFunType] converts the method to an equivalent [ExprType.Function] in this programming
 * language if possible.
 * If it is impossible due to some rules, it will throw [DisallowedRuntimeFunctionError].
 */
private fun Method.toFunType(): ExprType.Function {
    if (typeParameters.isNotEmpty()) {
        throw DisallowedRuntimeFunctionError()
    }
    val argumentTypes = parameterTypes.mapNotNull { it.toAllowedTypeExpr() }
    if (argumentTypes.size != parameterTypes.size) {
        throw DisallowedRuntimeFunctionError()
    }
    val returnType = this.returnType.toAllowedTypeExpr()
            ?: throw DisallowedRuntimeFunctionError()
    return ExprType.Function(argumentTypes = argumentTypes, returnType = returnType)
}

/**
 * [toAnnotatedFunctionSequence] converts the library instance to a sequence of pairs of the form
 * (methods name, method function type).
 */
private fun R.toAnnotatedFunctionSequence(): Sequence<Pair<String, ExprType>> =
        javaClass.methods.asSequence()
                .filter { Modifier.isStatic(it.modifiers) }
                .filter { it.getAnnotation(RuntimeFunction::class.java) != null }
                .map { it.name to it.toFunType() }

/**
 * [toAnnotatedFunctions] converts the library instance to a list of pairs of the form
 * (methods name, method function type).
 */
internal fun R.toAnnotatedFunctions(): List<Pair<String, ExprType>> =
        toAnnotatedFunctionSequence().toList()

/**
 * [withInjectedRuntime] returns a new type environment that contains the function in the
 * [providedRuntimeLibrary].
 */
internal fun TypeEnv.withInjectedRuntime(providedRuntimeLibrary: R?): TypeEnv {
    val providedRTSeq = providedRuntimeLibrary
            ?.toAnnotatedFunctionSequence()
            ?: emptySequence()
    return providedRTSeq.fold(initial = this) { e, (name, type) -> e.put(key = name, value = type) }
}
