package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.course_payments.structure.DbStructureCoursePayments

object MigrationFrom59To60 : Migration(59, 60) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // todo migrate Step table
    }
}