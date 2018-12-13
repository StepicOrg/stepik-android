package org.stepik.android.cache.personal_deadlines.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepik.android.cache.personal_deadlines.structure.DbStructureDeadlinesBanner
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import javax.inject.Inject

class DeadlinesBannerDaoImpl
@Inject
constructor(databaseOperations: DatabaseOperations): DaoBase<Long>(databaseOperations),
    DeadlinesBannerDao {
    override fun getDbName() =
            DbStructureDeadlinesBanner.DEADLINES_BANNER

    override fun getDefaultPrimaryColumn() =
            DbStructureDeadlinesBanner.Columns.COURSE_ID

    override fun getDefaultPrimaryValue(persistentObject: Long) =
            persistentObject.toString()

    override fun getContentValues(persistentObject: Long): ContentValues = ContentValues().apply {
        put(DbStructureDeadlinesBanner.Columns.COURSE_ID, persistentObject)
    }

    override fun parsePersistentObject(cursor: Cursor) =
            cursor.getLong(cursor.getColumnIndex(DbStructureDeadlinesBanner.Columns.COURSE_ID))
}