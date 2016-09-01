package org.stepic.droid.core.presenters.contracts;

public interface PersistentCourseListView {
    void showLoading();

    void showEmptyCourses();

    void showConnectionProblem();
}
