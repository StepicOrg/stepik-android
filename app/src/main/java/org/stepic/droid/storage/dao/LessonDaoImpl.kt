package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import com.google.gson.Gson
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.DbParseHelper
import org.stepic.droid.util.getBoolean
import org.stepic.droid.util.getDate
import org.stepic.droid.util.getLong
import org.stepic.droid.util.getString
import org.stepic.droid.util.toObject
import org.stepik.android.cache.lesson.structure.DbStructureLesson
import org.stepik.android.model.Lesson
import javax.inject.Inject

class LessonDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations,
    private val gson: Gson
) : DaoBase<Lesson>(databaseOperations) {
    public override fun getDbName() =
        DbStructureLesson.TABLE_NAME

    public override fun getDefaultPrimaryColumn(): String =
        DbStructureLesson.Columns.ID

    public override fun getDefaultPrimaryValue(persistentObject: Lesson): String =
        persistentObject.id.toString()

    public override fun parsePersistentObject(cursor: Cursor): Lesson =
        Lesson(
            id = cursor.getLong(DbStructureLesson.Columns.ID),
            title = cursor.getString(DbStructureLesson.Columns.TITLE),
            slug = cursor.getString(DbStructureLesson.Columns.SLUG),
            coverUrl = cursor.getString(DbStructureLesson.Columns.COVER_URL),
            courses = DbParseHelper.parseStringToLongArray(cursor.getString(DbStructureLesson.Columns.COURSES)) ?: longArrayOf(),
            steps = DbParseHelper.parseStringToLongArray(cursor.getString(DbStructureLesson.Columns.STEPS)) ?: longArrayOf(),
            actions = cursor.getString(DbStructureLesson.Columns.ACTIONS)?.toObject(gson),
            isFeatured = cursor.getBoolean(DbStructureLesson.Columns.IS_FEATURED),
            progress = cursor.getString(DbStructureLesson.Columns.PROGRESS),
            owner = cursor.getLong(DbStructureLesson.Columns.OWNER),
            subscriptions = DbParseHelper.parseStringToStringArray(cursor.getString(DbStructureLesson.Columns.SUBSCRIPTIONS)),
            viewedBy = cursor.getLong(DbStructureLesson.Columns.VIEWED_BY),
            passedBy = cursor.getLong(DbStructureLesson.Columns.PASSED_BY),
            voteDelta = cursor.getLong(DbStructureLesson.Columns.VOTE_DELTA),
            language = cursor.getString(DbStructureLesson.Columns.LANGUAGE),
            isPublic = cursor.getBoolean(DbStructureLesson.Columns.IS_PUBLIC),
            createDate = cursor.getDate(DbStructureLesson.Columns.CREATE_DATE),
            updateDate = cursor.getDate(DbStructureLesson.Columns.UPDATE_DATE),
            learnersGroup = cursor.getString(DbStructureLesson.Columns.LEARNERS_GROUP),
            teachersGroup = cursor.getString(DbStructureLesson.Columns.TEACHERS_GROUP),
            timeToComplete = cursor.getLong(DbStructureLesson.Columns.TIME_TO_COMPLETE)
        )

    public override fun getContentValues(lesson: Lesson): ContentValues {
        val values = ContentValues()

        values.put(DbStructureLesson.Columns.ID, lesson.id)
        values.put(DbStructureLesson.Columns.TITLE, lesson.title)
        values.put(DbStructureLesson.Columns.SLUG, lesson.slug)
        values.put(DbStructureLesson.Columns.COVER_URL, lesson.coverUrl)
        values.put(DbStructureLesson.Columns.COURSES, DbParseHelper.parseLongArrayToString(lesson.courses))
        values.put(DbStructureLesson.Columns.STEPS, DbParseHelper.parseLongArrayToString(lesson.steps))
        values.put(DbStructureLesson.Columns.ACTIONS, gson.toJson(lesson.actions))
        values.put(DbStructureLesson.Columns.IS_FEATURED, lesson.isFeatured)
        values.put(DbStructureLesson.Columns.PROGRESS, lesson.progress)
        values.put(DbStructureLesson.Columns.OWNER, lesson.owner)
        values.put(DbStructureLesson.Columns.SUBSCRIPTIONS, DbParseHelper.parseStringArrayToString(lesson.subscriptions))
        values.put(DbStructureLesson.Columns.VIEWED_BY, lesson.viewedBy)
        values.put(DbStructureLesson.Columns.PASSED_BY, lesson.passedBy)
        values.put(DbStructureLesson.Columns.VOTE_DELTA, lesson.voteDelta)
        values.put(DbStructureLesson.Columns.LANGUAGE, lesson.language)
        values.put(DbStructureLesson.Columns.IS_PUBLIC, lesson.isPublic)
        values.put(DbStructureLesson.Columns.CREATE_DATE, lesson.createDate?.time ?: -1)
        values.put(DbStructureLesson.Columns.UPDATE_DATE, lesson.updateDate?.time ?: -1)
        values.put(DbStructureLesson.Columns.LEARNERS_GROUP, lesson.learnersGroup)
        values.put(DbStructureLesson.Columns.TEACHERS_GROUP, lesson.teachersGroup)
        values.put(DbStructureLesson.Columns.TIME_TO_COMPLETE, lesson.timeToComplete)

        return values
    }
}
