package org.stepic.droid.persistence.storage

import io.reactivex.Maybe
import io.reactivex.Observable
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.storage.dao.IDao

interface PersistentItemDao: IDao<PersistentItem> {
    fun getItems(selector: Map<String, String>): Observable<List<PersistentItem>>
    fun getItem(selector: Map<String, String>): Maybe<PersistentItem>
}