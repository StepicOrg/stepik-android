package org.stepic.droid.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.stepic.droid.events.courses.SuccessCoursesDownloadEvent;
import org.stepic.droid.events.search.FailSearchEvent;
import org.stepic.droid.events.search.SuccessSearchEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.SearchResult;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.web.CoursesStepicResponse;
import org.stepic.droid.web.SearchResultResponse;

import java.util.List;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class CourseSearchFragment extends CourseListFragmentBase {
    public final static String QUERY_KEY = "query_key";

    private String mSearchQuery;
    private long[] mCourseIdsForSearch;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSearchQuery = getArguments().getString(QUERY_KEY);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEmptySearch.setClickable(false);
        mEmptySearch.setFocusable(false);
        mProgressBarOnEmptyScreen.setVisibility(View.VISIBLE);
        bus.register(this);
        fetchSearchResults();
    }

    private void fetchSearchResults() {
        ProgressHelper.activate(mProgressBarOnEmptyScreen);
        mShell.getApi().getSearchResultsCourses(mCurrentPage, mSearchQuery).enqueue(new Callback<SearchResultResponse>() {
            @Override
            public void onResponse(Response<SearchResultResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    bus.post(new SuccessSearchEvent(mSearchQuery, response));
                } else {
                    bus.post(new FailSearchEvent(mSearchQuery));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                bus.post(new FailSearchEvent(mSearchQuery));
            }
        });
    }

    @Subscribe
    public void onFailSearch(FailSearchEvent e) {
        if (!e.getQuery().equals(mSearchQuery)) return;

        ProgressHelper.dismiss(mProgressBarOnEmptyScreen);
        mEmptySearch.setVisibility(View.GONE);
        mSwipeRefreshLayout.setVisibility(View.GONE);
        mReportConnectionProblem.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void onSuccessSearchResult(SuccessSearchEvent e) {
        if (!e.getQuery().equals(mSearchQuery)) return;

        ProgressHelper.dismiss(mProgressBarOnEmptyScreen);

        List<SearchResult> searchResultList = e.getResponse().body().getSearchResultList();
        mCourseIdsForSearch = mSearchResolver.getCourseIdsFromSearchResults(searchResultList);

        // TODO: 31.12.15 has next page should be new for search results
        downloadData();
    }

    @Override
    public void downloadData() {
        if (mCourseIdsForSearch == null) return;
        mShell.getApi().getCourses(1, mCourseIdsForSearch).enqueue(new Callback<CoursesStepicResponse>() {
            @Override
            public void onResponse(Response<CoursesStepicResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    bus.post(new SuccessCoursesDownloadEvent(null, response, retrofit));
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });

    }

    @Override
    public void onDestroyView() {
        bus.unregister(this);
        super.onDestroyView();
    }

    @Override
    protected DatabaseManager.Table getCourseType() {
        return null;
    }

    void showEmptyScreen(boolean isShowed) {
        if (isShowed) {
            mEmptySearch.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setVisibility(View.GONE);
        } else {
            mEmptySearch.setVisibility(View.GONE);
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);

        }
    }

    @Subscribe
    public void onSuccessDataLoad(SuccessCoursesDownloadEvent e) {
        if (e.getType() != null) return;

        Response<CoursesStepicResponse> response = e.getResponse();
        if (response.body() != null &&
                response.body().getCourses() != null &&
                response.body().getCourses().size() != 0) {
            CoursesStepicResponse coursesStepicResponse = response.body();
            ProgressHelper.dismiss(mSwipeRefreshLayout);

            showCourses(coursesStepicResponse.getCourses());

            mHasNextPage = coursesStepicResponse.getMeta().isHas_next();
            if (mHasNextPage) {
                mCurrentPage = coursesStepicResponse.getMeta().getPage() + 1;
            }
        } else {
            mHasNextPage = false;
            mReportConnectionProblem.setVisibility(View.GONE);
            showEmptyScreen(true);

            mFooterDownloadingView.setVisibility(View.GONE);
            ProgressHelper.dismiss(mSwipeRefreshLayout);
        }
        isLoading = false;
    }

    protected void showCourses(List<Course> cachedCourses) {
        if (cachedCourses != null || cachedCourses.size() != 0) {
            showEmptyScreen(false);
            mReportConnectionProblem.setVisibility(View.GONE);
        }

        mCourses.clear();
        mCourses.addAll(cachedCourses);
        mCoursesAdapter.notifyDataSetChanged();
    }
}
