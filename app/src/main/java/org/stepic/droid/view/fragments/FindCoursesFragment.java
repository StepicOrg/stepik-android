package org.stepic.droid.view.fragments;

import android.os.Bundle;

import com.squareup.otto.Subscribe;

import org.stepic.droid.base.CoursesFragmentBase;
import org.stepic.droid.events.notify_ui.NotifyUICoursesEvent;
import org.stepic.droid.events.courses.FailCoursesDownloadEvent;
import org.stepic.droid.events.courses.FinishingGetCoursesFromDbEvent;
import org.stepic.droid.events.courses.FinishingSaveCoursesToDbEvent;
import org.stepic.droid.events.courses.GettingCoursesFromDbSuccessEvent;
import org.stepic.droid.events.courses.PreLoadCoursesEvent;
import org.stepic.droid.events.courses.StartingGetCoursesFromDbEvent;
import org.stepic.droid.events.courses.StartingSaveCoursesToDbEvent;
import org.stepic.droid.events.courses.SuccessCoursesDownloadEvent;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.store.operations.DatabaseManager;

public class FindCoursesFragment extends CoursesFragmentBase {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected DatabaseManager.Table getCourseType() {
        return DatabaseManager.Table.featured;
    }

    @Override
    public void onStart() {
        super.onStart();

        mTypeOfCourse = DatabaseManager.Table.featured;
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

    @Subscribe
    @Override
    public void onPreLoad(PreLoadCoursesEvent e) {
        if (e.getType() == mTypeOfCourse)
            super.onPreLoad(e);
    }

    @Subscribe
    @Override
    public void onSuccessJoin(SuccessJoinEvent e) {
        super.onSuccessJoin(e);
    }

    @Subscribe
    @Override
    public void onNotifyUI(NotifyUICoursesEvent e) {
        super.onNotifyUI(e);
    }
}
