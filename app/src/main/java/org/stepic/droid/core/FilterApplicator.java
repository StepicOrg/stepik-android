package org.stepic.droid.core;

import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DatabaseFacade;

import java.util.List;

public interface FilterApplicator {
    List<Course> getFilteredFromSharedPrefs(List<Course> sourceCourses, DatabaseFacade.Table filterType);

    List<Course> getFilteredFromDefault(List<Course> sourceCourses, DatabaseFacade.Table filterType);
}
