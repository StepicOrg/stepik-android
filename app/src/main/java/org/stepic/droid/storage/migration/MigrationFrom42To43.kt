package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.comment_banner.structure.DbStructureCommentBanner

object MigrationFrom42To43 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        DbStructureCommentBanner.dropTable(db)
    }
}