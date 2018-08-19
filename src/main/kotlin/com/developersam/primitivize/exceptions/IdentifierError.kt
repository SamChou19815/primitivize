package com.developersam.primitivize.exceptions

/**
 * [IdentifierError] is a collection of errors related to identifiers.
 */
sealed class IdentifierError(reason: String) : CompileTimeError(reason = reason) {

    /**
     * [ShadowedName] reports a shadowed Name of identifiers in declaration
     * during compile time.
     */
    class ShadowedName internal constructor(lineNo: Int, shadowedName: String) :
            IdentifierError(reason = "Shadowed name at line $lineNo detected: $shadowedName.")

    /**
     * [UndefinedIdentifier] reports that bad identifier is not found in the current
     * environment.
     */
    class UndefinedIdentifier internal constructor(lineNo: Int, badIdentifier: String) :
            IdentifierError(reason = "Identifier $badIdentifier at line $lineNo is not found.")

    /**
     * [NotAssignableIdentifier] reports that bad identifier is not assignable in the current
     * environment.
     */
    class NotAssignableIdentifier internal constructor(badIdentifier: String) :
            CompileTimeError(reason = "$badIdentifier is not assignable.")

}