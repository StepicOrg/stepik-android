package org.stepic.droid.persistence.files

import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.StorageLocation

interface ExternalStorageManager {
    fun getAvailableStorageLocations(): List<StorageLocation>
    fun getSelectedStorageLocation(): StorageLocation
    fun setStorageLocation(storage: StorageLocation)

    /**
     * Returns local canonical path to file in item
     *
     * @return path - return resolved path iff item status is correct and target file exists
     */
    fun resolvePathForPersistentItem(item: PersistentItem): String?
}