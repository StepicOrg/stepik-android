package org.stepic.droid.persistence.downloads.adapters

import io.reactivex.Observable
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.DownloadTask

interface StepDownloadTaskAdapter {
    fun convertToTask(
            courseId: Long,
            sectionId: Long,
            unitId: Long,
            lessonId: Long,
            vararg stepIds: Long,
            configuration: DownloadConfiguration
    ): Observable<DownloadTask>
}