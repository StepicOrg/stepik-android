package org.stepik.android.cache.social_profile.structure

object DbStructureSocialProfile {
    const val TABLE_NAME = "social_profiles"

    object Columns {
        const val ID = "id"
        const val USER = "user"
        const val PROVIDER = "provider"
        const val NAME = "name"
        const val URL = "url"
    }

    const val TABLE_SCHEMA =
        "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
            "${Columns.ID} LONG PRIMARY KEY," +
            "${Columns.USER} LONG," +
            "${Columns.PROVIDER} TEXT," +
            "${Columns.NAME} TEXT," +
            "${Columns.URL} TEXT"
}