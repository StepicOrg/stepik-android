package org.stepic.droid.persistence

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import org.stepic.droid.persistence.model.ProgressItem
import org.stepic.droid.persistence.model.Task

interface PersistenceManager {

    fun getSectionsProgress(vararg sectionsIds: Long): Observable<ProgressItem>
    fun getUnitsProgress(vararg unitsIds: Long): Observable<ProgressItem>

    fun addCacheTask(task: Task): Completable

    fun onDownloadCompleted(downloadId: Long, localPath: String): Completable
    fun resolvePath(originalPath: String): Maybe<String>

}