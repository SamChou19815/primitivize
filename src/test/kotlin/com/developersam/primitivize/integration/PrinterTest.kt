package com.developersam.primitivize.integration

import com.developersam.primitivize.ast.processed.ProcessedProgram
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

    @Suppress(names = ["ReplaceSingleLineLet"])
    private val ast: ProcessedProgram = javaClass.getResourceAsStream("sample_program.txt")
            .let(block = ::InputStreamReader)
            .let(block = ::BufferedReader)
            .lineSequence()
            .joinToString(separator = "\n")
            .let { primitivize(code = it, providedRuntimeLibrary = SimpleRuntime) }

    /**
     * A very simple dummy runtime.
     */
    private object SimpleRuntime : RuntimeLibrary {

        @RuntimeFunction
        @JvmStatic
        fun fooBar(): Unit = Unit

    }

    /**
     * [prettyPrinterTest] prints the code.
     */
    @Test
    fun prettyPrinterTest() {
        val compiled = PrettyPrinter.prettyPrint(node = ast)
        println(compiled)
    }

    /**
     * [prettyPrinterTest] prints the code.
     */
    @Test
    fun primitivePrinterTest() {
        val compiled = PrimitivePrinter.toPrimitiveString(processedProgram = ast)
        println(compiled)
    }

}
