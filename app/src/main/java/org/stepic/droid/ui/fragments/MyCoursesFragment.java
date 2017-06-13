package org.stepic.droid.ui.fragments;

import com.squareup.otto.Subscribe;

import org.stepic.droid.base.CoursesDatabaseFragmentBase;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.storage.operations.Table;

public class MyCoursesFragment extends CoursesDatabaseFragmentBase {

    public static MyCoursesFragment newInstance() {
        return new MyCoursesFragment();
    }

    @Override
    protected Table getCourseType() {
        return Table.enrolled;
    }

    @Subscribe
    @Override
    public void onSuccessJoin(SuccessJoinEvent e) {
        super.onSuccessJoin(e);
    }
}
