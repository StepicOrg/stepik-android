package org.stepic.droid.view.fragments;

import org.stepic.droid.store.operations.DbOperationsCourses;

public class FindCoursesFragment extends CoursesFragmentBase {

    @Override
    public void onStart() {
        super.onStart();

        mTypeOfCourse = DbOperationsCourses.Table.featured;
    }
}
