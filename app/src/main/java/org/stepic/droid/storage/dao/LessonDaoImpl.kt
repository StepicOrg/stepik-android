package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.jsonHelpers.adapters.UTCDateAdapter

import org.stepik.android.model.Lesson
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureLesson
import org.stepic.droid.util.DbParseHelper

import javax.inject.Inject

class LessonDaoImpl
@Inject
constructor(
        databaseOperations: DatabaseOperations,
        private val dateAdapter: UTCDateAdapter
) : DaoBase<Lesson>(databaseOperations) {
    public override fun parsePersistentObject(cursor: Cursor): Lesson {
        val columnIndexLessonId = cursor.getColumnIndex(DbStructureLesson.Column.LESSON_ID)
        val columnIndexSteps = cursor.getColumnIndex(DbStructureLesson.Column.STEPS)
        val columnIndexIsFeatured = cursor.getColumnIndex(DbStructureLesson.Column.IS_FEATURED)
        val columnIndexIsPrime = cursor.getColumnIndex(DbStructureLesson.Column.IS_PRIME)
        val columnIndexProgress = cursor.getColumnIndex(DbStructureLesson.Column.PROGRESS)
        val columnIndexOwner = cursor.getColumnIndex(DbStructureLesson.Column.OWNER)
        val columnIndexSubscriptions = cursor.getColumnIndex(DbStructureLesson.Column.SUBSCRIPTIONS)
        val columnIndexViewedBy = cursor.getColumnIndex(DbStructureLesson.Column.VIEWED_BY)
        val columnIndexPassedBy = cursor.getColumnIndex(DbStructureLesson.Column.PASSED_BY)
        val columnIndexVoteDelta = cursor.getColumnIndex(DbStructureLesson.Column.VOTE_DELTA)
        val columnIndexDependencies = cursor.getColumnIndex(DbStructureLesson.Column.DEPENDENCIES)
        val columnIndexIsPublic = cursor.getColumnIndex(DbStructureLesson.Column.IS_PUBLIC)
        val columnIndexTitle = cursor.getColumnIndex(DbStructureLesson.Column.TITLE)
        val columnIndexSlug = cursor.getColumnIndex(DbStructureLesson.Column.SLUG)
        val columnIndexCreateDate = cursor.getColumnIndex(DbStructureLesson.Column.CREATE_DATE)
        val columnIndexLearnersGroup = cursor.getColumnIndex(DbStructureLesson.Column.LEARNERS_GROUP)
        val columnIndexTeacherGroup = cursor.getColumnIndex(DbStructureLesson.Column.TEACHER_GROUP)
        val indexCoverURL = cursor.getColumnIndex(DbStructureLesson.Column.COVER_URL)

        return Lesson(
                id = cursor.getLong(columnIndexLessonId),
                title = cursor.getString(columnIndexTitle),
                steps = DbParseHelper.parseStringToLongArray(cursor.getString(columnIndexSteps))
                        ?: longArrayOf(),
                isFeatured = cursor.getInt(columnIndexIsFeatured) > 0,
                isPrime = cursor.getInt(columnIndexIsPrime) > 0,
                progress = cursor.getString(columnIndexProgress),
                owner = cursor.getInt(columnIndexOwner),
                subscriptions = DbParseHelper.parseStringToStringArray(cursor.getString(columnIndexSubscriptions)),
                viewedBy = cursor.getInt(columnIndexViewedBy),
                passedBy = cursor.getInt(columnIndexPassedBy),
                voteDelta = cursor.getLong(columnIndexVoteDelta),
                dependencies = DbParseHelper.parseStringToStringArray(cursor.getString(columnIndexDependencies)),
                isPublic = cursor.getInt(columnIndexIsPublic) > 0,
                slug = cursor.getString(columnIndexSlug),
                createDate = dateAdapter.stringToDate(cursor.getString(columnIndexCreateDate)),
                learnersGroup = cursor.getString(columnIndexLearnersGroup),
                teacherGroup = cursor.getString(columnIndexTeacherGroup),
                coverUrl = cursor.getString(indexCoverURL)
        )
    }

    public override fun getDbName() = DbStructureLesson.LESSONS

    public override fun getContentValues(lesson: Lesson): ContentValues {
        val values = ContentValues()

        values.put(DbStructureLesson.Column.LESSON_ID, lesson.id)
        values.put(DbStructureLesson.Column.STEPS, DbParseHelper.parseLongArrayToString(lesson.steps))
        values.put(DbStructureLesson.Column.IS_FEATURED, lesson.isFeatured)
        values.put(DbStructureLesson.Column.IS_PRIME, lesson.isPrime)
        values.put(DbStructureLesson.Column.PROGRESS, lesson.progress)
        values.put(DbStructureLesson.Column.OWNER, lesson.owner)
        values.put(DbStructureLesson.Column.SUBSCRIPTIONS, DbParseHelper.parseStringArrayToString(lesson.subscriptions))
        values.put(DbStructureLesson.Column.VIEWED_BY, lesson.viewedBy)
        values.put(DbStructureLesson.Column.PASSED_BY, lesson.passedBy)
        values.put(DbStructureLesson.Column.VOTE_DELTA, lesson.voteDelta)
        values.put(DbStructureLesson.Column.DEPENDENCIES, DbParseHelper.parseStringArrayToString(lesson.dependencies))
        values.put(DbStructureLesson.Column.IS_PUBLIC, lesson.isPublic)
        values.put(DbStructureLesson.Column.TITLE, lesson.title)
        values.put(DbStructureLesson.Column.SLUG, lesson.slug)
        values.put(DbStructureLesson.Column.CREATE_DATE, dateAdapter.dateToString(lesson.createDate))
        values.put(DbStructureLesson.Column.LEARNERS_GROUP, lesson.learnersGroup)
        values.put(DbStructureLesson.Column.TEACHER_GROUP, lesson.teacherGroup)
        values.put(DbStructureLesson.Column.COVER_URL, lesson.coverUrl)

        return values
    }

    public override fun getDefaultPrimaryColumn(): String = DbStructureLesson.Column.LESSON_ID

    public override fun getDefaultPrimaryValue(persistentObject: Lesson): String =
            persistentObject.id.toString()
}
