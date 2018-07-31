package org.stepic.droid.persistence.downloads.adapters

import io.reactivex.Observable
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.DownloadTask

interface DownloadTaskAdapter<T> {
    fun convertToTask(vararg ids: Long, configuration: DownloadConfiguration): Observable<DownloadTask>
    fun convertToTask(vararg items: T, configuration: DownloadConfiguration): Observable<DownloadTask>
}