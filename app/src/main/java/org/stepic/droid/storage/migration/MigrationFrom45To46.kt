package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepic.droid.storage.structure.DbStructureCertificateViewItem
import org.stepik.android.cache.certificates.structure.DbStructureCertificate

object MigrationFrom45To46 : Migration(45, 46) {
    private const val CODE_SUBMISSION = "code_submission"

    override fun migrate(db: SupportSQLiteDatabase) {
        DbStructureCertificate.createTable(db)
        dropCodeSubmissionTable(db)
        dropCertificateViewItemTable(db)
    }

    private fun dropCodeSubmissionTable(db: SupportSQLiteDatabase) {
        db.execSQL("""
            DROP TABLE IF EXISTS $CODE_SUBMISSION
        """.trimIndent())
    }

    private fun dropCertificateViewItemTable(db: SupportSQLiteDatabase) {
        db.execSQL("""
            DROP TABLE IF EXISTS ${DbStructureCertificateViewItem.CERTIFICATE_VIEW_ITEM}
        """.trimIndent())
    }
}