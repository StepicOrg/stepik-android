package org.stepic.droid.ui.presenters.course_finder;

import org.stepic.droid.ui.abstraction.LoadCourseView;

public interface CourseFinderPresenter {
    void attachView(LoadCourseView view);
    void detachView();

    void findCourseById(long courseId);
}
