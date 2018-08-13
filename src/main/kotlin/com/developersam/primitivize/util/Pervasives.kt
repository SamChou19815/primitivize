package com.developersam.primitivize.util

import java.util.Arrays
import java.util.stream.Collectors

/**
 * [Array.toTable] converts an array to a map indexed by string.
 *
 * @param f the function that maps the an element in the array to a string.
 */
internal fun <T> Array<T>.toTable(f: (T) -> String): Map<String, T> =
        Arrays.stream(this).collect(Collectors.toMap(f) { it })
