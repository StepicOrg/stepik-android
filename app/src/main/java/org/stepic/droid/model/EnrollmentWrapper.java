package org.stepic.droid.model;

public class EnrollmentWrapper {
    private Enrollment enrollment;

    public EnrollmentWrapper(long courseId) {
        enrollment = new Enrollment(courseId);
    }
}
