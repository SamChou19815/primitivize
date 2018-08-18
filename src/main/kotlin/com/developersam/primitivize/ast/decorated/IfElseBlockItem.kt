package com.developersam.primitivize.ast.decorated

import com.developersam.primitivize.ast.decorated.DecoratedExpression

/**
 * The if-else block item with a [condition] and [action]. It is a construct to print prettier code
 * with the if-else block
 *
 * @property condition the condition to hold.
 * @property action the code to execute when the condition holds.
 */
data class IfElseBlockItem(
        val condition: DecoratedExpression,
        val action: DecoratedExpression
)