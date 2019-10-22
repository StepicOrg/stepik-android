package org.stepik.android.domain.download.repository

import io.reactivex.Observable
import org.stepic.droid.persistence.model.DownloadItem

interface DownloadRepository {
    fun getDownloads(): Observable<DownloadItem>
}