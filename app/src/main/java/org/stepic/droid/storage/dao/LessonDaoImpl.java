package org.stepic.droid.storage.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.stepic.droid.model.Lesson;
import org.stepic.droid.storage.structure.DbStructureLesson;
import org.stepic.droid.util.DbParseHelper;

import javax.inject.Inject;

public class LessonDaoImpl extends DaoBase<Lesson> {

    @Inject
    public LessonDaoImpl(SQLiteDatabase openHelper) {
        super(openHelper);
    }

    @Override
    public Lesson parsePersistentObject(Cursor cursor) {
        Lesson lesson = new Lesson();
        int columnIndexLessonId = cursor.getColumnIndex(DbStructureLesson.Column.LESSON_ID);
        int columnIndexSteps = cursor.getColumnIndex(DbStructureLesson.Column.STEPS);
        int columnIndexIsFeatured = cursor.getColumnIndex(DbStructureLesson.Column.IS_FEATURED);
        int columnIndexIsPrime = cursor.getColumnIndex(DbStructureLesson.Column.IS_PRIME);
        int columnIndexProgress = cursor.getColumnIndex(DbStructureLesson.Column.PROGRESS);
        int columnIndexOwner = cursor.getColumnIndex(DbStructureLesson.Column.OWNER);
        int columnIndexSubscriptions = cursor.getColumnIndex(DbStructureLesson.Column.SUBSCRIPTIONS);
        int columnIndexViewedBy = cursor.getColumnIndex(DbStructureLesson.Column.VIEWED_BY);
        int columnIndexPassedBy = cursor.getColumnIndex(DbStructureLesson.Column.PASSED_BY);
        int columnIndexDependencies = cursor.getColumnIndex(DbStructureLesson.Column.DEPENDENCIES);
        int columnIndexIsPublic = cursor.getColumnIndex(DbStructureLesson.Column.IS_PUBLIC);
        int columnIndexTitle = cursor.getColumnIndex(DbStructureLesson.Column.TITLE);
        int columnIndexSlug = cursor.getColumnIndex(DbStructureLesson.Column.SLUG);
        int columnIndexCreateDate = cursor.getColumnIndex(DbStructureLesson.Column.CREATE_DATE);
        int columnIndexLearnersGroup = cursor.getColumnIndex(DbStructureLesson.Column.LEARNERS_GROUP);
        int columnIndexTeacherGroup = cursor.getColumnIndex(DbStructureLesson.Column.TEACHER_GROUP);
        int indexIsCached = cursor.getColumnIndex(DbStructureLesson.Column.IS_CACHED);
        int indexIsLoading = cursor.getColumnIndex(DbStructureLesson.Column.IS_LOADING);
        int indexCoverURL = cursor.getColumnIndex(DbStructureLesson.Column.COVER_URL);

        lesson.setId(cursor.getLong(columnIndexLessonId));
        lesson.setSteps(DbParseHelper.parseStringToLongArray(cursor.getString(columnIndexSteps)));
        lesson.set_featured(cursor.getInt(columnIndexIsFeatured) > 0);
        lesson.set_prime(cursor.getInt(columnIndexIsPrime) > 0);
        lesson.setProgress(cursor.getString(columnIndexProgress));
        lesson.setOwner(cursor.getInt(columnIndexOwner));
        lesson.setSubscriptions(DbParseHelper.parseStringToStringArray(cursor.getString(columnIndexSubscriptions)));
        lesson.setViewed_by(cursor.getInt(columnIndexViewedBy));
        lesson.setPassed_by(cursor.getInt(columnIndexPassedBy));
        lesson.setDependencies(DbParseHelper.parseStringToStringArray(cursor.getString(columnIndexDependencies)));
        lesson.set_public(cursor.getInt(columnIndexIsPublic) > 0);
        lesson.setTitle(cursor.getString(columnIndexTitle));
        lesson.setSlug(cursor.getString(columnIndexSlug));
        lesson.setCreate_date(cursor.getString(columnIndexCreateDate));
        lesson.setLearners_group(cursor.getString(columnIndexLearnersGroup));
        lesson.setTeacher_group(cursor.getString(columnIndexTeacherGroup));
        lesson.set_cached(cursor.getInt(indexIsCached) > 0);
        lesson.set_loading(cursor.getInt(indexIsLoading) > 0);
        lesson.setCover_url(cursor.getString(indexCoverURL));

        return lesson;
    }

    @Override
    public String getDbName() {
        return DbStructureLesson.LESSONS;
    }

    @Override
    public ContentValues getContentValues(Lesson lesson) {
        ContentValues values = new ContentValues();

        values.put(DbStructureLesson.Column.LESSON_ID, lesson.getId());
        values.put(DbStructureLesson.Column.STEPS, DbParseHelper.parseLongArrayToString(lesson.getSteps()));
        values.put(DbStructureLesson.Column.IS_FEATURED, lesson.is_featured());
        values.put(DbStructureLesson.Column.IS_PRIME, lesson.is_prime());
        values.put(DbStructureLesson.Column.PROGRESS, lesson.getProgress());
        values.put(DbStructureLesson.Column.OWNER, lesson.getOwner());
        values.put(DbStructureLesson.Column.SUBSCRIPTIONS, DbParseHelper.parseStringArrayToString(lesson.getSubscriptions()));
        values.put(DbStructureLesson.Column.VIEWED_BY, lesson.getViewed_by());
        values.put(DbStructureLesson.Column.PASSED_BY, lesson.getPassed_by());
        values.put(DbStructureLesson.Column.DEPENDENCIES, DbParseHelper.parseStringArrayToString(lesson.getDependencies()));
        values.put(DbStructureLesson.Column.IS_PUBLIC, lesson.is_public());
        values.put(DbStructureLesson.Column.TITLE, lesson.getTitle());
        values.put(DbStructureLesson.Column.SLUG, lesson.getSlug());
        values.put(DbStructureLesson.Column.CREATE_DATE, lesson.getCreate_date());
        values.put(DbStructureLesson.Column.LEARNERS_GROUP, lesson.getLearners_group());
        values.put(DbStructureLesson.Column.TEACHER_GROUP, lesson.getTeacher_group());
        values.put(DbStructureLesson.Column.COVER_URL, lesson.getCover_url());

        return values;
    }

    @Override
    public String getDefaultPrimaryColumn() {
        return DbStructureLesson.Column.LESSON_ID;
    }

    @Override
    public String getDefaultPrimaryValue(Lesson persistentObject) {
        return persistentObject.getId()+"";
    }
}
