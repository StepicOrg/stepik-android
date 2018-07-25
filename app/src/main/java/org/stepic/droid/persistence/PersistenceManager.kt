package org.stepic.droid.persistence

import io.reactivex.Maybe
import io.reactivex.Observable
import org.stepic.droid.persistence.model.PersistentData

interface PersistenceManager {

    fun subscribeForSections(vararg sectionsIds: Long): Observable<PersistentData>
    fun subscribeForLessons(vararg lessonsIds: Long): Observable<PersistentData>

    fun cacheSection(sectionId: Long): Observable<PersistentData>
    fun cacheLesson(lessonId: Long): Observable<PersistentData>

    fun onDowloadCompleted(downloadId: Long, localPath: String)

    fun resolvePath(originalPath: String): Maybe<String>

}