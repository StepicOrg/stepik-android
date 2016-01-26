package org.stepic.droid.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.events.courses.FailCoursesDownloadEvent;
import org.stepic.droid.events.courses.PreLoadCoursesEvent;
import org.stepic.droid.events.courses.SuccessCoursesDownloadEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.view.activities.MainFeedActivity;
import org.stepic.droid.view.adapters.MyCoursesAdapter;
import org.stepic.droid.view.custom.TouchDispatchableFrameLayout;
import org.stepic.droid.web.CoursesStepicResponse;
import org.stepic.droid.web.IApi;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Response;
import retrofit.Retrofit;

public abstract class CourseListFragmentBase extends FragmentBase implements SwipeRefreshLayout.OnRefreshListener {

    @Bind(R.id.swipe_refresh_layout_mycourses)
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.list_of_courses)
    protected ListView mListOfCourses;

    @Bind(R.id.report_problem)
    protected View mReportConnectionProblem;

    @Bind(R.id.empty_courses)
    protected View mEmptyCoursesView;

    @Bind(R.id.root_fragment_view)
    protected TouchDispatchableFrameLayout mRootView;

    @Bind(R.id.load_progressbar)
    protected ProgressBar mProgressBarOnEmptyScreen;

    @Bind(R.id.empty_search)
    protected ViewGroup mEmptySearch;

    protected List<Course> mCourses;
    protected MyCoursesAdapter mCoursesAdapter;

    protected boolean userScrolled;

    protected View mFooterDownloadingView;
    protected volatile boolean isLoading;
    protected int mCurrentPage;
    protected boolean mHasNextPage;

    protected boolean isFirstCreating;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_courses, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mProgressBarOnEmptyScreen.setVisibility(View.GONE);

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

        registerForContextMenu(mListOfCourses);

        mCoursesAdapter = new MyCoursesAdapter(this, mCourses, getCourseType());
        mListOfCourses.setAdapter(mCoursesAdapter);

        mListOfCourses.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true; // just for 1st creation
                } else {
//                    userScrolled = false;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!isLoading && mHasNextPage && firstVisibleItem + visibleItemCount >= totalItemCount && userScrolled) {
                    isLoading = true;
                    downloadData();
                }
            }
        });

        mListOfCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= mCourses.size() || position < 0) return;
                Course course = mCourses.get(position);
                if (course.getEnrollment() != 0) {
                    mShell.getScreenProvider().showSections(getActivity(), course);
                } else {
                    mShell.getScreenProvider().showCourseDescription(CourseListFragmentBase.this, course);
                }
            }
        });

        mEmptyCoursesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainFeedActivity parent = (MainFeedActivity) getActivity();
                if (parent == null || parent instanceof MainFeedActivity == false) return;

                parent.showFindLesson();
            }
        });

    }

    protected abstract DatabaseManager.Table getCourseType();

    public void downloadData() {
        retrofit.Callback<CoursesStepicResponse> callback = new retrofit.Callback<CoursesStepicResponse>() {
            @Override
            public void onResponse(Response<CoursesStepicResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    bus.post(new SuccessCoursesDownloadEvent(getCourseType(), response, retrofit));
                } else {

                    bus.post(new FailCoursesDownloadEvent(getCourseType()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                bus.post(new FailCoursesDownloadEvent(getCourseType()));
            }
        };

        IApi api = mShell.getApi();

        bus.post(new PreLoadCoursesEvent(getCourseType()));
        if (getCourseType() == DatabaseManager.Table.featured) {
            api.getFeaturedCourses(mCurrentPage).enqueue(callback);
        } else {
            api.getEnrolledCourses(mCurrentPage).enqueue(callback);
        }
    }

    @Override
    public void onRefresh() {
        YandexMetrica.reportEvent(AppConstants.METRICA_REFRESH_COURSE);
        mCurrentPage = 1;
        mHasNextPage = true;
        downloadData();
    }

    @Subscribe
    public void onFailureDataLoad(FailCoursesDownloadEvent e) {
        ProgressHelper.dismiss(mProgressBarOnEmptyScreen);
        ProgressHelper.dismiss(mSwipeRefreshLayout);
        mFooterDownloadingView.setVisibility(View.GONE);
        isLoading = false;

        if (mCourses == null || mCourses.isEmpty()) {
            //screen is clear due to error connection
            showEmptyScreen(false);
            mReportConnectionProblem.setVisibility(View.VISIBLE);
        }
    }


    public abstract void showEmptyScreen(boolean isShow);
}
