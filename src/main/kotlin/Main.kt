@file:JvmName(name = "Main")

import com.developersam.primitivize.codegen.PrettyPrinter
import com.developersam.primitivize.examples.critterlang.CritterCompiler
import com.developersam.primitivize.examples.critterlang.CritterLangRuntime
import com.developersam.primitivize.primitivize
import com.developersam.primitivize.util.toCode

/**
 * Print help messages.
 */
private fun printHelp() {
    println("Usage:")
    println("java -jar primitivizer -primitivize < program.txt")
    println("java -jar primitivizer -critter-compile < program.txt")
}

/**
 * The entry point.
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        printHelp()
    }
    val isPrimitivize = when (args[0]) {
        "-primitivize" -> true
        "-critter-compile" -> false
        else -> {
            printHelp()
            return
        }
    }
    val code = System.`in`.toCode()
    if (isPrimitivize) {
        println(PrettyPrinter.prettyPrint(node = primitivize(code = code)))
    } else {
        println(CritterCompiler.toPrimitiveString(processedProgram = primitivize(
                code = code, providedRuntimeLibrary = CritterLangRuntime
        )))
    }
}
