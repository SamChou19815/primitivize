package com.developersam.primitivize.ast.type

/**
 * [ExprType] represents a set of all supported types.
 */
sealed class ExprType {

    /**
     * The unit/void type.
     */
    object Unit : ExprType() {

        /**
         * @see Any.toString
         */
        override fun toString(): String = "unit"

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
     * @property returnType the type of return value.
     */
    data class Function(val returnType: ExprType) : ExprType() {

        /**
         * @see Any.toString
         */
        override fun toString(): String = "() -> $returnType"

        /**
         * @see Any.equals
         */
        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other is Function -> returnType == other.returnType
            else -> false
        }

        /**
         * @see Any.hashCode
         */
        override fun hashCode(): kotlin.Int = returnType.hashCode()

    }

}
