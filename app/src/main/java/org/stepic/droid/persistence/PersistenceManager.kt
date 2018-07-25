package org.stepic.droid.persistence

import io.reactivex.Maybe
import io.reactivex.Observable
import org.stepic.droid.persistence.model.ProgressItem

interface PersistenceManager {

    fun subscribeForSections(vararg sectionsIds: Long): Observable<ProgressItem>
    fun subscribeForLessons(vararg lessonsIds: Long): Observable<ProgressItem>

    fun cacheSection(sectionId: Long): Observable<ProgressItem>
    fun cacheLesson(lessonId: Long): Observable<ProgressItem>

    fun onDowloadCompleted(downloadId: Long, localPath: String)

    fun resolvePath(originalPath: String): Maybe<String>

}