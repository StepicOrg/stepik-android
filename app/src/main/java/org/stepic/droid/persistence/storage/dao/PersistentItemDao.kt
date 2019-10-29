package org.stepic.droid.persistence.storage.dao

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepic.droid.storage.dao.IDao

interface PersistentItemDao : IDao<PersistentItem> {
    /**
     * @param selector if empty returns all persistent items available
     */
    fun getItems(selector: Map<String, String>): Single<List<PersistentItem>>
    fun getItem(selector: Map<String, String>): Maybe<PersistentItem>

    /**
     * @see PersistentItem.Status::isCorrect
     */
    fun getAllCorrectItems(): Observable<List<PersistentItem>>

    fun getItemsByStep(step: Long): Single<List<PersistentItem>> =
        getItems(mapOf(DBStructurePersistentItem.Columns.STEP to step.toString()))

    fun getItemsByStatus(status: PersistentItem.Status): Single<List<PersistentItem>> =
        getItems(mapOf(DBStructurePersistentItem.Columns.STATUS to status.name))

    fun getItemsByCourse(course: Long): Single<List<PersistentItem>> =
        getItems(mapOf(DBStructurePersistentItem.Columns.COURSE to course.toString()))
}