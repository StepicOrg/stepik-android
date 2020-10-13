package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.course_payments.structure.DbStructureCoursePayments

object MigrationFrom58To59 : Migration(58, 59) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(DbStructureCoursePayments.TABLE_SCHEMA)
    }
}