package org.stepik.android.domain.download.mapper

import org.stepic.droid.persistence.downloads.progress.mapper.DownloadProgressStatusMapper
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.PersistentState
import org.stepic.droid.persistence.model.SystemDownloadRecord
import timber.log.Timber
import javax.inject.Inject

class DownloadProgressStatusMapperImpl
@Inject
constructor() : DownloadProgressStatusMapper {
    override fun countItemProgress(
        persistentItems: List<PersistentItem>,
        downloadRecords: List<SystemDownloadRecord>,
        itemState: PersistentState.State
    ): DownloadProgress.Status {
        Timber.d("A")
        return DownloadProgress.Status.NotCached
    }
}