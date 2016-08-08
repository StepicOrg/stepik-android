package org.stepic.droid.ui.presenters.course_joiner;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.model.Course;
import org.stepic.droid.ui.abstraction.CourseJoinView;

public interface CourseJoinerPresenter  {
    void attachView(CourseJoinView view);
    void detachView();
    void joinCourse(@NotNull Course mCourse);
}
