package org.stepic.droid.ui.presenters.course_finder;

import org.stepic.droid.ui.abstraction.LoadCourseView;
import org.stepic.droid.ui.presenters.Presenter;

public interface CourseFinderPresenter extends Presenter<LoadCourseView>{
    void findCourseById(long courseId);
}
