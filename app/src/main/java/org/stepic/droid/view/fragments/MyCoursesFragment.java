package org.stepic.droid.view.fragments;

import android.os.Bundle;

import com.squareup.otto.Subscribe;

import org.stepic.droid.events.courses.FailCoursesDownloadEvent;
import org.stepic.droid.events.courses.FinishingGetCoursesFromDbEvent;
import org.stepic.droid.events.courses.FinishingSaveCoursesToDbEvent;
import org.stepic.droid.events.courses.GettingCoursesFromDbSuccessEvent;
import org.stepic.droid.events.courses.StartingGetCoursesFromDbEvent;
import org.stepic.droid.events.courses.StartingSaveCoursesToDbEvent;
import org.stepic.droid.events.courses.SuccessCoursesDownloadEvent;
import org.stepic.droid.store.operations.DbOperationsCourses;
import org.stepic.droid.util.AppConstants;

public class MyCoursesFragment extends CoursesFragmentBase {


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (AppConstants.WAS_SWIPED_TO_REFRESH_MY_COURSES) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    getAndShowDataFromCache();
                }
            });
        } else {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    downloadData();
                }
            });
            AppConstants.WAS_SWIPED_TO_REFRESH_MY_COURSES = true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mTypeOfCourse = DbOperationsCourses.Table.enrolled;
    }


    @Override
    @Subscribe
    public void onFailureDataLoad(FailCoursesDownloadEvent e) {
        if (e.getType() == mTypeOfCourse)
            super.onFailureDataLoad(e);
    }

    @Override
    @Subscribe
    public void onStartingSaveToDb(StartingSaveCoursesToDbEvent e) {
        if (e.getType() == mTypeOfCourse)
            super.onStartingSaveToDb(e);
    }

    @Override
    @Subscribe
    public void onFinishingSaveToDb(FinishingSaveCoursesToDbEvent e) {
        if (e.getType() == mTypeOfCourse)
            super.onFinishingSaveToDb(e);
    }

    @Override
    @Subscribe
    public void onStartingGetFromDb(StartingGetCoursesFromDbEvent e) {
        if (e.getType() == mTypeOfCourse)
            super.onStartingGetFromDb(e);
    }

    @Override
    @Subscribe
    public void onFinishingGetFromDb(FinishingGetCoursesFromDbEvent e) {
        if (e.getType() == mTypeOfCourse)
            super.onFinishingGetFromDb(e);
    }

    @Subscribe
    public void onGettingFromDbSuccess(GettingCoursesFromDbSuccessEvent e) {
        if (e.getType() == mTypeOfCourse)
            super.onGettingFromDbSuccess(e);
    }

    @Subscribe
    @Override
    public void onSuccessDataLoad(SuccessCoursesDownloadEvent e) {
        if (e.getType() == mTypeOfCourse)
            super.onSuccessDataLoad(e);
    }
}
