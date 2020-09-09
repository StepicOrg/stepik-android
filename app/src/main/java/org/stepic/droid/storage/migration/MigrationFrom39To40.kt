package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.comment_banner.structure.DbStructureCommentBanner

object MigrationFrom39To40 : Migration(39, 40) {
    override fun migrate(db: SupportSQLiteDatabase) {
        DbStructureCommentBanner.createTable(db)
    }
}