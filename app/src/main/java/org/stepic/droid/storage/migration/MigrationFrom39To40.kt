package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.comments.structure.DbStructureCommentsBanner

object MigrationFrom39To40 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        DbStructureCommentsBanner.createTable(db)
    }
}