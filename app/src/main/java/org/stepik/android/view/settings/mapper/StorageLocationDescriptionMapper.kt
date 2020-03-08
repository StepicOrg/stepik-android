package org.stepik.android.view.settings.mapper

import android.content.Context
import org.stepic.droid.R
import org.stepic.droid.persistence.model.StorageLocation
import org.stepic.droid.util.TextUtil
import javax.inject.Inject

class StorageLocationDescriptionMapper
@Inject
constructor(
    private val context: Context
) {
    fun mapToDescription(index: Int, location: StorageLocation): String {
        val freeTitle = context.getString(R.string.free_title)
        val title = context.getString(if (index == 0) R.string.default_storage else R.string.secondary_storage)
        val totalSpace = TextUtil.formatBytes(location.totalSpaceBytes)
        val freeSpace = TextUtil.formatBytes(location.freeSpaceBytes)
        return "$title. $freeSpace / $totalSpace $freeTitle"
    }
}