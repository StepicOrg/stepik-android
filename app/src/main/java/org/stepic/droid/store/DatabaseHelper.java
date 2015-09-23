package org.stepic.droid.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.stepic.droid.store.structure.DBStructureCourses;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, DBStructureCourses.FILE_NAME, null, DBStructureCourses.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createCourseTable(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //todo: remake to incremental db
        db.execSQL("DROP TABLE IF EXISTS " + DBStructureCourses.NAME);
        onCreate(db);

    }

    private void createCourseTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + DBStructureCourses.NAME
                + " ("
                + DBStructureCourses.Column.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBStructureCourses.Column.COURSE_ID + " INTEGER, "
                + DBStructureCourses.Column.SUMMARY + " TEXT, "
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
                + DBStructureCourses.Column.BEGIN_DATE_SOURCE + " TEXT, "
                + DBStructureCourses.Column.LAST_DEADLINE + " TEXT "
                + ")";
        db.execSQL(sql);
    }
}
