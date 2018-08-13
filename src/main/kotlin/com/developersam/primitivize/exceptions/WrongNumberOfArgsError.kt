package com.developersam.primitivize.exceptions

/**
 * [WrongNumberOfArgsError] reports the problem of too many arguments in function application.
 */
class WrongNumberOfArgsError internal constructor(lineNo: Int, expected: Int, actual: Int) :
        CompileTimeError(
                reason = "Wrong number of arguments in function application at line $lineNo. " +
                        "Expected: $expected, Actual: $actual"
        )
