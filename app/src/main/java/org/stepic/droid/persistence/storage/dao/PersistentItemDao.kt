package org.stepic.droid.persistence.storage.dao

import io.reactivex.Maybe
import io.reactivex.Observable
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepic.droid.storage.dao.IDao

interface PersistentItemDao: IDao<PersistentItem> {
    /**
     * @param selector if empty returns all persistent items available
     */
    fun getItems(selector: Map<String, String>): Observable<List<PersistentItem>>
    fun getItem(selector: Map<String, String>): Maybe<PersistentItem>

    /**
     * @see PersistentItem.Status::isCorrect
     */
    fun getAllCorrectItems(): Observable<List<PersistentItem>>

    fun getItemsByStep(step: Long) =
            getItems(mapOf(DBStructurePersistentItem.Columns.STEP to step.toString()))

    fun getItemsByStatus(status: PersistentItem.Status) =
            getItems(mapOf(DBStructurePersistentItem.Columns.STATUS to status.name))

    fun getItemsByCourse(course: Long) =
            getItems(mapOf(DBStructurePersistentItem.Columns.COURSE to course.toString()))
}