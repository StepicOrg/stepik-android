package org.stepic.droid.presenters;

import org.stepic.droid.view.abstraction.LoadCourseView;

public interface CourseFinderPresenter {
    void onStart(LoadCourseView view);
    void onDestroy();

    void findCourseById(long courseId);
}
