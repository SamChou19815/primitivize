package com.developersam.primitivize.lowering

/**
 * [VariableRenamingService] is responsible for providing necessary context for renaming
 * variables in programs.
 */
internal class VariableRenamingService {

    /**
     * The counter for variables. Used for number based renaming.
     */
    private var counter: Int = 0

    /**
     * Returns the next available variable name.
     */
    val nextVariableName: String
        get() {
            val name = "_var$counter"
            counter++
            return name
        }

}
