package org.stepik.android.cache.certificates.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.getDate
import org.stepic.droid.util.getInt
import org.stepic.droid.util.getLong
import org.stepic.droid.util.getString
import org.stepik.android.cache.certificates.structure.DbStructureCertificate
import org.stepik.android.model.Certificate
import javax.inject.Inject

class CertificateDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations
) : DaoBase<Certificate>(databaseOperations) {

    override fun getDbName(): String =
        DbStructureCertificate.TABLE_NAME

    override fun getDefaultPrimaryColumn(): String =
        DbStructureCertificate.Columns.ID

    override fun getDefaultPrimaryValue(persistentObject: Certificate): String =
        persistentObject.id.toString()

    override fun parsePersistentObject(cursor: Cursor): Certificate =
        Certificate(
            id = cursor.getLong(DbStructureCertificate.Columns.ID),
            user = cursor.getLong(DbStructureCertificate.Columns.USER),
            course = cursor.getLong(DbStructureCertificate.Columns.COURSE),
            issueDate = cursor.getDate(DbStructureCertificate.Columns.ISSUE_DATE),
            updateDate = cursor.getDate(DbStructureCertificate.Columns.UPDATE_DATE),
            grade = cursor.getString(DbStructureCertificate.Columns.GRADE),
            type = Certificate.Type.values()[cursor.getInt(DbStructureCertificate.Columns.TYPE)],
            url = cursor.getString(DbStructureCertificate.Columns.URL),
            userRank = cursor.getLong(DbStructureCertificate.Columns.USER_RANK),
            userRankMax = cursor.getLong(DbStructureCertificate.Columns.USER_RANK_MAX),
            leaderboardSize = cursor.getLong(DbStructureCertificate.Columns.LEADERBOARD_SIZE),
            previewUrl = cursor.getString(DbStructureCertificate.Columns.PREVIEW_URL),
            savedFullName = cursor.getString(DbStructureCertificate.Columns.SAVED_FULLNAME),
            editsCount = cursor.getInt(DbStructureCertificate.Columns.EDITS_COUNT),
            allowedEditsCount = cursor.getInt(DbStructureCertificate.Columns.ALLOWED_EDITS_COUNT)
        )

    override fun getContentValues(persistentObject: Certificate): ContentValues =
        ContentValues().apply {
            put(DbStructureCertificate.Columns.ID, persistentObject.id)
            put(DbStructureCertificate.Columns.USER, persistentObject.user)
            put(DbStructureCertificate.Columns.COURSE, persistentObject.course)
            put(DbStructureCertificate.Columns.ISSUE_DATE, persistentObject.issueDate?.time ?: -1)
            put(DbStructureCertificate.Columns.UPDATE_DATE, persistentObject.updateDate?.time ?: -1)
            put(DbStructureCertificate.Columns.GRADE, persistentObject.grade)
            put(DbStructureCertificate.Columns.TYPE, persistentObject.type?.ordinal)
            put(DbStructureCertificate.Columns.URL, persistentObject.url)
            put(DbStructureCertificate.Columns.USER_RANK, persistentObject.userRank)
            put(DbStructureCertificate.Columns.USER_RANK_MAX, persistentObject.userRankMax)
            put(DbStructureCertificate.Columns.LEADERBOARD_SIZE, persistentObject.leaderboardSize)
            put(DbStructureCertificate.Columns.PREVIEW_URL, persistentObject.previewUrl)
            put(DbStructureCertificate.Columns.SAVED_FULLNAME, persistentObject.savedFullName)
            put(DbStructureCertificate.Columns.EDITS_COUNT, persistentObject.editsCount)
            put(DbStructureCertificate.Columns.ALLOWED_EDITS_COUNT, persistentObject.allowedEditsCount)
        }
}