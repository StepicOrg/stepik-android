package org.stepic.droid.view.fragments;

import com.squareup.otto.Subscribe;

import org.stepic.droid.events.FailCoursesDownloadEvent;
import org.stepic.droid.events.FinishingGetFromDbEvent;
import org.stepic.droid.events.FinishingSaveToDbEvent;
import org.stepic.droid.events.GettingFromDbSuccess;
import org.stepic.droid.events.StartingGetFromDbEvent;
import org.stepic.droid.events.StartingSaveToDbEvent;
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
    public void onStartingSaveToDb(StartingSaveToDbEvent e) {
        super.onStartingSaveToDb(e);
    }

    @Override
    @Subscribe
    public void onFinishingSaveToDb(FinishingSaveToDbEvent e) {
        super.onFinishingSaveToDb(e);
    }

    @Override
    @Subscribe
    public void onStartingGetFromDb(StartingGetFromDbEvent e) {
        super.onStartingGetFromDb(e);
    }

    @Override
    @Subscribe
    public void onFinishingGetFromDb(FinishingGetFromDbEvent e) {
        super.onFinishingGetFromDb(e);
    }

    @Subscribe
    public void onGettingFromDbSuccess(GettingFromDbSuccess e) {
        super.onGettingFromDbSuccess(e);
    }

    @Subscribe
    @Override
    public void onSuccessDataLoad(SuccessCoursesDownloadEvent e) {
        super.onSuccessDataLoad(e);
    }
}
