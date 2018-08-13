package com.developersam.primitivize.ast.common

import com.developersam.primitivize.ast.type.ExprType

/**
 * [Literal] represents a set of supported literal.
 *
 * @property inferredType the inferred type from the literal.
 */
sealed class Literal(val inferredType: ExprType) {

    /**
     * [Int] is the literal for int with [value].
     *
     * @property value value of the literal.
     */
    data class Int(val value: kotlin.Int) : Literal(inferredType = ExprType.Int) {

        /**
         * Returns the string representation of this int.
         */
        override fun toString(): kotlin.String = value.toString()

    }

    /**
     * [Bool] is the literal for bool with [value].
     *
     * @property value value of the literal.
     */
    data class Bool(val value: Boolean) : Literal(inferredType = ExprType.Bool) {

        /**
         * Returns the string representation of this bool.
         */
        override fun toString(): kotlin.String = value.toString()

    }

}
