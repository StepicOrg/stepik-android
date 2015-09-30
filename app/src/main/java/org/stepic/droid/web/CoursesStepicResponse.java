package org.stepic.droid.web;

import org.stepic.droid.model.Course;
import org.stepic.droid.model.Meta;

import java.util.List;

public class CoursesStepicResponse implements IStepicResponse {
    private List<Course> courses;
    private Meta meta;

    public CoursesStepicResponse (List<Course> courses, Meta meta) {
        this.courses = courses;
        this.meta = meta;
    }

    public List<Course> getCourses() {
        return courses;
    }
    public Meta getMeta() {
        return meta;
    }
}
