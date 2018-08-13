package com.developersam.primitivize.exceptions

/**
 * [DisallowedRuntimeFunctionError] reports the problem of using non-primitive-or-string types.
 */
class DisallowedRuntimeFunctionError internal constructor() :
        CompileTimeError(reason = "Runtime functions can only contain primitive types and string.")
