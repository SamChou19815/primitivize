package com.developersam.primitivize

import com.developersam.primitivize.antlr.PLLexer
import com.developersam.primitivize.antlr.PLParser
import com.developersam.primitivize.ast.processed.ProcessedProgram
import com.developersam.primitivize.parser.ProgramBuilder
import com.developersam.primitivize.runtime.RuntimeLibrary
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import java.io.ByteArrayInputStream

/**
 * [primitivize] returns the AST but in lower level for the given [code] with
 * [providedRuntimeLibrary] given to assist type checking.
 */
fun primitivize(code: String, providedRuntimeLibrary: RuntimeLibrary? = null): ProcessedProgram =
        code.toByteArray()
                .let(block = ::ByteArrayInputStream)
                .let(block = ::ANTLRInputStream)
                .let(block = ::PLLexer)
                .let(block = ::CommonTokenStream)
                .let(block = ::PLParser)
                .program()
                .accept(ProgramBuilder)
                .processWith(providedRuntimeLibrary = providedRuntimeLibrary)
