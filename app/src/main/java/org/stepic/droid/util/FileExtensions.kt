package org.stepic.droid.util

import java.io.File

fun File.size(): Long = if (isDirectory) {
    listFiles()?.map(File::size)?.sum() ?: 0
} else {
    length()
}