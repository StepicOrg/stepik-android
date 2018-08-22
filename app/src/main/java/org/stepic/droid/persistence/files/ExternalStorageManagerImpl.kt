package org.stepic.droid.persistence.files

import android.content.Context
import android.os.Environment
import android.support.v4.content.ContextCompat
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.StorageLocation
import org.stepic.droid.persistence.service.FileTransferService
import org.stepic.droid.preferences.UserPreferences
import java.io.File
import javax.inject.Inject

@PersistenceScope
class ExternalStorageManagerImpl
@Inject
constructor(
        private val analytic: Analytic,
        private val context: Context,
        private val userPreferences: UserPreferences
): ExternalStorageManager {
    @Throws(ExternalStorageNotAvailable::class)
    override fun getAvailableStorageLocations(): List<StorageLocation> {
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

    @Throws(ExternalStorageNotAvailable::class)
    override fun getSelectedStorageLocation(): StorageLocation {
        val locations = getAvailableStorageLocations()
        val selectedLocation = userPreferences.storageLocation
        return if (selectedLocation == null || locations.indexOf(selectedLocation) < 0) {
            locations[0]
        } else {
            selectedLocation
        }
    }

    override fun setStorageLocation(storage: StorageLocation) {
        userPreferences.storageLocation = storage
        FileTransferService.enqueueWork(context) // move files
    }

    override fun resolvePathForPersistentItem(item: PersistentItem): String? {
        if (item.status != PersistentItem.Status.COMPLETED) return null

        val file = if (item.isInAppInternalDir) {
            resolveDownloadsFilesDir()?.absolutePath?.let { File(it, item.localFileName) }
        } else {
            File(item.localFileDir, item.localFileName)
        }

        val path = file?.takeIf(File::exists)?.canonicalPath
        if (path == null) {
            analytic.reportEventWithName(Analytic.DownloaderV2.FILE_NOT_FOND, "${file?.canonicalPath}")
        }
        return path
    }



    class ExternalStorageNotAvailable: IllegalStateException("External storage not available")
}