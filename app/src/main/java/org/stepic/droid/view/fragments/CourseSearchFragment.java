package org.stepic.droid.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.stepic.droid.events.search.FailSearchEvent;
import org.stepic.droid.events.search.SuccessSearchEvent;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.web.SearchResultResponse;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class CourseSearchFragment extends CourseListFragmentBase {
    public final static String QUERY_KEY = "query_key";

    private String mSearchQuery;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSearchQuery = getArguments().getString(QUERY_KEY);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
                    bus.post(new SuccessSearchEvent(mSearchQuery));
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
        // TODO: 31.12.15 RESOLVE RESPONSE -> Courses id
        // TODO: 31.12.15 has next page should be new for search results
//        downloadData();

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
}
