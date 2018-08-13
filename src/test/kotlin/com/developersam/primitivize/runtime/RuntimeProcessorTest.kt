package com.developersam.primitivize.runtime

import com.developersam.primitivize.ast.type.ExprType
import com.developersam.primitivize.exceptions.DisallowedRuntimeFunctionError
import junit.framework.TestCase.assertEquals
import org.junit.Test

/**
 * [RuntimeProcessorTest] tests whether the Runtime Processor works correctly.
 */
class RuntimeProcessorTest {

    /**
     * [testCorrectness] tests whether running `RuntimeProcessor` on [lib] will produce
     * [expectedOutput].
     */
    private fun testCorrectness(lib: RuntimeLibrary, expectedOutput: List<Pair<String, ExprType>>) {
        assertEquals(expectedOutput, lib.toAnnotatedFunctions())
    }

    /**
     * [emptyTest] tests whether an empty [RuntimeLibrary] corresponds to an empty list.
     */
    @Test
    fun emptyTest() {
        testCorrectness(lib = object : RuntimeLibrary {}, expectedOutput = emptyList())
    }

    /**
     * [RuntimeLibraryWithOnePrivateFunction] is a runtime library with only private function.
     */
    private object RuntimeLibraryWithOnePrivateFunction : RuntimeLibrary {
        @RuntimeFunction
        @JvmStatic
        private fun abc(): Unit = Unit
    }

    /**
     * [RuntimeLibraryWithOnePrivateFunction] is a runtime library with only private function.
     */
    private object RuntimeLibraryWithOnePublicFunction : RuntimeLibrary {
        @RuntimeFunction
        @JvmStatic
        fun ab(): Unit = Unit
    }

    /**
     * [onePrivateFunctionTest] tests whether a [RuntimeLibrary] with only one private function
     * corresponds to an empty list.
     */
    @Test
    fun onePrivateFunctionTest() {
        testCorrectness(lib = RuntimeLibraryWithOnePrivateFunction, expectedOutput = emptyList())
    }

    /**
     * [onePublicFunctionTest] tests whether a [RuntimeLibrary] with only one public function
     * corresponds to a singleton list with that function's info.
     */
    @Test
    fun onePublicFunctionTest() {
        testCorrectness(lib = RuntimeLibraryWithOnePublicFunction, expectedOutput = listOf(
                "ab" to ExprType.Function(emptyList(), ExprType.Unit)
        ))
    }

    /**
     * [RuntimeLibraryWithNoAnnotation] is a runtime library with no annotated function.
     */
    private object RuntimeLibraryWithNoAnnotation : RuntimeLibrary {
        @JvmStatic
        fun abc(): Unit = Unit
    }

    /**
     * [onePublicFunctionTest] tests whether a [RuntimeLibrary] with no annotated functions
     * corresponds to an empty list.
     */
    @Test
    fun noAnnotationEmptyTest() {
        testCorrectness(lib = RuntimeLibraryWithNoAnnotation, expectedOutput = emptyList())
    }

    /**
     * [SimpleLibraryWithGenerics] is a quick demo to see how generics works in the processor.
     */
    private object SimpleLibraryWithGenerics : RuntimeLibrary {
        @RuntimeFunction
        @JvmStatic
        fun <T : Any> objectToString(obj: T): String = obj.toString()

    }

    /**
     * [simpleLibraryWithGenericsDisallowedTest] tests whether the processor can correctly deal
     * with generics information when it's not allowed.
     */
    @Test(expected = DisallowedRuntimeFunctionError::class)
    fun simpleLibraryWithGenericsDisallowedTest() {
        SimpleLibraryWithGenerics.toAnnotatedFunctions()
    }

}
