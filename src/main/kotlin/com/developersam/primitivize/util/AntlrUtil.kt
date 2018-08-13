package com.developersam.primitivize.util

import com.developersam.primitivize.antlr.PLLexer
import com.developersam.primitivize.antlr.PLParser
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import com.developersam.primitivize.ast.raw.RawProgram
import com.developersam.primitivize.parser.TopLevelMemberBuilder
import java.io.ByteArrayInputStream

/**
 * [createRawProgramFromSource] tries to create a raw program from the source files in the given
 * [code].
 */
internal fun createRawProgramFromSource(code: String): RawProgram =
        code.toByteArray().let(block = ::ByteArrayInputStream)
                .let(block = ::ANTLRInputStream)
                .let(block = ::PLLexer)
                .let(block = ::CommonTokenStream)
                .let(block = ::PLParser)
                .program().topLevelDeclaration()
                .map { it.accept(TopLevelMemberBuilder) }
                .let(block = ::RawProgram)
