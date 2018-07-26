package org.stepic.droid.persistence

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import org.stepic.droid.persistence.model.ProgressItem

interface PersistenceManager {

    fun getSectionsProgress(vararg sectionsIds: Long): Observable<ProgressItem>
    fun getUnitsProgress(vararg unitsIds: Long): Observable<ProgressItem>

    fun addCacheSectionTask(sectionId: Long): Completable
    fun addCacheUnitTask(unitId: Long): Completable


    fun onDownloadCompleted(downloadId: Long, localPath: String): Completable
    fun resolvePath(originalPath: String): Maybe<String>

}