package org.stepik.android.cache.analytic.dao

import android.content.ContentValues
import android.database.Cursor
import com.google.gson.Gson
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.getLong
import org.stepic.droid.util.getString
import org.stepik.android.cache.analytic.structure.DbStructureAnalytic
import org.stepik.android.model.analytic.AnalyticLocalEvent
import javax.inject.Inject

class AnalyticDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations,
    private val gson: Gson
) : DaoBase<AnalyticLocalEvent>(databaseOperations) {
    companion object {
        private const val EMPTY = ""
    }

    override fun getDefaultPrimaryColumn(): String =
        DbStructureAnalytic.Columns.ID

    override fun getDbName(): String =
        DbStructureAnalytic.TABLE_NAME

    override fun getDefaultPrimaryValue(persistentObject: AnalyticLocalEvent?): String =
        EMPTY

    override fun getContentValues(persistentObject: AnalyticLocalEvent): ContentValues =
        ContentValues(3).apply {
            put(DbStructureAnalytic.Columns.EVENT_NAME, persistentObject.name)
            put(DbStructureAnalytic.Columns.EVENT_JSON, persistentObject.eventData.toString())
            put(DbStructureAnalytic.Columns.EVENT_TIMESTAMP, persistentObject.eventTimestamp)
        }

    override fun parsePersistentObject(cursor: Cursor): AnalyticLocalEvent =
        AnalyticLocalEvent(
            cursor.getString(DbStructureAnalytic.Columns.EVENT_NAME)!!,
            gson.toJsonTree(cursor.getString(DbStructureAnalytic.Columns.EVENT_JSON)),
            cursor.getLong(DbStructureAnalytic.Columns.EVENT_TIMESTAMP)
        )
}