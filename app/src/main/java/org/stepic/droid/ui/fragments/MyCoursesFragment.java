package org.stepic.droid.ui.fragments;

import org.stepic.droid.base.CoursesDatabaseFragmentBase;
import org.stepic.droid.storage.operations.Table;

public class MyCoursesFragment extends CoursesDatabaseFragmentBase {

    public static MyCoursesFragment newInstance() {
        return new MyCoursesFragment();
    }

    @Override
    protected Table getCourseType() {
        return Table.enrolled;
    }

}
