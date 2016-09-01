package org.stepic.droid.core.presenters.contracts;

import org.stepic.droid.model.Course;

import java.util.List;

public interface CoursesView {
    void showLoading();

    void showEmptyCourses();

    void showConnectionProblem();

    void showCourses (List<Course> courses);
}
