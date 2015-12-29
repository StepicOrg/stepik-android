package org.stepic.droid.view.fragments;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.CoursesFragmentBase;
import org.stepic.droid.events.courses.FailCoursesDownloadEvent;
import org.stepic.droid.events.courses.FailDropCourseEvent;
import org.stepic.droid.events.courses.FinishingGetCoursesFromDbEvent;
import org.stepic.droid.events.courses.FinishingSaveCoursesToDbEvent;
import org.stepic.droid.events.courses.GettingCoursesFromDbSuccessEvent;
import org.stepic.droid.events.courses.PreLoadCoursesEvent;
import org.stepic.droid.events.courses.StartingGetCoursesFromDbEvent;
import org.stepic.droid.events.courses.StartingSaveCoursesToDbEvent;
import org.stepic.droid.events.courses.SuccessCoursesDownloadEvent;
import org.stepic.droid.events.courses.SuccessDropCourseEvent;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.store.operations.DatabaseManager;

public class FindCoursesFragment extends CoursesFragmentBase {

    SearchView mSearchView = null;
    MenuItem mMenuItem = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListOfCourses.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    collapseAndHide();
                }
            }
        });

    }

    private void collapseAndHide() {
        if (mSearchView != null && mMenuItem != null) {
            Log.e(TAG, "COLLAPSE");
            hideSoftKeypad();
            MenuItemCompat.collapseActionView(mMenuItem);
        }
    }

    @Override
    protected DatabaseManager.Table getCourseType() {
        return DatabaseManager.Table.featured;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    @Subscribe
    public void onFailureDataLoad(FailCoursesDownloadEvent e) {
        if (e.getType() == DatabaseManager.Table.featured)
            super.onFailureDataLoad(e);
    }

    @Override
    @Subscribe
    public void onStartingSaveToDb(StartingSaveCoursesToDbEvent e) {
        if (e.getType() == DatabaseManager.Table.featured)
            super.onStartingSaveToDb(e);
    }

    @Override
    @Subscribe
    public void onFinishingSaveToDb(FinishingSaveCoursesToDbEvent e) {
        if (e.getType() == DatabaseManager.Table.featured)
            super.onFinishingSaveToDb(e);
    }

    @Override
    @Subscribe
    public void onStartingGetFromDb(StartingGetCoursesFromDbEvent e) {
        if (e.getType() == DatabaseManager.Table.featured)
            super.onStartingGetFromDb(e);
    }

    @Override
    @Subscribe
    public void onFinishingGetFromDb(FinishingGetCoursesFromDbEvent e) {
        if (e.getType() == DatabaseManager.Table.featured)
            super.onFinishingGetFromDb(e);
    }

    @Subscribe
    public void onGettingFromDbSuccess(GettingCoursesFromDbSuccessEvent e) {
        if (e.getType() == DatabaseManager.Table.featured)
            super.onGettingFromDbSuccess(e);
    }

    @Subscribe
    @Override
    public void onSuccessDataLoad(SuccessCoursesDownloadEvent e) {
        if (e.getType() == DatabaseManager.Table.featured)
            super.onSuccessDataLoad(e);
    }

    @Subscribe
    @Override
    public void onPreLoad(PreLoadCoursesEvent e) {
        if (e.getType() == DatabaseManager.Table.featured)
            super.onPreLoad(e);
    }

    @Subscribe
    @Override
    public void onSuccessJoin(SuccessJoinEvent e) {
        super.onSuccessJoin(e);
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


    String TAG = "searchView";

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) mMenuItem.getActionView();


        ComponentName componentName = getActivity().getComponentName();
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(componentName);
        mSearchView.setSearchableInfo(searchableInfo);

        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.e(TAG, "onFocusChangeQuery");
                if (!hasFocus) {
                    hideSoftKeypad();
                }
            }
        });

        mSearchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.e(TAG, "onFocusChange");
                if (!hasFocus) {
                    hideSoftKeypad();
                }
            }
        });


    }


}
