package org.stepic.droid.persistence.storage

import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.storage.dao.IDao

interface PersistentItemDao: IDao<PersistentItem> {

    fun getItemsBySectionId(sectionId: Long): Observable<List<PersistentItem>>
    fun getItemsByLessonId(lessonId: Long): Observable<List<PersistentItem>>

}