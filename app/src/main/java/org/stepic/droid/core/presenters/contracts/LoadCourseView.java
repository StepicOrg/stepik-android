package org.stepic.droid.core.presenters.contracts;

import org.stepic.droid.events.courses.CourseCantLoadEvent;
import org.stepic.droid.events.courses.CourseFoundEvent;
import org.stepic.droid.events.courses.CourseUnavailableForUserEvent;

public interface LoadCourseView {
    void onCourseFound(CourseFoundEvent event);

    void onCourseUnavailable(CourseUnavailableForUserEvent event);

    void onInternetFailWhenCourseIsTriedToLoad(CourseCantLoadEvent event);
}
