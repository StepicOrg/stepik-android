package org.stepic.droid.model;

public class Enrollment {
    private long course_id;
    private boolean has_course;
    private long course; // it is course id!

    public Enrollment(long courseId) {
        course = courseId; //it is real
        course_id = courseId;
        has_course = false;
    }
}
