package org.stepic.droid.persistence

import io.reactivex.Maybe
import io.reactivex.Observable
import org.stepic.droid.persistence.model.ProgressItem

interface PersistenceManager {

    fun getSectionsProgress(vararg sectionsIds: Long): Observable<ProgressItem>
    fun getLessonsProgress(vararg lessonsIds: Long): Observable<ProgressItem>

    fun cacheSection(sectionId: Long): Observable<ProgressItem>
    fun cacheLesson(lessonId: Long): Observable<ProgressItem>

    fun onDownloadCompleted(downloadId: Long, localPath: String)

    fun resolvePath(originalPath: String): Maybe<String>

}