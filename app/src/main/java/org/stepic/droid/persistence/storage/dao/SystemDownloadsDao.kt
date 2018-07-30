package org.stepic.droid.persistence.storage.dao

import io.reactivex.Observable
import org.stepic.droid.persistence.model.SystemDownload

interface SystemDownloadsDao {
    fun get(vararg ids: Long): Observable<List<SystemDownload>>
}