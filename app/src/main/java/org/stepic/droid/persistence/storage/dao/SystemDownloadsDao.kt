package org.stepic.droid.persistence.storage.dao

import io.reactivex.Single
import org.stepic.droid.persistence.model.SystemDownloadRecord

interface SystemDownloadsDao {
    fun get(vararg ids: Long): Single<List<SystemDownloadRecord>>
}