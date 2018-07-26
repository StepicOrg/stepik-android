package org.stepic.droid.persistence.storage

import io.reactivex.Maybe
import io.reactivex.Observable
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.storage.dao.IDao

interface PersistentItemDao: IDao<PersistentItem> {

    fun getItemsBySectionId(sectionId: Long): Observable<List<PersistentItem>>
    fun getItemsByUnitId(unitId: Long): Observable<List<PersistentItem>>

    fun getItemByPath(path: String): Maybe<PersistentItem>

}