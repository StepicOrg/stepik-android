package org.stepic.droid.util

import android.content.Context
import android.net.Uri
import android.support.v4.content.FileProvider
import java.io.File

object CacheUtil {
    fun writeReturnInternalStorageFile(context: Context, fileName: String, fileContents: String): Uri {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(fileContents.toByteArray())
        }
        val file = File(context.filesDir, fileName)
        return FileProvider.getUriForFile(context, "org.stepic.droid.provider", file)
    }
}