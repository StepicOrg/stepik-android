package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.comment_banner.structure.DbStructureCommentBanner

object MigrationFrom42To43 : Migration(42, 43) {
    override fun migrate(db: SupportSQLiteDatabase) {
        DbStructureCommentBanner.dropTable(db)
    }
}