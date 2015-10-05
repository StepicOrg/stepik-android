package org.stepic.droid.events;

import org.stepic.droid.model.Course;

import java.util.List;

public class GettingFromDbSuccess {
    public List<Course> getCourses() {
        return courses;
    }

    private final List<Course> courses;

    public GettingFromDbSuccess(List<Course> courses) {
        this.courses = courses;
    }
}
