package org.stepic.droid.core;

import org.stepic.droid.model.Course;
import org.stepic.droid.storage.operations.Table;

import java.util.List;

public interface FilterApplicator {
    List<Course> getFilteredFromSharedPrefs(List<Course> sourceCourses, Table courseType);

    List<Course> getFilteredFromDefault(List<Course> sourceCourses, Table courseType);
}
