package org.stepic.droid.persistence.downloads.progress

import io.reactivex.Observable
import org.stepic.droid.persistence.model.DownloadProgress

interface DownloadProgressProvider {
    fun getProgress(vararg ids: Long): Observable<DownloadProgress>
}