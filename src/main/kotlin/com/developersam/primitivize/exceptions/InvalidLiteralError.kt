package com.developersam.primitivize.exceptions

/**
 * [InvalidLiteralError] reports an invalid literal at given line number during compile time.
 */
class InvalidLiteralError internal constructor(lineNo: Int, invalidLiteral: String) :
        CompileTimeError(reason = "Invalid Literal at line $lineNo Detected: $invalidLiteral.")
