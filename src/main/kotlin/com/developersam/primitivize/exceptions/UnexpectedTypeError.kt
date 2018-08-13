package com.developersam.primitivize.exceptions

import com.developersam.primitivize.ast.type.ExprType

/**
 * [UnexpectedTypeError] reports an unexpected type during compile time type checking.
 *
 * @param expectedType the expected type according to the context.
 * @param actualType actual type deduced from the expression.
 */
class UnexpectedTypeError internal constructor(
        lineNo: Int, expectedType: String, actualType: ExprType
) : CompileTimeError(reason = "Line $lineNo: Unexpected type: $actualType; " +
        "Expected: $expectedType.") {

    internal companion object {

        /**
         * [check] checks whether [actualType] matches [expectedType] at [lineNo]. If not, it will
         * throw [UnexpectedTypeError].
         */
        @JvmStatic
        fun check(lineNo: Int, expectedType: ExprType, actualType: ExprType) {
            if (expectedType != actualType) {
                throw UnexpectedTypeError(lineNo, expectedType.toString(), actualType)
            }
        }

    }

}
