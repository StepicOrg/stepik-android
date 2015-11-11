package org.stepic.droid.base;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.concurrency.FromDbCoursesTask;
import org.stepic.droid.concurrency.ToDbCoursesTask;
import org.stepic.droid.concurrency.UpdateCourseTask;
import org.stepic.droid.events.courses.FailCoursesDownloadEvent;
import org.stepic.droid.events.courses.FinishingGetCoursesFromDbEvent;
import org.stepic.droid.events.courses.FinishingSaveCoursesToDbEvent;
import org.stepic.droid.events.courses.GettingCoursesFromDbSuccessEvent;
import org.stepic.droid.events.courses.PreLoadCoursesEvent;
import org.stepic.droid.events.courses.StartingGetCoursesFromDbEvent;
import org.stepic.droid.events.courses.StartingSaveCoursesToDbEvent;
import org.stepic.droid.events.courses.SuccessCoursesDownloadEvent;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.AppConstants;
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

public abstract class CoursesFragmentBase extends FragmentBase implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "base_fragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_courses, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Bind(R.id.swipe_refresh_layout_mycourses)
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.list_of_courses)
    protected ListView mListOfCourses;


    //    protected LoadingCoursesTask mLoadingCoursesTask;
    protected List<Course> mCourses;
    protected MyCoursesAdapter mCoursesAdapter;
    protected int mCurrentPage;
    protected boolean mHasNextPage;
    protected DatabaseManager.Table mTypeOfCourse;
    protected FromDbCoursesTask mDbGetCoursesTask;
    protected ToDbCoursesTask mDbSaveCoursesTask;
    protected View mFooterDownloadingView;
    protected volatile boolean isLoading;
    protected Handler mHandlerStateUpdating;

    boolean userScrolled;

    private boolean isFirstCreating;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        isFirstCreating = true;
        isLoading = false;
        mCurrentPage = 1;
        mHasNextPage = true;

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon);

        if (mCourses == null) mCourses = new ArrayList<>();

        mFooterDownloadingView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.loading_view, null, false);
        mFooterDownloadingView.setVisibility(View.GONE);
        mListOfCourses.addFooterView(mFooterDownloadingView);

        mCoursesAdapter = new MyCoursesAdapter(getContext(), mCourses, getCourseType());
        mListOfCourses.setAdapter(mCoursesAdapter);

        mListOfCourses.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true; // just for 1st creation
                } else {
//                    userScrolled = false;
                }
                Log.i(TAG, "user scrolled " + (userScrolled ? "true" : "false"));
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.i(TAG, "onScroll is invoked");
                if (!isLoading && mHasNextPage && firstVisibleItem + visibleItemCount >= totalItemCount && userScrolled) {
                    Log.i(TAG, "Go load from scroll");
                    isLoading = true;
                    downloadData();
                }
            }
        });

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                getAndShowDataFromCache();
            }
        });

        mHandlerStateUpdating = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updateState();
                mHandlerStateUpdating.postDelayed(this, AppConstants.UI_UPDATING_TIME);
            }
        };
        mHandlerStateUpdating.post(runnable);
    }

    protected void updateState() {
        if (mCourses == null || mCoursesAdapter == null || mCourses.size() == 0) {
            return;
        }

        for (Course course : mCourses) {
            course.setIs_loading(mDatabaseManager.isCourseLoading(course, mTypeOfCourse));
            course.setIs_cached(mDatabaseManager.isCourseCached(course, mTypeOfCourse));
        }
        mCoursesAdapter.notifyDataSetChanged();
    }


    protected void showCourses(List<Course> cachedCourses) {
        mCourses.clear();
        mCourses.addAll(cachedCourses);
        mCoursesAdapter.notifyDataSetChanged();
    }

    protected abstract DatabaseManager.Table getCourseType();

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
                if (response.isSuccess()) {
                    bus.post(new SuccessCoursesDownloadEvent(mTypeOfCourse, response, retrofit));
                } else {

                    bus.post(new FailCoursesDownloadEvent(mTypeOfCourse));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                bus.post(new FailCoursesDownloadEvent(mTypeOfCourse));
            }
        };

        IApi api = mShell.getApi();


        Log.i(TAG, "post pre load");
        bus.post(new PreLoadCoursesEvent(mTypeOfCourse));
        if (mTypeOfCourse == DatabaseManager.Table.featured) {
            api.getFeaturedCourses(mCurrentPage).enqueue(callback);
        } else {
            api.getEnrolledCourses(mCurrentPage).enqueue(callback);
        }
        Log.i(TAG, "mLoadingCoursesTask starts to execute");

    }

    private void saveDataToCache(List<Course> courses) {
        mDbSaveCoursesTask = new ToDbCoursesTask(courses, mTypeOfCourse, mCurrentPage);
        mDbSaveCoursesTask.execute();
    }


    public void getAndShowDataFromCache() {
        mDbGetCoursesTask = new FromDbCoursesTask(mTypeOfCourse) {
            @Override
            protected void onSuccess(List<Course> courses) {
                super.onSuccess(courses);
                bus.post(new GettingCoursesFromDbSuccessEvent(mTypeOfCourse, courses));
            }
        };
        mDbGetCoursesTask.execute();
    }

    @Subscribe
    public void onPreLoad(PreLoadCoursesEvent e) {
        Log.i(TAG, "preLoad");
        isLoading = true;
        if (mCurrentPage == 1) {
            mFooterDownloadingView.setVisibility(View.GONE);
        } else {
            mFooterDownloadingView.setVisibility(View.VISIBLE);
        }
        ProgressHelper.activate(mSwipeRefreshLayout);
    }

    @Subscribe
    public void onSuccessDataLoad(SuccessCoursesDownloadEvent e) {
        Response<CoursesStepicResponse> response = e.getResponse();
        if (response.body() != null &&
                response.body().getCourses() != null &&
                response.body().getCourses().size() != 0) {
            CoursesStepicResponse coursesStepicResponse = response.body();
            ProgressHelper.dismiss(mSwipeRefreshLayout);
            saveDataToCache(coursesStepicResponse.getCourses());
            getAndShowDataFromCache();

            mHasNextPage = coursesStepicResponse.getMeta().isHas_next();
            if (mHasNextPage) {
                mCurrentPage = coursesStepicResponse.getMeta().getPage() + 1;
            }
        } else {
            mHasNextPage = false;
            //// TODO: 17.10.15 explore this case (just when user do not have enrolled courses?)
            bus.post(new FailCoursesDownloadEvent(mTypeOfCourse));
        }
        isLoading = false;
    }

    @Subscribe
    public void onFailureDataLoad(FailCoursesDownloadEvent e) {
        ProgressHelper.dismiss(mSwipeRefreshLayout);
        mFooterDownloadingView.setVisibility(View.GONE);
        isLoading = false;
    }

    @Subscribe
    public void onStartingSaveToDb(StartingSaveCoursesToDbEvent e) {
        ProgressHelper.activate(mSwipeRefreshLayout);
    }

    @Subscribe
    public void onFinishingSaveToDb(FinishingSaveCoursesToDbEvent e) {
        ProgressHelper.dismiss(mSwipeRefreshLayout);
    }

    @Subscribe
    public void onStartingGetFromDb(StartingGetCoursesFromDbEvent e) {
        ProgressHelper.activate(mSwipeRefreshLayout);
    }

    @Subscribe
    public void onFinishingGetFromDb(FinishingGetCoursesFromDbEvent e) {
        ProgressHelper.dismiss(mSwipeRefreshLayout);
        if (mFooterDownloadingView != null) mFooterDownloadingView.setVisibility(View.GONE);

        if (e.getResult() != null && e.getResult().size() == 0)
            downloadData();
    }

    @Subscribe
    public void onGettingFromDbSuccess(GettingCoursesFromDbSuccessEvent e) {
        showCourses(e.getCourses());
    }

    @Subscribe
    public void onSuccessJoin(SuccessJoinEvent e) {
        //We do not upgrade database, because when
        //Only for find courses event.

        Course courseForUpdate = e.getCourse();
        for (Course courseItem : mCourses) {
            if (courseItem.getCourseId() == courseForUpdate.getCourseId()) {
                courseItem.setEnrollment((int) courseItem.getCourseId());
                courseForUpdate = courseItem;
                break;
            }
        }

        UpdateCourseTask updateCourseTask = new UpdateCourseTask(mTypeOfCourse, courseForUpdate);
        updateCourseTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        mSwipeRefreshLayout.setRefreshing(false);
        bus.register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onStop() {
        super.onStop();
        bus.unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
