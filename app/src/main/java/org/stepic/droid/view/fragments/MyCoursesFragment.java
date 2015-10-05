package org.stepic.droid.view.fragments;

import com.squareup.otto.Subscribe;

import org.stepic.droid.events.FailCoursesDownloadEvent;
import org.stepic.droid.events.FinishingGetCoursesFromDbEvent;
import org.stepic.droid.events.FinishingSaveCoursesToDbEvent;
import org.stepic.droid.events.GettingCoursesFromDbSuccessEvent;
import org.stepic.droid.events.StartingGetCoursesFromDbEvent;
import org.stepic.droid.events.StartingSaveCoursesToDbEvent;
import org.stepic.droid.events.SuccessCoursesDownloadEvent;
import org.stepic.droid.store.operations.DbOperationsCourses;

public class MyCoursesFragment extends CoursesFragmentBase {
    @Override
    public void onStart() {
        super.onStart();
        mTypeOfCourse = DbOperationsCourses.Table.enrolled;
    }


    @Override
    @Subscribe
    public void onFailureDataLoad(FailCoursesDownloadEvent e) {
        super.onFailureDataLoad(e);
    }

    @Override
    @Subscribe
    public void onStartingSaveToDb(StartingSaveCoursesToDbEvent e) {
        super.onStartingSaveToDb(e);
    }

    @Override
    @Subscribe
    public void onFinishingSaveToDb(FinishingSaveCoursesToDbEvent e) {
        super.onFinishingSaveToDb(e);
    }

    @Override
    @Subscribe
    public void onStartingGetFromDb(StartingGetCoursesFromDbEvent e) {
        super.onStartingGetFromDb(e);
    }

    @Override
    @Subscribe
    public void onFinishingGetFromDb(FinishingGetCoursesFromDbEvent e) {
        super.onFinishingGetFromDb(e);
    }

    @Subscribe
    public void onGettingFromDbSuccess(GettingCoursesFromDbSuccessEvent e) {
        super.onGettingFromDbSuccess(e);
    }

    @Subscribe
    @Override
    public void onSuccessDataLoad(SuccessCoursesDownloadEvent e) {
        super.onSuccessDataLoad(e);
    }
}
