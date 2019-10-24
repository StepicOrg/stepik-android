package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.adaptive.model.AdaptiveWeekProgress
import org.stepic.droid.adaptive.model.LocalExpItem
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureAdaptiveExp
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import java.util.ArrayList
import java.util.Calendar
import javax.inject.Inject

class AdaptiveExpDaoImpl
@Inject
constructor(databaseOperations: DatabaseOperations) : DaoBase<LocalExpItem>(databaseOperations), AdaptiveExpDao {
    companion object {
        private const val FIELD_DAY = "day"
        private const val FIELD_WEEK = "week"
    }

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
            val sql = sqlPrefix +
                    "AND ${DbStructureAdaptiveExp.Column.SUBMISSION_ID} <> 0 " + // submission id = 0 only for syncing
                    "ORDER BY ${DbStructureAdaptiveExp.Column.SOLVED_AT} DESC LIMIT 1"
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

    override fun getExpForLast7Days(courseId: Long): LongArray {
        val sql =
                "SELECT " +
                        "STRFTIME('%Y %j', ${DbStructureAdaptiveExp.Column.SOLVED_AT}) as $FIELD_DAY, " +
                        "STRFTIME('%s', ${DbStructureAdaptiveExp.Column.SOLVED_AT}) as ${DbStructureAdaptiveExp.Column.SOLVED_AT}, " +
                        "SUM(${DbStructureAdaptiveExp.Column.EXP}) as ${DbStructureAdaptiveExp.Column.EXP} " +
                "FROM $dbName " +
                "WHERE ${DbStructureAdaptiveExp.Column.SOLVED_AT} >= (SELECT DATETIME('now', '-7 day')) " +
                        "AND ${DbStructureAdaptiveExp.Column.SUBMISSION_ID} <> 0 " +
                        "AND ${DbStructureAdaptiveExp.Column.COURSE_ID} = ? " +
                "GROUP BY $FIELD_DAY " +
                "ORDER BY $FIELD_DAY"

        return rawQuery(sql, arrayOf(courseId.toString())) {
            val res = LongArray(7) { 0 }

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val now = DateTimeHelper.calendarToLocalMillis(calendar) + AppConstants.MILLIS_IN_24HOURS

            if (it.moveToFirst()) {
                do {
                    val date = it.getLong(it.getColumnIndex(DbStructureAdaptiveExp.Column.SOLVED_AT)) * 1000
                    val day = ((now - date) / AppConstants.MILLIS_IN_24HOURS).toInt()

                    if (day in 0..6) {
                        res[6 - day] = it.getLong(it.getColumnIndex(DbStructureAdaptiveExp.Column.EXP))
                    }
                } while (it.moveToNext())
            }

            return@rawQuery res
        }
    }

    override fun getExpForWeeks(courseId: Long): List<AdaptiveWeekProgress> {
        val sql =
                "SELECT " +
                        "STRFTIME('%Y %W', ${DbStructureAdaptiveExp.Column.SOLVED_AT}) as $FIELD_WEEK, " +
                        "STRFTIME('%s', ${DbStructureAdaptiveExp.Column.SOLVED_AT}) as ${DbStructureAdaptiveExp.Column.SOLVED_AT}, " +
                        "SUM(${DbStructureAdaptiveExp.Column.EXP}) as ${DbStructureAdaptiveExp.Column.EXP} " +
                "FROM $dbName " +
                "WHERE ${DbStructureAdaptiveExp.Column.SUBMISSION_ID} <> 0 " +
                        "AND ${DbStructureAdaptiveExp.Column.COURSE_ID} = ? " +
                "GROUP BY $FIELD_WEEK " +
                "ORDER BY $FIELD_WEEK DESC"

        return rawQuery(sql, arrayOf(courseId.toString())) {
            val res = ArrayList<AdaptiveWeekProgress>()

            if (it.moveToFirst()) {
                do {
                    val w = it.getLong(it.getColumnIndex(DbStructureAdaptiveExp.Column.SOLVED_AT)) * 1000

                    val start = DateTimeHelper.calendarFromLocalMillis(w)
                    start.set(Calendar.DAY_OF_WEEK, start.firstDayOfWeek)

                    val end = DateTimeHelper.calendarFromLocalMillis(w)
                    end.set(Calendar.DAY_OF_WEEK, (end.firstDayOfWeek + 6) % 7)

                    res.add(AdaptiveWeekProgress(start, end, it.getLong(it.getColumnIndex(DbStructureAdaptiveExp.Column.EXP))))
                } while (it.moveToNext())
            }

            return@rawQuery res
        }
    }
}