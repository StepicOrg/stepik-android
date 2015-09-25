package org.stepic.droid.view.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.stepic.droid.R;
import org.stepic.droid.base.StepicBaseFragment;
import org.stepic.droid.concurrency.AsyncResultWrapper;
import org.stepic.droid.concurrency.LoadingCoursesTask;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DbOperationsCourses;
import org.stepic.droid.view.adapters.MyCoursesAdapter;

import java.sql.SQLException;
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon);

        if (mCourses == null) mCourses = new ArrayList<>();
        mCoursesAdapter = new MyCoursesAdapter(getContext(), mCourses);
        mListOfCourses.setAdapter(mCoursesAdapter);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                onRefresh();
            }
        });
    }

    public final LoadingCoursesTask initCoursesLoadingTask(final LoadingCoursesTask.CourseType type) {
        LoadingCoursesTask task = new LoadingCoursesTask(type) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mSwipeRefreshLayout.setRefreshing(true);
            }

            @Override
            protected void onSuccess(List<Course> courses) {
                super.onSuccess(courses);
                showCachedCourses(courses);
            }

            @Override
            protected void onException(Throwable exception) {
                super.onException(exception);
                int ignore = 0;
            }

            @Override
            protected void onPostExecute(AsyncResultWrapper<List<Course>> listAsyncResultWrapper) {
                super.onPostExecute(listAsyncResultWrapper);
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
    public abstract void onRefresh();

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
