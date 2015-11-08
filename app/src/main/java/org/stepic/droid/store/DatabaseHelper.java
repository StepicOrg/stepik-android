package org.stepic.droid.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.stepic.droid.store.structure.DBStructureCourses;
import org.stepic.droid.store.structure.DbStructureBlock;
import org.stepic.droid.store.structure.DbStructureCachedVideo;
import org.stepic.droid.store.structure.DbStructureLesson;
import org.stepic.droid.store.structure.DbStructureSections;
import org.stepic.droid.store.structure.DbStructureSharedDownloads;
import org.stepic.droid.store.structure.DbStructureStep;
import org.stepic.droid.store.structure.DbStructureUnit;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, DBStructureCourses.FILE_NAME, null, DBStructureCourses.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createCourseTable(db, DBStructureCourses.ENROLLED_COURSES);
        createCourseTable(db, DBStructureCourses.FEATURED_COURSES);
        createSectionTable(db, DbStructureSections.SECTIONS);
        createCachedVideoTable(db, DbStructureCachedVideo.CACHED_VIDEO);
        createUnitsDb(db, DbStructureUnit.UNITS);
        createLessonsDb(db, DbStructureLesson.LESSONS);
        createStepsDb(db, DbStructureStep.STEPS);
        createBlocksDb(db, DbStructureBlock.BLOCKS);
        createShareDownloads(db, DbStructureSharedDownloads.SHARED_DOWNLOADS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //todo: remake to incremental db
        db.execSQL("DROP TABLE IF EXISTS " + DBStructureCourses.ENROLLED_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + DBStructureCourses.FEATURED_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + DbStructureSections.SECTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + DbStructureCachedVideo.CACHED_VIDEO);
        db.execSQL("DROP TABLE IF EXISTS " + DbStructureUnit.UNITS);
        db.execSQL("DROP TABLE IF EXISTS " + DbStructureLesson.LESSONS);
        db.execSQL("DROP TABLE IF EXISTS " + DbStructureStep.STEPS);
        db.execSQL("DROP TABLE IF EXISTS " + DbStructureBlock.BLOCKS);
        db.execSQL("DROP TABLE IF EXISTS " + DbStructureSharedDownloads.SHARED_DOWNLOADS);
        onCreate(db);
    }

    private void createCourseTable(SQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DBStructureCourses.Column.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBStructureCourses.Column.COURSE_ID + " LONG, "
                + DBStructureCourses.Column.WORKLOAD + " TEXT, "
                + DBStructureCourses.Column.COVER_LINK + " TEXT, "
                + DBStructureCourses.Column.INTRO_LINK_VIMEO + " TEXT, "
                + DBStructureCourses.Column.COURSE_FORMAT + " TEXT, "
                + DBStructureCourses.Column.TARGET_AUDIENCE + " TEXT, "
                + DBStructureCourses.Column.INSTRUCTORS + " TEXT, "
                + DBStructureCourses.Column.REQUIREMENTS + " TEXT, "
                + DBStructureCourses.Column.DESCRIPTION + " TEXT, "
                + DBStructureCourses.Column.SECTIONS + " TEXT, "
                + DBStructureCourses.Column.TOTAL_UNITS + " INTEGER, "
                + DBStructureCourses.Column.ENROLLMENT + " INTEGER, "
                + DBStructureCourses.Column.IS_FEATURED + " BOOLEAN, "
                + DBStructureCourses.Column.OWNER + " LONG, "
                + DBStructureCourses.Column.IS_CONTEST + " BOOLEAN, "
                + DBStructureCourses.Column.LANGUAGE + " TEXT, "
                + DBStructureCourses.Column.IS_PUBLIC + " BOOLEAN, "
                + DBStructureCourses.Column.IS_CACHED + " BOOLEAN, "
                + DBStructureCourses.Column.TITLE + " TEXT, "
                + DBStructureCourses.Column.SLUG + " TEXT, "
                + DBStructureCourses.Column.SUMMARY + " TEXT, "
                + DBStructureCourses.Column.BEGIN_DATE_SOURCE + " TEXT, "
                + DBStructureCourses.Column.LAST_DEADLINE + " TEXT "
                + ")";
        db.execSQL(sql);
    }

    private void createSectionTable(SQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureSections.Column.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbStructureSections.Column.SECTION_ID + " LONG, "
                + DbStructureSections.Column.COURSE + " LONG, "
                + DbStructureSections.Column.UNITS + " TEXT, "
                + DbStructureSections.Column.PROGRESS + " TEXT, "
                + DbStructureSections.Column.POSITION + " INTEGER, "
                + DbStructureSections.Column.TITLE + " TEXT, "
                + DbStructureSections.Column.SLUG + " TEXT, "
                + DbStructureSections.Column.BEGIN_DATE + " TEXT, "
                + DbStructureSections.Column.END_DATE + " TEXT, "
                + DbStructureSections.Column.SOFT_DEADLINE + " TEXT, "
                + DbStructureSections.Column.HARD_DEADLINE + " TEXT, "
                + DbStructureSections.Column.GRADING_POLICY + " TEXT, "
                + DbStructureSections.Column.BEGIN_DATE_SOURCE + " TEXT, "
                + DbStructureSections.Column.END_DATE_SOURCE + " TEXT, "
                + DbStructureSections.Column.SOFT_DEADLINE_SOURCE + " TEXT, "
                + DbStructureSections.Column.HARD_DEADLINE_SOURCE + " TEXT, "
                + DbStructureSections.Column.GRADING_POLICY_SOURCE + " TEXT, "
                + DbStructureSections.Column.IS_ACTIVE + " BOOLEAN, "
                + DbStructureSections.Column.IS_CACHED + " BOOLEAN, "
                + DbStructureSections.Column.CREATE_DATE + " TEXT, "
                + DbStructureSections.Column.UPDATE_DATE + " TEXT "

                + ")";
        db.execSQL(sql);
    }

    private void createCachedVideoTable(SQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureCachedVideo.Column.VIDEO_ID + " LONG, "
                + DbStructureCachedVideo.Column.STEP_ID + " LONG, "
                + DbStructureCachedVideo.Column.URL + " TEXT, "
                + DbStructureCachedVideo.Column.THUMBNAIL + " TEXT "

                + ")";
        db.execSQL(sql);
    }

    private void createUnitsDb(SQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureUnit.Column.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbStructureUnit.Column.UNIT_ID + " LONG, "
                + DbStructureUnit.Column.SECTION + " LONG, "
                + DbStructureUnit.Column.LESSON + " LONG, "
                + DbStructureUnit.Column.ASSIGNMENTS + " TEXT, "
                + DbStructureUnit.Column.POSITION + " INTEGER, "
                + DbStructureUnit.Column.PROGRESS + " TEXT, "
                + DbStructureUnit.Column.BEGIN_DATE + " TEXT, "
                + DbStructureUnit.Column.END_DATE + " TEXT, "
                + DbStructureUnit.Column.SOFT_DEADLINE + " TEXT, "
                + DbStructureUnit.Column.HARD_DEADLINE + " TEXT, "
                + DbStructureUnit.Column.GRADING_POLICY + " TEXT, "
                + DbStructureUnit.Column.BEGIN_DATE_SOURCE + " TEXT, "
                + DbStructureUnit.Column.END_DATE_SOURCE + " TEXT, "
                + DbStructureUnit.Column.SOFT_DEADLINE_SOURCE + " TEXT, "
                + DbStructureUnit.Column.HARD_DEADLINE_SOURCE + " TEXT, "
                + DbStructureUnit.Column.GRADING_POLICY_SOURCE + " TEXT, "
                + DbStructureUnit.Column.IS_ACTIVE + " BOOLEAN, "
                + DbStructureUnit.Column.CREATE_DATE + " TEXT, "
                + DbStructureUnit.Column.IS_CACHED + " BOOLEAN, "
                + DbStructureUnit.Column.UPDATE_DATE + " TEXT "

                + ")";
        db.execSQL(sql);
    }

    private void createLessonsDb(SQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureLesson.Column.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbStructureLesson.Column.LESSON_ID + " LONG, "
                + DbStructureLesson.Column.STEPS + " TEXT, "
                + DbStructureLesson.Column.IS_FEATURED + " BOOLEAN, "
                + DbStructureLesson.Column.IS_PRIME + " BOOLEAN, "
                + DbStructureLesson.Column.PROGRESS + " TEXT, "
                + DbStructureLesson.Column.OWNER + " INTEGER, "
                + DbStructureLesson.Column.SUBSCRIPTIONS + " TEXT, "
                + DbStructureLesson.Column.VIEWED_BY + " INTEGER, "
                + DbStructureLesson.Column.PASSED_BY + " INTEGER, "
                + DbStructureLesson.Column.DEPENDENCIES + " TEXT, "
                + DbStructureLesson.Column.IS_PUBLIC + " BOOLEAN, "
                + DbStructureLesson.Column.TITLE + " TEXT, "
                + DbStructureLesson.Column.SLUG + " TEXT, "
                + DbStructureLesson.Column.CREATE_DATE + " TEXT, "
                + DbStructureLesson.Column.LEARNERS_GROUP + " TEXT, "
                + DbStructureLesson.Column.IS_CACHED + " BOOLEAN, "
                + DbStructureLesson.Column.TEACHER_GROUP + " TEXT "

                + ")";
        db.execSQL(sql);
    }

    private void createStepsDb(SQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureStep.Column.STEP_ID + " LONG, "
                + DbStructureStep.Column.LESSON_ID + " LONG, "
                + DbStructureStep.Column.STATUS + " TEXT, "
                + DbStructureStep.Column.PROGRESS + " TEXT, "
                + DbStructureStep.Column.SUBSCRIPTIONS + " TEXT, "
                + DbStructureStep.Column.VIEWED_BY + " LONG, "
                + DbStructureStep.Column.PASSED_BY + " LONG, "
                + DbStructureStep.Column.POSITION + " LONG, "
                + DbStructureStep.Column.CREATE_DATE + " TEXT, "
                + DbStructureStep.Column.IS_CACHED + " BOOLEAN, "
                + DbStructureStep.Column.UPDATE_DATE + " TEXT "
                + ")";
        db.execSQL(sql);
    }

    private void createBlocksDb(SQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureBlock.Column.STEP_ID + " LONG, "
                + DbStructureBlock.Column.NAME + " TEXT, "
                + DbStructureBlock.Column.TEXT + " TEXT "
                    + ")";
        db.execSQL(sql);
    }

    private void createShareDownloads(SQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureSharedDownloads.Column.DOWNLOAD_ID + " LONG, "
                + DbStructureSharedDownloads.Column.STEP_ID + " LONG, "
                + DbStructureSharedDownloads.Column.VIDEO_ID + " LONG "
                + ")";
        db.execSQL(sql);
    }

}
