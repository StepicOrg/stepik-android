package org.stepic.droid.persistence.storage

import org.stepic.droid.persistence.model.PersistentState
import org.stepic.droid.persistence.model.Structure


interface PersistentStateManager {
    fun invalidateStructure(structure: Structure, state: PersistentState.State)
    fun getState(id: Long, type: PersistentState.Type): PersistentState.State
}