package com.developersam.primitivize.integration

import com.developersam.primitivize.codegen.PrettyPrinter
import com.developersam.primitivize.primitivize
import com.developersam.primitivize.runtime.RuntimeFunction
import com.developersam.primitivize.runtime.RuntimeLibrary
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * [PrinterTest] tests the validity of the printer by simply printing out the compiled code in
 * pretty-printed format.
 */
class PrinterTest {

    /**
     * A very simple dummy runtime.
     */
    private object SimpleRuntime : RuntimeLibrary {

        @RuntimeFunction
        @JvmStatic
        fun fooBar(): Unit = Unit

    }

    /**
     * [printTest] prints the code.
     */
    @Test
    fun printTest() {
        val code = javaClass.getResourceAsStream("sample_program.txt")
                .let(block = ::InputStreamReader)
                .let(block = ::BufferedReader)
                .lineSequence()
                .joinToString(separator = "\n")
        val ast = primitivize(code = code, providedRuntimeLibrary = SimpleRuntime)
        val compiled = PrettyPrinter.prettyPrint(node = ast)
        println(compiled)
    }

}
