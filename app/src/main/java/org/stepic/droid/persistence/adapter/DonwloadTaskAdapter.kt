package org.stepic.droid.persistence.adapter

import io.reactivex.Completable
import org.stepic.droid.persistence.model.DownloadConfiguration

interface DonwloadTaskAdapter {
    fun addTask(vararg ids: Long, configuration: DownloadConfiguration): Completable
    fun removeTask(id: Long): Completable
}