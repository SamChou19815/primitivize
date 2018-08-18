package com.developersam.primitivize.integration

import com.developersam.primitivize.runtime.RuntimeFunction
import com.developersam.primitivize.runtime.RuntimeLibrary

/**
 * The simple runtime.
 */
@Suppress(names = ["UNUSED_PARAMETER"])
object Runtime : RuntimeLibrary {

    @RuntimeFunction
    @JvmStatic
    fun memsize(): Int = 0

    @RuntimeFunction
    @JvmStatic
    fun defense(): Int = 0

    @RuntimeFunction
    @JvmStatic
    fun offense(): Int = 0

    @RuntimeFunction
    @JvmStatic
    fun size(): Int = 0

    @RuntimeFunction
    @JvmStatic
    fun energy(): Int = 0

    @RuntimeFunction
    @JvmStatic
    fun pass(): Int = 0

    @RuntimeFunction
    @JvmStatic
    fun tag(): Int = 0

    @RuntimeFunction
    @JvmStatic
    fun posture(): Int = 0

    @RuntimeFunction
    @JvmStatic
    fun waitFor(): Unit = Unit

    @RuntimeFunction
    @JvmStatic
    fun forward(): Unit = Unit

    @RuntimeFunction
    @JvmStatic
    fun backward(): Unit = Unit

    @RuntimeFunction
    @JvmStatic
    fun left(): Unit = Unit

    @RuntimeFunction
    @JvmStatic
    fun right(): Unit = Unit

    @RuntimeFunction
    @JvmStatic
    fun eat(): Unit = Unit

    @RuntimeFunction
    @JvmStatic
    fun attack(): Unit = Unit

    @RuntimeFunction
    @JvmStatic
    fun grow(): Unit = Unit

    @RuntimeFunction
    @JvmStatic
    fun bud(): Unit = Unit

    @RuntimeFunction
    @JvmStatic
    fun mate(): Unit = Unit

    @RuntimeFunction
    @JvmStatic
    fun tag(i: Int): Unit = Unit

    @RuntimeFunction
    @JvmStatic
    fun serve(i: Int): Unit = Unit

    @RuntimeFunction
    @JvmStatic
    fun nearby(i: Int): Int = 0

    @RuntimeFunction
    @JvmStatic
    fun ahead(i: Int): Int = 0

    @RuntimeFunction
    @JvmStatic
    fun random(i: Int): Int = 0

    @RuntimeFunction
    @JvmStatic
    fun smell(): Int = 0

}
