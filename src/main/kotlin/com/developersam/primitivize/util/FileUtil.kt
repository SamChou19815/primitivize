package com.developersam.primitivize.util

import java.io.File
import java.io.IOException

/**
 * [readFromFile] returns the content in [filename] or `null` if there is no such file.
 */
internal fun readFromFile(filename: String): String? =
        try {
            File(filename).useLines { it.joinToString(separator = "\n") }
        } catch (e: IOException) {
            null
        }

/**
 * [writeToFile] writes the [content] into a file with [filename].
 */
internal fun writeToFile(filename: String, content: String): Unit =
        File(filename).apply { createNewFile() }
                .writeText(content)
