package org.stepic.droid.persistence.downloads.progress

import io.reactivex.Flowable
import org.stepic.droid.persistence.model.DownloadProgress

interface DownloadProgressProvider<T> {
    fun getProgress(vararg ids: Long): Flowable<DownloadProgress>
    fun getProgress(vararg items: T): Flowable<DownloadProgress>
}