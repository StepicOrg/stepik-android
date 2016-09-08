package org.stepic.droid.core.presenters.contracts;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.model.Course;

import java.util.List;

public interface FilterForCoursesView {
    void clearAndShowLoading();

    void showFilteredCourses(@NotNull List<Course> filteredList);
}
