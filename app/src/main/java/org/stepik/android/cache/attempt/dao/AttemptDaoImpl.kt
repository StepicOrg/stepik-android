package org.stepik.android.cache.attempt.dao

import android.content.ContentValues
import android.database.Cursor
import com.google.gson.Gson
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.getDate
import org.stepic.droid.util.getLong
import org.stepic.droid.util.getString
import org.stepic.droid.util.toObject
import org.stepik.android.cache.attempt.structure.DbStructureAttempt
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.model.attempts.DatasetWrapper
import javax.inject.Inject

class AttemptDaoImpl
@Inject
constructor(
    private val gson: Gson,
    databaseOperations: DatabaseOperations
) : DaoBase<Attempt>(databaseOperations) {

    override fun getDbName(): String =
        DbStructureAttempt.TABLE_NAME

    override fun getDefaultPrimaryColumn(): String =
        DbStructureAttempt.Columns.ID

    override fun getDefaultPrimaryValue(persistentObject: Attempt): String =
        persistentObject.id.toString()

    override fun parsePersistentObject(cursor: Cursor): Attempt =
        Attempt(
            id = cursor.getLong(DbStructureAttempt.Columns.ID),
            step = cursor.getLong(DbStructureAttempt.Columns.STEP),
            user = cursor.getLong(DbStructureAttempt.Columns.USER),
            _dataset = DatasetWrapper(cursor.getString(DbStructureAttempt.Columns.DATASET)?.toObject(gson)),
            datasetUrl = cursor.getString(DbStructureAttempt.Columns.DATASET_URL),
            status = cursor.getString(DbStructureAttempt.Columns.STATUS),
            time = cursor.getDate(DbStructureAttempt.Columns.TIME),
            timeLeft = cursor.getString(DbStructureAttempt.Columns.TIME_LEFT)
        )

    override fun getContentValues(persistentObject: Attempt): ContentValues =
        ContentValues().apply {
            put(DbStructureAttempt.Columns.ID, persistentObject.id)
            put(DbStructureAttempt.Columns.STEP, persistentObject.step)
            put(DbStructureAttempt.Columns.USER, persistentObject.user)
            put(DbStructureAttempt.Columns.DATASET, persistentObject.dataset?.let(gson::toJson))
            put(DbStructureAttempt.Columns.DATASET_URL, persistentObject.datasetUrl)
            put(DbStructureAttempt.Columns.STATUS, persistentObject.status)
            put(DbStructureAttempt.Columns.TIME, persistentObject.time?.time ?: -1)
            put(DbStructureAttempt.Columns.TIME_LEFT, persistentObject.timeLeft)
        }

    override fun insertOrReplaceAll(persistentObjects: List<Attempt>) {
        persistentObjects.forEach(::insertOrReplace)
    }

    override fun insertOrReplace(persistentObject: Attempt) {
        executeSql("""
            INSERT OR REPLACE INTO ${DbStructureAttempt.TABLE_NAME} (
                ${DbStructureAttempt.Columns.ID},
                ${DbStructureAttempt.Columns.STEP},
                ${DbStructureAttempt.Columns.USER},
                ${DbStructureAttempt.Columns.DATASET},
                ${DbStructureAttempt.Columns.DATASET_URL},
                ${DbStructureAttempt.Columns.STATUS},
                ${DbStructureAttempt.Columns.TIME},
                ${DbStructureAttempt.Columns.TIME_LEFT}
            )
            SELECT ?, ?, ?, ?, ?, ?, ?, ?
            WHERE NOT EXISTS (
                SELECT * FROM ${DbStructureAttempt.TABLE_NAME} 
                WHERE ${DbStructureAttempt.Columns.ID} > ? 
                AND ${DbStructureAttempt.Columns.STEP} = ? 
                AND ${DbStructureAttempt.Columns.USER} = ?
            )
        """.trimIndent(),
            arrayOf(
                persistentObject.id,
                persistentObject.step,
                persistentObject.user,
                persistentObject.dataset?.let(gson::toJson),
                persistentObject.datasetUrl,
                persistentObject.status,
                persistentObject.time?.time ?: -1,
                persistentObject.timeLeft,
                persistentObject.id,
                persistentObject.step,
                persistentObject.user
            )
        )
    }
}