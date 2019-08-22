package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepic.droid.storage.structure.DbStructureCertificateViewItem
import org.stepik.android.cache.certificates.structure.DbStructureCertificate

object MigrationFrom45To46 : Migration {
    private const val CODE_SUBMISSION = "code_submission"

    override fun migrate(db: SQLiteDatabase) {
        DbStructureCertificate.createTable(db)
        dropCodeSubmissionTable(db)
        dropCertificateViewItemTable(db)
    }

    private fun dropCodeSubmissionTable(db: SQLiteDatabase) {
        db.execSQL("""
            DROP TABLE IF EXISTS $CODE_SUBMISSION
        """.trimIndent())
    }

    private fun dropCertificateViewItemTable(db: SQLiteDatabase) {
        db.execSQL("""
            DROP TABLE IF EXISTS ${DbStructureCertificateViewItem.CERTIFICATE_VIEW_ITEM}
        """.trimIndent())
    }
}