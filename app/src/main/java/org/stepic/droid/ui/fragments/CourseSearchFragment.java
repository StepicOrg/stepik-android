package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.CourseListModule;
import org.stepic.droid.core.presenters.SearchCoursesPresenter;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.store.operations.DatabaseFacade;

import javax.inject.Inject;

public class CourseSearchFragment extends CourseListFragmentBase {
    public static Fragment newInstance(String query) {
        Fragment fragment = new CourseSearchFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CourseSearchFragment.QUERY_KEY, query);
        fragment.setArguments(bundle);
        return fragment;
    }

    private final static String QUERY_KEY = "query_key";

    private String searchQuery;

    @Inject
    SearchCoursesPresenter searchCoursesPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        MainApplication.component().plus(new CourseListModule()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        searchQuery = getArguments().getString(QUERY_KEY);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmptySearch.setClickable(false);
        mEmptySearch.setFocusable(false);
        bus.register(this);
        searchCoursesPresenter.attachView(this);
        searchCoursesPresenter.restoreState();
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                searchCoursesPresenter.downloadData(searchQuery);
            }
        });

    }

    @Override
    public void onDestroyView() {
        bus.unregister(this);
        searchCoursesPresenter.detachView(this);
        super.onDestroyView();
    }

    @Override
    protected DatabaseFacade.Table getCourseType() {
        return null;
    }

    @Override
    public void showEmptyScreen(boolean isShowed) {
        if (isShowed) {
            mEmptySearch.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setVisibility(View.GONE);
        } else {
            mEmptySearch.setVisibility(View.GONE);
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);

        }
    }

    @Subscribe
    @Override
    public void onSuccessJoin(SuccessJoinEvent e) {
        super.onSuccessJoin(e);
    }

    @Override
    protected void onNeedDownloadNextPage() {
        searchCoursesPresenter.downloadData(searchQuery);
    }

    @Override
    public void onRefresh() {
        searchCoursesPresenter.refreshData(searchQuery);
    }
}
