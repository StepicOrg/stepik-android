package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.stepic.droid.events.courses.FailCoursesDownloadEvent;
import org.stepic.droid.events.courses.SuccessCoursesDownloadEvent;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.events.search.SuccessSearchEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.SearchResult;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.web.CoursesStepicResponse;
import org.stepic.droid.web.SearchResultResponse;

import java.util.ArrayList;
import java.util.List;

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
        mEmptySearch.setClickable(false);
        mEmptySearch.setFocusable(false);
//        mProgressBarOnEmptyScreen.setVisibility(View.VISIBLE);
        bus.register(this);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                downloadData();
            }
        });

    }

    @Override
    public void downloadData() {
        ProgressHelper.activate(mSwipeRefreshLayout);
        isLoading = true;
        mShell.getApi().getSearchResultsCourses(mCurrentPage, mSearchQuery).enqueue(new Callback<SearchResultResponse>() {
            @Override
            public void onResponse(Response<SearchResultResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    bus.post(new SuccessSearchEvent(mSearchQuery, response));
                } else {
                    bus.post(new FailCoursesDownloadEvent(null));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                bus.post(new FailCoursesDownloadEvent(null));
            }
        });
    }
//
//    @Subscribe
//    public void onFailSearch(FailSearchEvent e) {
//        if (!e.getQuery().equals(mSearchQuery)) return;
//
//        ProgressHelper.dismiss(mProgressBarOnEmptyScreen);
//        mEmptySearch.setVisibility(View.GONE);
//        mSwipeRefreshLayout.setVisibility(View.GONE);
//        reportConnectionProblem.setVisibility(View.VISIBLE);
//    }

    @Subscribe
    public void onSuccessSearchResult(SuccessSearchEvent e) {
        if (!e.getQuery().equals(mSearchQuery)) return;

        List<SearchResult> searchResultList = e.getResponse().body().getSearchResultList();
        long[] courseIdsForSearch = mSearchResolver.getCourseIdsFromSearchResults(searchResultList);

        mHasNextPage = e.getResponse().body().getMeta().getHas_next();
        mCurrentPage++;

        // TODO: 31.12.15 has next page should be new for search results
        downloadCoursesById(courseIdsForSearch);
    }


    public void downloadCoursesById(final long[] mCourseIdsForSearch) {
        if (mCourseIdsForSearch == null || mCourseIdsForSearch.length == 0) {
            if (mCourses.isEmpty()) {
                showEmptyState();
            } else {
                bus.post(new FailCoursesDownloadEvent(null));
            }
            return;
        }
        mShell.getApi().getCourses(1, mCourseIdsForSearch).enqueue(new Callback<CoursesStepicResponse>() {
            @Override
            public void onResponse(Response<CoursesStepicResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {

                    bus.post(new SuccessCoursesDownloadEvent(null, response, retrofit, mCourseIdsForSearch));
                } else {
                    bus.post(new FailCoursesDownloadEvent(null));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                bus.post(new FailCoursesDownloadEvent(null));
            }
        });

    }

    @Override
    public void onDestroyView() {
        bus.unregister(this);
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
    public void onSuccessDataLoad(SuccessCoursesDownloadEvent e) {
        if (e.getType() != null) return;


        Response<CoursesStepicResponse> response = e.getResponse();
        if (response.body() != null &&
                response.body().getCourses() != null &&
                response.body().getCourses().size() != 0) {
            CoursesStepicResponse coursesStepicResponse = response.body();
            ProgressHelper.dismiss(mSwipeRefreshLayout);

            List<Course> cachedCourses = coursesStepicResponse.getCourses();
            List<Course> sortedCopy = new ArrayList<>();
            long[] searchIds = e.getSearchIds();
            if (searchIds != null) {
                //// FIXME: 10.01.16 use other data structure
                Course forInsert = null;
                for (long searchId : searchIds) {
                    for (Course cachedCourse : cachedCourses) {
                        if (cachedCourse.getCourseId() == searchId) {
                            forInsert = cachedCourse;
                            break;
                        }
                    }
                    if (forInsert != null) {
                        sortedCopy.add(forInsert);
                    }
                    forInsert = null;
                }
            }
            showCourses(sortedCopy);
        } else {
            showEmptyState();
        }
        isLoading = false;
    }

    //local helper method todo refactor
    private void showEmptyState() {
        mReportConnectionProblem.setVisibility(View.GONE);
        showEmptyScreen(true);

        mFooterDownloadingView.setVisibility(View.GONE);
        ProgressHelper.dismiss(mSwipeRefreshLayout);
    }

    protected void showCourses(List<Course> cachedCourses) {
        if (!cachedCourses.isEmpty()) {
            showEmptyScreen(false);
            mReportConnectionProblem.setVisibility(View.GONE);

            //todo optimize it with other data structure

            boolean needAdd = true;
            for (Course newLoadedCourse : cachedCourses) {
                for (int i = 0; i < mCourses.size(); i++) {
                    if (mCourses.get(i).getCourseId() == newLoadedCourse.getCourseId()) {
                        needAdd = false;
                        break;
                    }
                }
                if (needAdd) {
                    mCourses.add(newLoadedCourse);
                }
                needAdd = true;
            }
            mCoursesAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe
    @Override
    public void onFailureDataLoad(FailCoursesDownloadEvent e) {
        super.onFailureDataLoad(e);
    }

    @Subscribe
    @Override
    public void onSuccessJoin(SuccessJoinEvent e) {
        super.onSuccessJoin(e);
    }

}
