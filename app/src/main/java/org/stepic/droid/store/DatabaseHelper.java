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
                + DBCoursesStructure.Column.USERNAME + " TEXT "
                + ")";
        db.execSQL(sql);
    }
}
