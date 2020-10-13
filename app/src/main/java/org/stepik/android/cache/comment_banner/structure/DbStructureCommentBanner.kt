package org.stepik.android.cache.comment_banner.structure

import androidx.sqlite.db.SupportSQLiteDatabase

@Deprecated("Removed structure")
object DbStructureCommentBanner {
    private const val TABLE_NAME = "comments_banner"

    fun dropTable(db: SupportSQLiteDatabase) {
        db.execSQL("""
            DROP TABLE IF EXISTS $TABLE_NAME
        """.trimIndent())
    }
}