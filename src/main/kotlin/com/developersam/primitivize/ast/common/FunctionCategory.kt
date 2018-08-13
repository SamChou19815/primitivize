package com.developersam.primitivize.ast.common

/**
 * [FunctionCategory] is a classification of function's category based on their level of
 * "predefined-ness"
 */
enum class FunctionCategory {

    /**
     * [PROVIDED] represents the functions provided by the user of this compiler.
     * This is the basic mechanism for interacting with other systems.
     */
    PROVIDED,
    /**
     * [USER_DEFINED] represents the functions defined in the actual program. This is the most
     * common category.
     */
    USER_DEFINED

}
