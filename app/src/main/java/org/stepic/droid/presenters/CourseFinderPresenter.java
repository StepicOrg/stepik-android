package org.stepic.droid.presenters;

import org.stepic.droid.view.abstraction.LoadCourseView;

public interface CourseFinderPresenter {
    void onCreate(LoadCourseView view);
    void onDestroy();

    void findCourseById(long courseId);
}
