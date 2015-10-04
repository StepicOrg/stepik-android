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
import android.widget.AdapterView;
import android.widget.ListView;

import org.stepic.droid.R;
import org.stepic.droid.base.StepicBaseFragment;
import org.stepic.droid.concurrency.AsyncResultWrapper;
import org.stepic.droid.concurrency.FromDbCoursesTask;
import org.stepic.droid.concurrency.ToDbCoursesTask;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DbOperationsCourses;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.view.adapters.MyCoursesAdapter;
import org.stepic.droid.web.CoursesStepicResponse;
import org.stepic.droid.web.IApi;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Response;
import retrofit.Retrofit;

public abstract class CoursesFragmentBase extends StepicBaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "base_fragment";

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

    //    protected LoadingCoursesTask mLoadingCoursesTask;
    protected List<Course> mCourses;
    protected MyCoursesAdapter mCoursesAdapter;
    protected int mCurrentPage;
    protected boolean mHasNextPage;
    protected DbOperationsCourses.Table mTypeOfCourse;
    protected FromDbCoursesTask mDbGetCoursesTask;
    protected ToDbCoursesTask mDbSaveCoursesTask;
    private volatile boolean isPaused;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        isPaused = false;
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
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mHasNextPage && firstVisibleItem + visibleItemCount >= totalItemCount) {
                    downloadData();
                }
            }
        });
        mListOfCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Course courseItem = (Course) mListOfCourses.getItemAtPosition(position);
                if (courseItem.getEnrollment() != 0) {
                    mShell.getScreenProvider().showCourseDescriptionForEnrolled(CoursesFragmentBase.this.getContext(), courseItem);
                } else {
                    mShell.getScreenProvider().showCourseDescriptionForNotEnrolled(CoursesFragmentBase.this.getContext(), courseItem);
                }
            }
        });

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                getAndShowDataFromCache();
            }
        });
    }


    protected void showCourses(List<Course> cachedCourses) {
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


    public void downloadData() {

        retrofit.Callback<CoursesStepicResponse> callback = new retrofit.Callback<CoursesStepicResponse>() {
            @Override
            public void onResponse(Response<CoursesStepicResponse> response, Retrofit retrofit) {

                CoursesStepicResponse coursesStepicResponse = response.body();

                saveDataToCache(coursesStepicResponse.getCourses());
                getAndShowDataFromCache();

                mHasNextPage = coursesStepicResponse.getMeta().isHas_next();
                if (mHasNextPage) {
                    mCurrentPage++;
                }
            }

            @Override
            public void onFailure(Throwable t) {
                ProgressHelper.dismiss(mSwipeRefreshLayout);

            }
        };

        IApi api = mShell.getApi();
        ProgressHelper.activate(mSwipeRefreshLayout);
        if (mTypeOfCourse == DbOperationsCourses.Table.featured) {
            api.getFeaturedCourses(mCurrentPage).enqueue(callback);
        } else {
            api.getEnrolledCourses(mCurrentPage).enqueue(callback);
        }
        Log.i(TAG, "mLoadingCoursesTask starts to execute");
    }

    public void saveDataToCache(List<Course> courses) {
        mDbSaveCoursesTask = new ToDbCoursesTask(courses, mTypeOfCourse) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                ProgressHelper.activate(mSwipeRefreshLayout);
            }


            @Override
            protected void onPostExecute(AsyncResultWrapper<Void> voidAsyncResultWrapper) {
                super.onPostExecute(voidAsyncResultWrapper);
                ProgressHelper.dismiss(mSwipeRefreshLayout);
            }
        };
        mDbSaveCoursesTask.execute();
    }


    public void getAndShowDataFromCache() {
        mDbGetCoursesTask = new FromDbCoursesTask(mTypeOfCourse) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mSwipeRefreshLayout.setRefreshing(true);
            }

            @Override
            protected void onSuccess(List<Course> courses) {
                super.onSuccess(courses);
                showCourses(courses);
            }

            @Override
            protected void onPostExecute(AsyncResultWrapper<List<Course>> listAsyncResultWrapper) {
                if (isPaused) return;
                super.onPostExecute(listAsyncResultWrapper);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        };
        mDbGetCoursesTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        //todo Use otto for handling errors

        if (mDbGetCoursesTask != null && mDbGetCoursesTask.getStatus() != AsyncTask.Status.FINISHED) {
            mDbGetCoursesTask.cancel(true);
            mSwipeRefreshLayout.setRefreshing(false);
        }

        isPaused = true;

        //// FIXME: 28.09.15 : Task may init after onPause() and start to execute.
    }


    @Override
    public void onStop() {
        super.onStop();

        //todo Use otto for handling errors
    }

    @Override
    public void onDestroyView() {
        if (mListOfCourses != null)
            mListOfCourses.setAdapter(null);
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(false);
        super.onDestroyView();
    }
}
