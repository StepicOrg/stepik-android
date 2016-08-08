package org.stepic.droid.presenters.course_joiner;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.model.Course;
import org.stepic.droid.ui.abstraction.CourseJoinView;

public interface CourseJoinerPresenter {
    void onStart(CourseJoinView view);
    void onStop();
    void joinCourse(@NotNull Course mCourse);
}
