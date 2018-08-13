package com.developersam.primitivize.runtime

import com.developersam.primitivize.ast.common.FunctionCategory
import com.developersam.primitivize.ast.common.Literal
import com.developersam.primitivize.ast.raw.TopLevelMember
import com.developersam.primitivize.ast.raw.LiteralExpr
import com.developersam.primitivize.ast.type.ExprType
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
    "void" -> ExprType.Unit
    "int" -> ExprType.Int
    "boolean" -> ExprType.Bool
    else -> null
}

/**
 * [Method.toTypeInfo] converts the method to an equivalent [ExprType.Function] in this programming
 * language if possible.
 * If it is impossible due to some rules, it will throw [DisallowedRuntimeFunctionError].
 */
private fun Method.toTypeInfo(): ExprType.Function {
    if (typeParameters.isNotEmpty()) {
        throw DisallowedRuntimeFunctionError()
    }
    val parameterTypes = genericParameterTypes
            .asSequence()
            .map { it.toAllowedTypeExpr() }
            .filterNotNull()
            .toList()
    if (parameterTypes.size != genericParameterTypes.size) {
        throw DisallowedRuntimeFunctionError()
    }
    val returnType = this.returnType.toAllowedTypeExpr()
            ?: throw DisallowedRuntimeFunctionError()
    return ExprType.Function(argumentTypes = parameterTypes, returnType = returnType)
}

/**
 * [toAnnotatedFunctionSequence] converts the library instance to a sequence of pairs of the form
 * (methods name, method function type).
 */
private fun R.toAnnotatedFunctionSequence(): Sequence<Pair<String, ExprType>> =
        this::class.java.methods.asSequence()
                .filter { Modifier.isStatic(it.modifiers) }
                .filter { it.getAnnotation(RuntimeFunction::class.java) != null }
                .map { it.name to it.toTypeInfo() }

/**
 * [toAnnotatedFunctions] converts the library instance to a list of pairs of the form
 * (methods name, method function type).
 */
internal fun R.toAnnotatedFunctions(): List<Pair<String, ExprType>> =
        toAnnotatedFunctionSequence().toList()

/**
 * [toFunctionMember] converts a pair of function name and type info to a class function member
 * with the specified function category [c].
 */
private fun Pair<String, ExprType>.toFunctionMember(c: FunctionCategory): TopLevelMember.Function {
    val (name, type) = this
    val functionType = type as ExprType.Function
    val arguments = functionType.argumentTypes.mapIndexed { i, t -> "var$i" to t }
    return TopLevelMember.Function(
            category = c, identifierLineNo = -1, identifier = name,
            arguments = arguments, returnType = functionType.returnType,
            body = LiteralExpr(lineNo = 0, literal = Literal.Int(value = 0)) // dummy expression
    )
}

/**
 * [withInjectedRuntime] returns a copy of itself and with an optional [providedRuntimeLibrary]
 * injected to itself.
 */
internal fun List<TopLevelMember>.withInjectedRuntime(
        providedRuntimeLibrary: R?
): List<TopLevelMember> {
    val providedRTSeq = providedRuntimeLibrary
            ?.toAnnotatedFunctionSequence()
            ?.map { it.toFunctionMember(c = FunctionCategory.PROVIDED) }
            ?: emptySequence()
    val newFunctionMembers = providedRTSeq.toList()
    val oldMembers = this
    return ArrayList<TopLevelMember>(oldMembers).apply { addAll(elements = newFunctionMembers) }
}
