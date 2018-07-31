package org.stepic.droid.persistence.downloads.adapters

import io.reactivex.Completable
import org.stepic.droid.persistence.model.DownloadConfiguration

interface StepDownloadTaskAdapter {
    fun addTask(courseId: Long, sectionId: Long, unitId: Long, lessonId: Long, vararg stepIds: Long, configuration: DownloadConfiguration): Completable
}