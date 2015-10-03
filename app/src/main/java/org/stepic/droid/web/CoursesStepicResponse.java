package org.stepic.droid.web;

import org.stepic.droid.model.Course;
import org.stepic.droid.model.Meta;

import java.util.List;

public class CoursesStepicResponse extends StepicResponseBase {
    private List<Course> courses;

    public CoursesStepicResponse(List<Course> courses, Meta meta) {
        super(meta);
        this.courses = courses;
    }

    public List<Course> getCourses() {
        return courses;
    }
}
