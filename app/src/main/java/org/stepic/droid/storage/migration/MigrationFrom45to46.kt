package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepic.droid.storage.structure.DbStructureCertificateViewItem
import org.stepik.android.cache.certificates.structure.DbStructureCertificate

object MigrationFrom45to46: Migration {
    override fun migrate(db: SQLiteDatabase) {
        DbStructureCertificate.createTable(db)
        dropCertificateViewItemTable(db)
    }

    private fun dropCertificateViewItemTable(db: SQLiteDatabase) {
        db.execSQL("""
            DROP TABLE IF EXISTS ${DbStructureCertificateViewItem.CERTIFICATE_VIEW_ITEM}
        """.trimIndent())
    }
}