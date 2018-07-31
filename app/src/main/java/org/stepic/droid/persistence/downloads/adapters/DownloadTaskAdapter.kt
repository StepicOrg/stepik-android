package org.stepic.droid.persistence.downloads.adapters

import io.reactivex.Completable
import org.stepic.droid.persistence.model.DownloadConfiguration

interface DownloadTaskAdapter {
    fun createTask(vararg ids: Long, configuration: DownloadConfiguration): Completable
    fun removeTask(id: Long): Completable
}