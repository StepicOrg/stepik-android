package org.stepic.droid.persistence.storage

import io.reactivex.Observable
import org.stepic.droid.persistence.model.DownloadItem

interface DownloadItemDao {
    fun get(vararg ids: Long): Observable<List<DownloadItem>>
}