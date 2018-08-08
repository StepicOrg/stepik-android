package org.stepic.droid.persistence.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.di.storage.StorageSingleton
import org.stepic.droid.persistence.model.PersistentState
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentState
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import javax.inject.Inject

@StorageSingleton
class PersistentStateDaoImpl
@Inject
constructor(
        databaseOperations: DatabaseOperations
): DaoBase<PersistentState>(databaseOperations) {
    override fun getDbName() = DBStructurePersistentState.TABLE_NAME

    override fun getDefaultPrimaryColumn() = DBStructurePersistentState.Columns.ID // actually ID + TYPE
    override fun getDefaultPrimaryValue(persistentObject: PersistentState): String = persistentObject.id.toString()

    override fun getContentValues(persistentObject: PersistentState) = ContentValues().apply {
        put(DBStructurePersistentState.Columns.ID, persistentObject.id)
        put(DBStructurePersistentState.Columns.TYPE, persistentObject.type.name)
        put(DBStructurePersistentState.Columns.STATE, persistentObject.state.name)
    }

    override fun parsePersistentObject(cursor: Cursor) = PersistentState (
            id = cursor.getLong(cursor.getColumnIndex(DBStructurePersistentState.Columns.ID)),
            type = PersistentState.Type.valueOf(cursor.getString(cursor.getColumnIndex(DBStructurePersistentState.Columns.TYPE))),
            state = PersistentState.State.valueOf(cursor.getString(cursor.getColumnIndex(DBStructurePersistentState.Columns.STATE)))
    )
}