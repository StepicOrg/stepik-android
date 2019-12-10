package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.social_profile.structure.DbStructureSocialProfile
import org.stepik.android.cache.user.structure.DbStructureUser

object MigrationFrom49To50 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureUser.TABLE_NAME} ADD COLUMN ${DbStructureUser.Columns.COVER} TEXT")
        db.execSQL("ALTER TABLE ${DbStructureUser.TABLE_NAME} ADD COLUMN ${DbStructureUser.Columns.SOCIAL_PROFILES} TEXT")
        db.execSQL("ALTER TABLE ${DbStructureUser.TABLE_NAME} ADD COLUMN ${DbStructureUser.Columns.KNOWLEDGE} LONG")
        db.execSQL("ALTER TABLE ${DbStructureUser.TABLE_NAME} ADD COLUMN ${DbStructureUser.Columns.KNOWLEDGE_RANK} LONG")
        db.execSQL("ALTER TABLE ${DbStructureUser.TABLE_NAME} ADD COLUMN ${DbStructureUser.Columns.REPUTATION} LONG")
        db.execSQL("ALTER TABLE ${DbStructureUser.TABLE_NAME} ADD COLUMN ${DbStructureUser.Columns.REPUTATION_RANK} LONG")

        db.execSQL("ALTER TABLE ${DbStructureUser.TABLE_NAME} ADD COLUMN ${DbStructureUser.Columns.CREATED_COURSES_COUNT} LONG")
        db.execSQL("ALTER TABLE ${DbStructureUser.TABLE_NAME} ADD COLUMN ${DbStructureUser.Columns.FOLLOWERS_COUNT} LONG")
        db.execSQL("ALTER TABLE ${DbStructureUser.TABLE_NAME} ADD COLUMN ${DbStructureUser.Columns.ISSUED_CERTIFICATES_COUNT} LONG")
        db.execSQL("ALTER TABLE ${DbStructureUser.TABLE_NAME} ADD COLUMN ${DbStructureUser.Columns.IS_GUEST} INTEGER")

        db.execSQL(DbStructureSocialProfile.TABLE_SCHEMA)
    }
}