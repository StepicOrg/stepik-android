package org.stepic.droid.core;

import org.stepic.droid.core.presenters.FilterForCoursesPresenter;

import dagger.Module;

@Module
public class CourseListModule {
    @PerFragment
    public FilterForCoursesPresenter provideFilterForCoursePresenter() {
        return new FilterForCoursesPresenter();
    }
}
