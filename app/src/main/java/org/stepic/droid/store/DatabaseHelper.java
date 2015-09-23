package org.stepic.droid.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, DBCoursesStructure.FILE_NAME, null, DBCoursesStructure.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createCourseTable(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void createCourseTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + DBCoursesStructure.NAME
                + " ("
                + DBCoursesStructure.Column.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBCoursesStructure.Column.COURSE_ID + " INTEGER, "
                + DBCoursesStructure.Column.SUMMARY + " TEXT, "
                + DBCoursesStructure.Column.WORKLOAD + " TEXT, "
                + DBCoursesStructure.Column.COVER_LINK + " TEXT, "
                + DBCoursesStructure.Column.INTRO_LINK_VIMEO + " TEXT, "
                + DBCoursesStructure.Column.COURSE_FORMAT + " TEXT, "
                + DBCoursesStructure.Column.TARGET_AUDIENCE + " TEXT, "
                + DBCoursesStructure.Column.INSTRUCTORS + " TEXT, " //todo: remake to other db
                + DBCoursesStructure.Column.REQUIREMENTS + " TEXT, "
                + DBCoursesStructure.Column.DESCRIPTION + " TEXT, "
                + DBCoursesStructure.Column.SECTIONS + " TEXT, " //todo: remake to other db
                + DBCoursesStructure.Column.TOTAL_UNITS + " INTEGER, "
                + DBCoursesStructure.Column.ENROLLMENT + " INTEGER, "
                + DBCoursesStructure.Column.IS_FEATURED + " BOOLEAN, "
                + DBCoursesStructure.Column.OWNER + " LONG, "
                + DBCoursesStructure.Column.IS_CONTEST + " BOOLEAN, "
                + DBCoursesStructure.Column.LANGUAGE + " TEXT, "
                + DBCoursesStructure.Column.IS_PUBLIC + " BOOLEAN, "
                + DBCoursesStructure.Column.TITLE + " TEXT, "
                + DBCoursesStructure.Column.SLUG + " TEXT, "
                + DBCoursesStructure.Column.BEGIN_DATE_SOURCE + " TEXT, "
                + DBCoursesStructure.Column.LAST_DEADLINE + " TEXT "
                + ")";
        db.execSQL(sql);
    }
}
