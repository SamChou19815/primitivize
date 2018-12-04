package com.developersam.primitivize.util

import com.developersam.primitivize.examples.critterlang.CritterLangRuntime
import com.developersam.primitivize.primitivize
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Arrays
import java.util.stream.Collectors

/**
 * [Array.toTable] converts an array to a map indexed by string.
 *
 * @param f the function that maps the an element in the array to a string.
 */
internal fun <T> Array<T>.toTable(f: (T) -> String): Map<String, T> =
        Arrays.stream(this).collect(Collectors.toMap(f) { it })

/**
 * Return the string form of an input stream.
 */
internal fun InputStream.toCode(): String =
        let(block = ::InputStreamReader)
                .let(block = ::BufferedReader)
                .lineSequence()
                .joinToString(separator = "\n")
