package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.course_payments.structure.DbStructureCoursePayments

object MigrationFrom58To59 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        db.execSQL(DbStructureCoursePayments.TABLE_SCHEMA)
    }
}