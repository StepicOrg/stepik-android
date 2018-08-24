package org.stepic.droid.persistence.storage.dao

import org.stepic.droid.persistence.model.PersistentState
import org.stepic.droid.storage.dao.IDao

interface PersistentStateDao : IDao<PersistentState> {
    fun resetInProgressItems()
}