package com.developersam.primitivize.integration

import com.developersam.primitivize.ast.processed.ProcessedProgram
import com.developersam.primitivize.codegen.PrettyPrinter
import com.developersam.primitivize.primitivize
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * [PrinterTest] tests the validity of the printer by simply printing out the compiled ast in
 * pretty-printed format.
 */
class PrinterTest {

    /**
     * The ast to print.
     */
    @Suppress(names = ["ReplaceSingleLineLet"])
    private val ast: ProcessedProgram = javaClass.getResourceAsStream("sample_program.txt")
            .let(block = ::InputStreamReader)
            .let(block = ::BufferedReader)
            .lineSequence()
            .joinToString(separator = "\n")
            .let { primitivize(code = it, providedRuntimeLibrary = Runtime) }

    /**
     * [prettyPrinterTest] prints the ast by [PrettyPrinter].
     */
    @Test
    fun prettyPrinterTest() {
        println(PrettyPrinter.prettyPrint(ast))
    }

    /**
     * [primitivePrinterTest] prints the ast by [PrimitivePrinter].
     */
    @Test
    fun primitivePrinterTest() {
        println(PrimitivePrinter.toPrimitiveString(ast))
    }

}
