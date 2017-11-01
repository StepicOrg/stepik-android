package org.stepic.droid.core;

import org.stepic.droid.model.Course;
import org.stepic.droid.storage.operations.Table;

import java.util.List;

public interface FilterApplicator {
    List<Course> getFilteredFeaturedFromSharedPrefs(List<Course> sourceCourses);

    List<Course> getFilteredFeaturedFromDefault(List<Course> sourceCourses);
}
