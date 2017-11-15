package org.stepic.droid.core;

import org.stepic.droid.model.Course;

import java.util.List;

public interface FilterApplicator {
    List<Course> filterCourses(List<Course> sourceCourses);
}
