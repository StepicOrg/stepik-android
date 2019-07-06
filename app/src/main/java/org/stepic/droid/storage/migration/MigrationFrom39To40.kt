package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.comment_banner.structure.DbStructureCommentBanner

object MigrationFrom39To40 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        DbStructureCommentBanner.createTable(db)
    }
}