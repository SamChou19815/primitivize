package com.developersam.primitivize.runtime

import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
/**
 * [RuntimeFunction] is the marker annotation for the interpreter and compiler to detect and
 * validate runtime functions.
 */
annotation class RuntimeFunction
