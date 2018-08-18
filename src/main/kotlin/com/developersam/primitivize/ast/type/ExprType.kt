package com.developersam.primitivize.ast.type

/**
 * [ExprType] represents a set of all supported types.
 */
sealed class ExprType {

    /**
     * The unit/void type.
     */
    object Void : ExprType() {

        /**
         * @see Any.toString
         */
        override fun toString(): String = "void"

    }

    /**
     * The (32 bit) integer type.
     */
    object Int : ExprType() {

        /**
         * @see Any.toString
         */
        override fun toString(): String = "int"

    }

    /**
     * The boolean type.
     */
    object Bool : ExprType() {

        /**
         * @see Any.toString
         */
        override fun toString(): String = "bool"

    }

    /**
     * The function type.
     *
     * @property argumentTypes a list of argument types, which can be empty.
     * @property returnType the type of return value.
     */
    data class Function(val argumentTypes: List<ExprType>, val returnType: ExprType) : ExprType() {

        /**
         * @see Any.toString
         */
        override fun toString(): String =
                "(${argumentTypes.joinToString(separator = ", ")}) -> $returnType"

        /**
         * @see Any.equals
         */
        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other is Function -> argumentTypes == other.argumentTypes &&
                    returnType == other.returnType
            else -> false
        }

        /**
         * @see Any.hashCode
         */
        override fun hashCode(): kotlin.Int = argumentTypes.hashCode() * 31 + returnType.hashCode()

    }

}
