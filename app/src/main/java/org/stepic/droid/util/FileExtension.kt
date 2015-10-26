package org.stepic.droid.util

import java.io.File

fun File.cleanDirectory() {
    if (this.isDirectory)
        for (child in this.listFiles())
            child.cleanDirectory()

    this.delete()
}