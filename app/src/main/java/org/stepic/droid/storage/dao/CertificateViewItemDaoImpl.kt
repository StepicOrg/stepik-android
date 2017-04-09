package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.stepic.droid.model.CertificateType
import org.stepic.droid.model.CertificateViewItem
import org.stepic.droid.storage.structure.DbStructureCertificateViewItem
import javax.inject.Inject

class CertificateViewItemDaoImpl @Inject constructor(writeableDatabase: SQLiteDatabase) : DaoBase<CertificateViewItem>(writeableDatabase) {

    override fun getDbName() = DbStructureCertificateViewItem.CERTIFICATE_VIEW_ITEM

    override fun getDefaultPrimaryColumn() = DbStructureCertificateViewItem.Column.CERTIFICATE_ID

    override fun getDefaultPrimaryValue(persistentObject: CertificateViewItem) = persistentObject.certificateId?.toString() ?: "null"

    override fun getContentValues(persistentObject: CertificateViewItem): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(DbStructureCertificateViewItem.Column.CERTIFICATE_ID, persistentObject.certificateId)
        contentValues.put(DbStructureCertificateViewItem.Column.TITLE, persistentObject.title)
        contentValues.put(DbStructureCertificateViewItem.Column.COVER_FULL_PATH, persistentObject.coverFullPath)

        contentValues.put(DbStructureCertificateViewItem.Column.TYPE, persistentObject.type?.ordinal)

        contentValues.put(DbStructureCertificateViewItem.Column.FULL_PATH, persistentObject.fullPath)
        contentValues.put(DbStructureCertificateViewItem.Column.GRADE, persistentObject.grade)
        contentValues.put(DbStructureCertificateViewItem.Column.ISSUE_DATE, persistentObject.issue_date)


        return contentValues
    }

    override fun parsePersistentObject(cursor: Cursor): CertificateViewItem {
        val indexCalendarId = cursor.getColumnIndex(DbStructureCertificateViewItem.Column.CERTIFICATE_ID)
        val indexTitle = cursor.getColumnIndex(DbStructureCertificateViewItem.Column.TITLE)
        val indexCoverFullPath = cursor.getColumnIndex(DbStructureCertificateViewItem.Column.COVER_FULL_PATH)
        val indexType = cursor.getColumnIndex(DbStructureCertificateViewItem.Column.TYPE)
        val indexFullPath = cursor.getColumnIndex(DbStructureCertificateViewItem.Column.FULL_PATH)
        val indexGrade = cursor.getColumnIndex(DbStructureCertificateViewItem.Column.GRADE)
        val indexIssueDate = cursor.getColumnIndex(DbStructureCertificateViewItem.Column.ISSUE_DATE)

        var certificateId: Long? = cursor.getLong(indexCalendarId)
        if (certificateId == 0L) certificateId = null

        val typeId = cursor.getInt(indexType)
        val certificateType = getCertificateTypeByTypeId(typeId)

        val certificateViewItem = CertificateViewItem(
                certificateId = certificateId,
                title = cursor.getString(indexTitle),
                coverFullPath = cursor.getString(indexCoverFullPath),
                type = certificateType,
                fullPath = cursor.getString(indexFullPath),
                grade = cursor.getString(indexGrade),
                issue_date = cursor.getString(indexIssueDate)
        )
        return certificateViewItem

    }

    private fun getCertificateTypeByTypeId(typeId: Int): CertificateType? {
        val localValues = CertificateType.values()
        if (typeId >= 0 && typeId < localValues.size) {
            return localValues[typeId]
        } else {
            return null
        }
    }
}
