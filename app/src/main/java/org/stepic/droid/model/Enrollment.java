package org.stepic.droid.model;

public class Enrollment {
    private String course_id;
    private boolean has_course;
    private long course; // it is course id!

    public Enrollment(long courseId) {
        course = courseId;
        course_id = null;
        has_course = false;
    }
}
