package org.stepic.droid.persistence.downloads.adapters

import io.reactivex.Observable
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.DownloadTask
import org.stepic.droid.persistence.model.PersistentItem
import org.stepik.android.model.Unit

interface UnitDownloadTaskAdapter: DownloadTaskAdapter<Unit> {
    fun convertToPersistentItems(
            courseId: Long,
            sectionId: Long,
            vararg unitIds: Long,
            configuration: DownloadConfiguration
    ): Observable<DownloadTask>
}