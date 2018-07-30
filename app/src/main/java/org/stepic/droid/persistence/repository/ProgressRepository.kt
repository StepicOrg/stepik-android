package org.stepic.droid.persistence.repository

import io.reactivex.Observable
import org.stepic.droid.persistence.model.DownloadProgress

interface ProgressRepository {
    fun getProgress(vararg ids: Long): Observable<DownloadProgress>
}