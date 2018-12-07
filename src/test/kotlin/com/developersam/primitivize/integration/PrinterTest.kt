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
     * Returns an AST given a [resourceName] that contains the source.
     */
    private fun getAst(resourceName: String): ProcessedProgram =
            javaClass.getResourceAsStream(resourceName)
                    .toCode()
                    .let { primitivize(code = it, providedRuntimeLibrary = CritterLangRuntime) }

    /**
     * [prettyPrinterTest] prints the ast by [PrettyPrinter].
     */
    @Test
    fun prettyPrinterTest() {
        println(PrettyPrinter.prettyPrint(getAst(resourceName = "sample_program.txt")))
        println(PrettyPrinter.prettyPrint(getAst(resourceName = "critter-program.txt")))
        println(PrettyPrinter.prettyPrint(getAst(resourceName = "recursive-program.txt")))
    }

    /**
     * [critterCompilerTest] prints the ast by [CritterCompiler].
     */
    @Test
    fun critterCompilerTest() {
        println(CritterCompiler.toPrimitiveString(getAst(resourceName = "sample_program.txt")))
        println(CritterCompiler.toPrimitiveString(getAst(resourceName = "critter-program.txt")))
        // println(CritterCompiler.toPrimitiveString(getAst(resourceName = "recursive-program.txt")))
    }

}
