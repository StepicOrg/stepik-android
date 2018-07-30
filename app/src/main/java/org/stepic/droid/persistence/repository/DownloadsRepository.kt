package org.stepic.droid.persistence.repository

import io.reactivex.Observable
import org.stepic.droid.persistence.model.DownloadItem

interface DownloadsRepository {
    fun getDownloads(): Observable<DownloadItem>
}