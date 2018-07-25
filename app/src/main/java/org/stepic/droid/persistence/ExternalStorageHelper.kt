package org.stepic.droid.persistence

import android.content.Context
import android.os.Environment
import android.support.v4.content.ContextCompat
import org.stepic.droid.di.AppSingleton
import java.io.File
import javax.inject.Inject

@AppSingleton
class ExternalStorageHelper
@Inject
constructor(
        private val context: Context
) {
    @Throws(ExternalStorageNotAvailable::class)
    fun getExternalStorageOptions(): List<File> {
        val paths = mutableSetOf<String>()
        val locations = mutableListOf<File>()

        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.let {
            paths.add(it.canonicalPath)
            locations.add(it)
        }

        ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_DOWNLOADS).filterNotNull().forEach {
            if (!paths.contains(it.canonicalPath)) {
                paths.add(it.canonicalPath)
                locations.add(it)
            }
        }

        if (locations.isEmpty()) throw ExternalStorageNotAvailable()
        return locations
    }

    class ExternalStorageNotAvailable: IllegalStateException("External storage not available")
}