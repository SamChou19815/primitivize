package com.developersam.primitivize.integration

import com.developersam.primitivize.ast.processed.ProcessedProgram
import com.developersam.primitivize.codegen.PrettyPrinter
import com.developersam.primitivize.examples.critterlang.CritterCompiler
import com.developersam.primitivize.examples.critterlang.CritterLangRuntime
import com.developersam.primitivize.primitivize
import com.developersam.primitivize.util.toCode
import org.junit.Test

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
            .toCode()
            .let { primitivize(code = it, providedRuntimeLibrary = CritterLangRuntime) }

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
        println(CritterCompiler.toPrimitiveString(ast))
    }

}
