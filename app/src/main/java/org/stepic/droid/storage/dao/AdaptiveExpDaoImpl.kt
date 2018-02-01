package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.adaptive.model.LocalExpItem
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureAdaptiveExp
import javax.inject.Inject

class AdaptiveExpDaoImpl
@Inject
constructor(databaseOperations: DatabaseOperations) : DaoBase<LocalExpItem>(databaseOperations), AdaptiveExpDao {
    override fun getDbName() = DbStructureAdaptiveExp.ADAPTIVE_EXP

    override fun getDefaultPrimaryColumn() = DbStructureAdaptiveExp.Column.SUBMISSION_ID

    override fun getDefaultPrimaryValue(persistentObject: LocalExpItem) = persistentObject.submissionId.toString()

    override fun getContentValues(persistentObject: LocalExpItem): ContentValues {
        val contentValues = ContentValues()

        contentValues.put(DbStructureAdaptiveExp.Column.SUBMISSION_ID, persistentObject.submissionId)
        contentValues.put(DbStructureAdaptiveExp.Column.COURSE_ID, persistentObject.courseId)
        contentValues.put(DbStructureAdaptiveExp.Column.EXP, persistentObject.exp)

        return contentValues
    }

    override fun parsePersistentObject(cursor: Cursor) = LocalExpItem(
            cursor.getLong(cursor.getColumnIndex(DbStructureAdaptiveExp.Column.EXP)),
            cursor.getLong(cursor.getColumnIndex(DbStructureAdaptiveExp.Column.SUBMISSION_ID)),
            cursor.getLong(cursor.getColumnIndex(DbStructureAdaptiveExp.Column.COURSE_ID))
    )

    override fun getExpItem(courseId: Long, submissionId: Long): LocalExpItem? {
        val sqlPrefix = "SELECT * FROM $dbName WHERE ${DbStructureAdaptiveExp.Column.COURSE_ID} = ? "

        return if (submissionId == -1L) {
            val sql = sqlPrefix + "ORDER BY ${DbStructureAdaptiveExp.Column.SOLVED_AT} DESC LIMIT 1"
            getAllWithQuery(sql, arrayOf(courseId.toString()))
        } else {
            val sql = sqlPrefix + "AND ${DbStructureAdaptiveExp.Column.SUBMISSION_ID} = ?"
            getAllWithQuery(sql, arrayOf(courseId.toString(), submissionId.toString()))
        }.firstOrNull()
    }

    override fun getExpForCourse(courseId: Long): Long {
        val sql =
                "SELECT IFNULL(SUM(${DbStructureAdaptiveExp.Column.EXP}), 0) as ${DbStructureAdaptiveExp.Column.EXP} " +
                "FROM $dbName " +
                "WHERE ${DbStructureAdaptiveExp.Column.COURSE_ID} = ?"

        return rawQuery(sql, arrayOf(courseId.toString())) {
            it.moveToFirst()

            return@rawQuery if (!it.isAfterLast) {
                it.getLong(it.getColumnIndex(DbStructureAdaptiveExp.Column.EXP))
            } else 0
        }
    }
}