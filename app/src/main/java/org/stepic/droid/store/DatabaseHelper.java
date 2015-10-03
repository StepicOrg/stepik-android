package org.stepic.droid.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.stepic.droid.store.structure.DBStructureCourses;
import org.stepic.droid.store.structure.DbStructureSections;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, DBStructureCourses.FILE_NAME, null, DBStructureCourses.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createCourseTable(db, DBStructureCourses.ENROLLED_COURSES);
        createCourseTable(db, DBStructureCourses.FEATURED_COURSES);
        createSectionTable(db, DbStructureSections.SECTIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //todo: remake to incremental db
        db.execSQL("DROP TABLE IF EXISTS " + DBStructureCourses.ENROLLED_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + DBStructureCourses.FEATURED_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + DbStructureSections.SECTIONS);
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
                + DBStructureCourses.Column.INSTRUCTORS + " TEXT, " //todo: remake to other db
                + DBStructureCourses.Column.REQUIREMENTS + " TEXT, "
                + DBStructureCourses.Column.DESCRIPTION + " TEXT, "
                + DBStructureCourses.Column.SECTIONS + " TEXT, " //todo: remake to other db
                + DBStructureCourses.Column.TOTAL_UNITS + " INTEGER, "
                + DBStructureCourses.Column.ENROLLMENT + " INTEGER, "
                + DBStructureCourses.Column.IS_FEATURED + " BOOLEAN, "
                + DBStructureCourses.Column.OWNER + " LONG, "
                + DBStructureCourses.Column.IS_CONTEST + " BOOLEAN, "
                + DBStructureCourses.Column.LANGUAGE + " TEXT, "
                + DBStructureCourses.Column.IS_PUBLIC + " BOOLEAN, "
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
                + DbStructureSections.Column.CREATE_DATE + " TEXT, "
                + DbStructureSections.Column.UPDATE_DATE + " TEXT "

                + ")";
        db.execSQL(sql);
    }
}
