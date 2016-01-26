package org.stepic.droid.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.stepic.droid.store.structure.DBStructureBase;
import org.stepic.droid.store.structure.DBStructureCourses;
import org.stepic.droid.store.structure.DbStructureAssignment;
import org.stepic.droid.store.structure.DbStructureBlock;
import org.stepic.droid.store.structure.DbStructureCachedVideo;
import org.stepic.droid.store.structure.DbStructureLesson;
import org.stepic.droid.store.structure.DbStructureProgress;
import org.stepic.droid.store.structure.DbStructureSections;
import org.stepic.droid.store.structure.DbStructureSharedDownloads;
import org.stepic.droid.store.structure.DbStructureStep;
import org.stepic.droid.store.structure.DbStructureUnit;
import org.stepic.droid.store.structure.DbStructureViewQueue;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = "TEXT";

    public DatabaseHelper(Context context) {
        super(context, DBStructureBase.FILE_NAME, null, DBStructureBase.VERSION);
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

        //from version 2:
        createAssignment(db, DbStructureAssignment.ASSIGNMENTS);
        createProgress(db, DbStructureProgress.PROGRESS);
        createViewQueue(db, DbStructureViewQueue.VIEW_QUEUE);


//Use new manner for upgrade, it is more safety and maintainability (but may be less effective in onCreate) :
        upgradeFrom3To4(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
//            update from 1 to 2
            createAssignment(db, DbStructureAssignment.ASSIGNMENTS);
            createProgress(db, DbStructureProgress.PROGRESS);
            createViewQueue(db, DbStructureViewQueue.VIEW_QUEUE);
        }

        if (oldVersion < 3) {
            //update from 2 to 3
            String upgradeToV3 =
                    "ALTER TABLE " + DbStructureCachedVideo.CACHED_VIDEO + " ADD COLUMN "
                            + DbStructureCachedVideo.Column.QUALITY + " TEXT ";
            db.execSQL(upgradeToV3);

            upgradeToV3 = "ALTER TABLE " + DbStructureSharedDownloads.SHARED_DOWNLOADS + " ADD COLUMN "
                    + DbStructureSharedDownloads.Column.QUALITY + " TEXT ";
            db.execSQL(upgradeToV3);


            //in release 0.6 we create progress table with score type = Text, but in database it was Integer, now rename it:
            //http://stackoverflow.com/questions/21199398/sqlite-alter-a-tables-column-type

            String tempTableName = "tmp2to3";
            String renameTableQuery = "ALTER TABLE " + DbStructureProgress.PROGRESS + " RENAME TO "
                    + tempTableName;
            db.execSQL(renameTableQuery);

            createProgress(db, DbStructureProgress.PROGRESS);

            String[] allFields = DbStructureProgress.getUsedColumns();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < allFields.length; i++) {
                sb.append(allFields[i]);
                if (i != allFields.length - 1) {
                    sb.append(", ");
                }
            }
            String fields_correct = sb.toString();
            String insertValues = "INSERT INTO " + DbStructureProgress.PROGRESS + "("
                    + fields_correct +
                    ")" +
                    "   SELECT " +
                    fields_correct +
                    "   FROM " + tempTableName;

            db.execSQL(insertValues);

            String drop = "DROP TABLE " + tempTableName;
            db.execSQL(drop);
        }
        if (oldVersion < 4) {
            upgradeFrom3To4(db);
        }
    }

    private void upgradeFrom3To4(SQLiteDatabase db) {
        alterColumn(db, DBStructureCourses.ENROLLED_COURSES, DBStructureCourses.Column.CERTIFICATE, TEXT_TYPE);
        alterColumn(db, DBStructureCourses.FEATURED_COURSES, DBStructureCourses.Column.CERTIFICATE, TEXT_TYPE);
    }

    private void alterColumn(SQLiteDatabase db, String dbName, String column, String type) {
        String upgrade = "ALTER TABLE " + dbName + " ADD COLUMN "
                + column + " " + type + " ";
        db.execSQL(upgrade);
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
                + DBStructureCourses.Column.IS_LOADING + " BOOLEAN, "
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
                + DbStructureSections.Column.IS_LOADING + " BOOLEAN, "
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
                + DbStructureCachedVideo.Column.QUALITY + " TEXT, "
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
                + DbStructureUnit.Column.IS_LOADING + " BOOLEAN, "
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
                + DbStructureLesson.Column.IS_LOADING + " BOOLEAN, "
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
                + DbStructureStep.Column.IS_LOADING + " BOOLEAN, "
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
                + DbStructureSharedDownloads.Column.THUMBNAIL + " TEXT, "
                + DbStructureSharedDownloads.Column.QUALITY + " TEXT, "
                + DbStructureSharedDownloads.Column.VIDEO_ID + " LONG "
                + ")";
        db.execSQL(sql);
    }

    private void createAssignment(SQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureAssignment.Column.ASSIGNMENT_ID + " LONG, "
                + DbStructureAssignment.Column.UNIT_ID + " LONG, "
                + DbStructureAssignment.Column.STEP_ID + " LONG, "
                + DbStructureAssignment.Column.PROGRESS + " TEXT, "
                + DbStructureAssignment.Column.CREATE_DATE + " TEXT, "
                + DbStructureAssignment.Column.UPDATE_DATE + " TEXT "
                + ")";
        db.execSQL(sql);
    }

    private void createProgress(SQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureProgress.Column.IS_PASSED + " BOOLEAN, "
                + DbStructureProgress.Column.ID + " TEXT, "
                + DbStructureProgress.Column.LAST_VIEWED + " TEXT, "
                + DbStructureProgress.Column.SCORE + " TEXT, "
                + DbStructureProgress.Column.COST + " INTEGER, "
                + DbStructureProgress.Column.N_STEPS + " INTEGER, "
                + DbStructureProgress.Column.N_STEPS_PASSED + " INTEGER "
                + ")";
        db.execSQL(sql);
    }

    private void createViewQueue(SQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureViewQueue.Column.STEP_ID + " LONG, "
                + DbStructureViewQueue.Column.ASSIGNMENT_ID + " LONG "
                + ")";
        db.execSQL(sql);
    }

}
