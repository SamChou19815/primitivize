package com.developersam.primitivize.integration

import com.developersam.primitivize.codegen.PrettyPrinter
import com.developersam.primitivize.primitivize
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * [PrinterTest] tests the validity of the printer by simply printing out the compiled code in
 * pretty-printed format.
 */
class PrinterTest {

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
        val ast = primitivize(code = code)
        val compiled = PrettyPrinter.prettyPrint(node = ast)
        println(compiled)
    }

}
