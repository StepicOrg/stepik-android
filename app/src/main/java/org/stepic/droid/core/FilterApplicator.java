package org.stepic.droid.core;

import org.stepik.android.model.Course;

import java.util.List;

public interface FilterApplicator {
    List<Course> filterCourses(List<Course> sourceCourses);
}
