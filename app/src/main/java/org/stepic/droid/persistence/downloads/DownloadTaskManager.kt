package org.stepic.droid.persistence.downloads

import io.reactivex.Completable
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.DownloadTask

interface DownloadTaskManager {
    fun addTask(task: DownloadTask, configuration: DownloadConfiguration): Completable
    fun removeTask(task: DownloadTask): Completable
}