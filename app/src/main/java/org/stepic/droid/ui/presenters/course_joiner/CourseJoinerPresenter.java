package org.stepic.droid.ui.presenters.course_joiner;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.model.Course;
import org.stepic.droid.ui.abstraction.CourseJoinView;
import org.stepic.droid.ui.presenters.Presenter;

public interface CourseJoinerPresenter  extends Presenter<CourseJoinView>{
    void joinCourse(@NotNull Course mCourse);
}
