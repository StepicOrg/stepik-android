package org.stepic.droid.store.operations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.stepic.droid.model.Course;
import org.stepic.droid.store.structure.DBStructureCourses;

import java.util.ArrayList;
import java.util.List;

public final class DbOperationsCourses extends DbOperationsBase {
    public DbOperationsCourses(Context context) {
        super(context);
    }

    public void addCourse (Course course) {
        ContentValues values = new ContentValues();

        values.put(DBStructureCourses.Column.COURSE_ID, course.getCourseId());
        values.put(DBStructureCourses.Column.SUMMARY, course.getSummary());
        values.put(DBStructureCourses.Column.COVER_LINK, course.getCover());
        values.put(DBStructureCourses.Column.INTRO_LINK_VIMEO, course.getIntro());
        values.put(DBStructureCourses.Column.TITLE, course.getTitle());
        values.put(DBStructureCourses.Column.LANGUAGE, course.getLanguage());

        database.insert(DBStructureCourses.NAME, null, values);
    }

    public void deleteCourse (Course course) {
        long courseId = course.getCourseId();
        database.delete(DBStructureCourses.NAME,
                DBStructureCourses.Column.COURSE_ID + " = " + courseId,
                null);
    }

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();

        Cursor cursor = getCursor();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Course course = parseCourse(cursor);
            courses.add(course);
            cursor.moveToNext();
        }

        cursor.close();
        return courses;
    }

    private Course parseCourse(Cursor cursor) {
        Course course = new Course();
        //ignore id of table
        int columnNumber = 1;
        course.setId(cursor.getLong(columnNumber++));
        course.setSummary(cursor.getString(columnNumber++));
        course.setCover(cursor.getColumnName(columnNumber++));
        course.setIntro(cursor.getColumnName(columnNumber++));
        course.setTitle(cursor.getColumnName(columnNumber++));

        course.setLanguage(cursor.getColumnName(columnNumber));

        return course;
    }

    @Override
    public Cursor getCursor() {
        return database.query(DBStructureCourses.NAME, DBStructureCourses.getUsedColumns(),
                null, null, null, null, null);
    }
}
