package org.stepic.droid.persistence

import android.content.Context
import android.os.Environment
import android.support.v4.content.ContextCompat
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.StorageLocation
import org.stepic.droid.preferences.UserPreferences
import java.io.File
import javax.inject.Inject

@PersistenceScope
class ExternalStorageManager
@Inject
constructor(
        private val context: Context,
        private val userPreferences: UserPreferences
) {
    @Throws(ExternalStorageNotAvailable::class)
    fun getAvailableStorageLocations(): List<StorageLocation> {
        val locations = mutableSetOf<StorageLocation>()

        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?.let { locations.add(StorageLocation(it, type = StorageLocation.Type.PRIMARY)) } ?: // by default prefer external files dir
        resolveDownloadsFilesDir()
                ?.let { locations.add(StorageLocation(it, type = StorageLocation.Type.APP_INTERNAL)) } // but if it is unavailable use default app files dir

        ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_DOWNLOADS).filterNotNull().forEach {
            locations.add(StorageLocation(it, type = StorageLocation.Type.SECONDARY))
        }

        if (locations.isEmpty()) throw ExternalStorageNotAvailable()
        return locations.toList()
    }

    @Throws(ExternalStorageNotAvailable::class)
    fun getSelectedStorageLocation(): StorageLocation {
        return TODO()
    }

    private fun resolveDownloadsFilesDir(): File? = context.filesDir?.let {
        File(it, Environment.DIRECTORY_DOWNLOADS)
    }?.let {
        if (!it.exists()) {
            if (!it.mkdir()) {
                return@let null
            }
        }
        return@let it
    }

    class ExternalStorageNotAvailable: IllegalStateException("External storage not available")
}