package org.stepic.droid.persistence

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import org.stepic.droid.persistence.model.DownloadProgress

interface PersistenceManager {

    fun getSectionsProgress(vararg sectionsIds: Long): Observable<DownloadProgress>
    fun getUnitsProgress(vararg unitsIds: Long): Observable<DownloadProgress>


    fun onDownloadCompleted(downloadId: Long, localPath: String): Completable
    fun resolvePath(originalPath: String): Maybe<String>

}