package org.stepik.android.cache.user.structure

import android.database.sqlite.SQLiteDatabase

object DbStructureUser {
    const val TABLE_NAME = "users"

    object Columns {
        const val ID = "id"
        const val PROFILE = "profile"
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
        const val FULL_NAME = "full_name"
        const val SHORT_BIO = "short_bio"
        const val DETAILS = "details"
        const val AVATAR = "avatar"
        const val COVER = "cover"
        const val IS_PRIVATE = "is_private"
        const val IS_ORGANIZATION = "is_organization"

        const val SOCIAL_PROFILES = "social_profiles"
        const val KNOWLEDGE = "knowledge"
        const val KNOWLEDGE_RANK = "knowledge_rank"
        const val REPUTATION = "reputation"
        const val REPUTATION_RANK = "reputation_rank"

        const val JOIN_DATE = "join_date"
    }

    fun createTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                ${DbStructureUser.Columns.ID} LONG PRIMARY KEY,
                ${DbStructureUser.Columns.PROFILE} LONG,
                ${DbStructureUser.Columns.FIRST_NAME} TEXT,
                ${DbStructureUser.Columns.LAST_NAME} TEXT,
                ${DbStructureUser.Columns.FULL_NAME} TEXT,
                ${DbStructureUser.Columns.SHORT_BIO} TEXT,
                ${DbStructureUser.Columns.DETAILS} TEXT,
                ${DbStructureUser.Columns.AVATAR} TEXT,
                ${DbStructureUser.Columns.IS_PRIVATE} INTEGER,
                ${DbStructureUser.Columns.IS_ORGANIZATION} INTEGER,
                ${DbStructureUser.Columns.JOIN_DATE} LONG
            )
        """.trimIndent())
    }
}