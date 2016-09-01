package org.stepic.droid.ui.fragments;

import com.squareup.otto.Subscribe;

import org.stepic.droid.base.CoursesDatabaseFragmentBase;
import org.stepic.droid.events.courses.FailDropCourseEvent;
import org.stepic.droid.events.courses.SuccessDropCourseEvent;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.store.operations.DatabaseFacade;

public class MyCoursesFragment extends CoursesDatabaseFragmentBase {

    public static MyCoursesFragment newInstance() {
        return new MyCoursesFragment();
    }

    @Override
    protected DatabaseFacade.Table getCourseType() {
        return DatabaseFacade.Table.enrolled;
    }

    @Subscribe
    @Override
    public void onSuccessDrop(SuccessDropCourseEvent e) {
        super.onSuccessDrop(e);
    }

    @Subscribe
    @Override
    public void onFailDrop(FailDropCourseEvent e) {
        super.onFailDrop(e);
    }


    @Subscribe
    @Override
    public void onSuccessJoin(SuccessJoinEvent e) {
        super.onSuccessJoin(e);
    }
}
