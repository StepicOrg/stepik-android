package org.stepic.droid.ui.fragments;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.CoursesDatabaseFragmentBase;
import org.stepic.droid.events.courses.FailDropCourseEvent;
import org.stepic.droid.events.courses.SuccessDropCourseEvent;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.ui.listeners.OnRootTouchedListener;

public class FindCoursesFragment extends CoursesDatabaseFragmentBase {

    public static FindCoursesFragment newInstance() {
        return new FindCoursesFragment();
    }

    SearchView mSearchView = null;
    MenuItem mMenuItem = null;
    private boolean handledByRoot = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRootView.setParentTouchEvent(new OnRootTouchedListener() {
            @Override
            public void makeBeforeChildren() {
                collapseAndHide(true);
            }
        });

        mListOfCourses.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!handledByRoot) {
                        collapseAndHide(false);
                    }
                    handledByRoot = false;
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        if (mListOfCourses != null) {
            mListOfCourses.setOnFocusChangeListener(null);
        }
        super.onDestroyView();
    }

    private void collapseAndHide(boolean rootHandle) {
        if (mSearchView != null && mMenuItem != null && mMenuItem.isActionViewExpanded()) {
            if (rootHandle) handledByRoot = true;
            hideSoftKeypad();//in collapse action view keypad going to invisible after animation
            MenuItemCompat.collapseActionView(mMenuItem);
        }
    }

    @Override
    protected DatabaseFacade.Table getCourseType() {
        return DatabaseFacade.Table.featured;
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) mMenuItem.getActionView();

        ComponentName componentName = getActivity().getComponentName();
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(componentName);
        mSearchView.setSearchableInfo(searchableInfo);
        mSearchView.setMaxWidth(20000);//it is dirty workaround for expand in landscape
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                collapseAndHide(false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSearchView != null) {
            mSearchView.setOnQueryTextListener(null);
        }
    }
}
