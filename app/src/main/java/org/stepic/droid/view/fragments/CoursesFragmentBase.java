package org.stepic.droid.view.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import org.stepic.droid.R;
import org.stepic.droid.base.StepicBaseFragment;
import org.stepic.droid.concurrency.AsyncResultWrapper;
import org.stepic.droid.concurrency.LoadingCoursesTask;
import org.stepic.droid.model.Course;
import org.stepic.droid.view.adapters.MyCoursesAdapter;
import org.stepic.droid.web.CoursesStepicResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class CoursesFragmentBase extends StepicBaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_courses, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Bind(R.id.swipe_refresh_layout_mycourses)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.list_of_courses)
    ListView mListOfCourses;

    protected LoadingCoursesTask mLoadingCoursesTask;
    protected List<Course> mCourses;
    protected MyCoursesAdapter mCoursesAdapter;
    protected int mCurrentPage;
    protected boolean mHasNextPage;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCurrentPage = 1;
//        mHasNextPage = true;

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon);

        if (mCourses == null) mCourses = new ArrayList<>();
        mCoursesAdapter = new MyCoursesAdapter(getContext(), mCourses);
        mListOfCourses.setAdapter(mCoursesAdapter);
        mListOfCourses.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int CheckPls = 0;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mHasNextPage && firstVisibleItem + visibleItemCount >= totalItemCount) {
                    downloadData();
                }
            }
        });


        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                downloadData();
            }
        });
    }

    public final LoadingCoursesTask initCoursesLoadingTask(final LoadingCoursesTask.CourseType type) {
        LoadingCoursesTask task = new LoadingCoursesTask(type, mCurrentPage) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mSwipeRefreshLayout.setRefreshing(true);
            }

            @Override
            protected void onSuccess(CoursesStepicResponse coursesStepicResponse) {
                super.onSuccess(coursesStepicResponse);
                showCachedCourses(coursesStepicResponse.getCourses());

                mHasNextPage = coursesStepicResponse.getMeta().isHas_next();
                if (mHasNextPage) {
                    mCurrentPage++;
                    if (mCurrentPage > 2) {
                        int lol = 10;
                    }
                }
            }

            @Override
            protected void onException(Throwable exception) {
                super.onException(exception);
                int ignore = 0;
            }

            @Override
            protected void onPostExecute(AsyncResultWrapper<CoursesStepicResponse> coursesStepicResponseAsyncResultWrapper) {
                super.onPostExecute(coursesStepicResponseAsyncResultWrapper);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        };
        return task;
    }


    protected void showCachedCourses(List<Course> cachedCourses) {
        mCourses.clear();
        mCourses.addAll(cachedCourses);
        mCoursesAdapter.notifyDataSetChanged();
    }

    @Override
    public final void onRefresh() {
        mCurrentPage = 1;
        mHasNextPage = true;
        downloadData();
    }

    public abstract void downloadData();

    @Override
    public void onPause() {
        super.onPause();
        if (mLoadingCoursesTask != null && mLoadingCoursesTask.getStatus() != AsyncTask.Status.FINISHED) {
            mLoadingCoursesTask.cancel(true);
            mSwipeRefreshLayout.setRefreshing(false);
        }
        mListOfCourses.setAdapter(null);
    }


    @Override
    public void onStop() {
        super.onStop();
        ButterKnife.unbind(this);
        if (mLoadingCoursesTask != null)
            mLoadingCoursesTask.unbind();
    }
}
