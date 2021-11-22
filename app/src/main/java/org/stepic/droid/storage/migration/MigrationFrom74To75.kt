package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.certificates.structure.DbStructureCertificate

object MigrationFrom74To75 : Migration(74, 75) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureCertificate.TABLE_NAME} ADD COLUMN ${DbStructureCertificate.Columns.SAVED_FULLNAME} TEXT")
        db.execSQL("ALTER TABLE ${DbStructureCertificate.TABLE_NAME} ADD COLUMN ${DbStructureCertificate.Columns.EDITS_COUNT} INTEGER")
        db.execSQL("ALTER TABLE ${DbStructureCertificate.TABLE_NAME} ADD COLUMN ${DbStructureCertificate.Columns.ALLOWED_EDITS_COUNT} INTEGER")
    }
}