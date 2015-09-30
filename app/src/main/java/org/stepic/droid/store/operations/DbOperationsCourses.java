package org.stepic.droid.store.operations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.stepic.droid.model.Course;
import org.stepic.droid.store.DbParseHelper;
import org.stepic.droid.store.structure.DBStructureCourses;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

@Singleton
public final class DbOperationsCourses extends DbOperationsBase {
    public DbOperationsCourses(Context context, Table type) {
        super(context);
        mType = type;
    }

    public enum Table {
        enrolled(DBStructureCourses.ENROLLED_COURSES),
        featured(DBStructureCourses.FEATURED_COURSES);


        private String description;

        Table(String description) {
            this.description = description;
        }

        private String getStoreName() {
            return description;
        }
    }

    private Table mType;

    public void addCourse(Course course) {
        ContentValues values = new ContentValues();

        values.put(DBStructureCourses.Column.COURSE_ID, course.getCourseId());
        values.put(DBStructureCourses.Column.SUMMARY, course.getSummary());
        values.put(DBStructureCourses.Column.COVER_LINK, course.getCover());
        values.put(DBStructureCourses.Column.INTRO_LINK_VIMEO, course.getIntro());
        values.put(DBStructureCourses.Column.TITLE, course.getTitle());
        values.put(DBStructureCourses.Column.LANGUAGE, course.getLanguage());
        values.put(DBStructureCourses.Column.BEGIN_DATE_SOURCE, course.getBegin_date_source());
        values.put(DBStructureCourses.Column.LAST_DEADLINE, course.getLast_deadline());
        values.put(DBStructureCourses.Column.DESCRIPTION, course.getDescription());

        String instructorsParsed = DbParseHelper.parseLongArrayToString(course.getInstructors());

        values.put(DBStructureCourses.Column.INSTRUCTORS, instructorsParsed);

        database.insert(mType.getStoreName(), null, values);
    }

    public void deleteCourse(Course course) {
        long courseId = course.getCourseId();
        database.delete(mType.getStoreName(),
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

    public void clearCache() {
        List<Course> courses = getAllCourses();
        for (Course courseItem : courses) {
            deleteCourse(courseItem);
        }
    }

    private Course parseCourse(Cursor cursor) {
        Course course = new Course();
        //ignore id of table
        int columnNumber = 1;
        course.setId(cursor.getLong(columnNumber++));
        course.setSummary(cursor.getString(columnNumber++));
        course.setCover(cursor.getString(columnNumber++));
        course.setIntro(cursor.getString(columnNumber++));
        course.setTitle(cursor.getString(columnNumber++));
        course.setLanguage(cursor.getString(columnNumber++));
        course.setBegin_date_source(cursor.getString(columnNumber++));
        course.setLast_deadline(cursor.getString(columnNumber++));
        course.setDescription(cursor.getString(columnNumber++));
        course.setInstructors(DbParseHelper.parseStringToLongArray(cursor.getString(columnNumber++)));

        return course;
    }

    @Override
    public Cursor getCursor() {
        return database.query(mType.getStoreName(), DBStructureCourses.getUsedColumns(),
                null, null, null, null, null);
    }

    public boolean isCourseInDB(Course course) {
        String Query = "Select * from " + mType.getStoreName() + " where " + DBStructureCourses.Column.COURSE_ID + " = " + course.getCourseId();
        Cursor cursor = database.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }
}
