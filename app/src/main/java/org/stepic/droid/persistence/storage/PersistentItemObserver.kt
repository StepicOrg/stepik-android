package org.stepic.droid.persistence.storage

import org.stepic.droid.persistence.model.PersistentItem

interface PersistentItemObserver {
    fun update(item: PersistentItem)
    fun remove(item: PersistentItem)
}