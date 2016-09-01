package org.stepic.droid.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.events.courses.FailCoursesDownloadEvent;
import org.stepic.droid.events.courses.PreLoadCoursesEvent;
import org.stepic.droid.events.courses.SuccessCoursesDownloadEvent;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.ui.activities.MainFeedActivity;
import org.stepic.droid.ui.adapters.MyCoursesAdapter;
import org.stepic.droid.ui.custom.TouchDispatchableFrameLayout;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.StepicUtil;
import org.stepic.droid.web.CoursesStepicResponse;
import org.stepic.droid.web.IApi;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Response;
import retrofit.Retrofit;

public abstract class CourseListFragmentBase extends FragmentBase implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.swipe_refresh_layout_mycourses)
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.list_of_courses)
    protected ListView mListOfCourses;

    @BindView(R.id.report_problem)
    protected View mReportConnectionProblem;

    @BindView(R.id.empty_courses)
    protected View mEmptyCoursesView;

    @BindView(R.id.empty_courses_button)
    protected Button findCourseButton;

    @BindView(R.id.root_fragment_view)
    protected TouchDispatchableFrameLayout mRootView;

    @BindView(R.id.load_progressbar)
    protected ProgressBar mProgressBarOnEmptyScreen;

    @BindView(R.id.empty_search)
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
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                    //check inside more expensive condition:
                    if (StepicUtil.INSTANCE.isInternetAvailable()) {
                        isLoading = true;
                        downloadData();
                    } else {
//                        userScrolled =false;
                    }
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

        findCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity parent = getActivity();
                if (parent == null || !(parent instanceof MainFeedActivity)) return;

                analytic.reportEvent(Analytic.Interaction.CLICK_FIND_COURSE_EMPTY_SCREEN);
                ((MainFeedActivity) parent).showFindLesson();
            }
        });

    }

    @Override
    public void onDestroyView() {
        if (mListOfCourses != null) {
            mListOfCourses.setAdapter(null);
            mListOfCourses.setOnScrollListener(null);
            mListOfCourses.setOnItemClickListener(null);
        }
        super.onDestroyView();
    }

    protected abstract DatabaseFacade.Table getCourseType();

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
        if (getCourseType() == DatabaseFacade.Table.featured) {
            api.getFeaturedCourses(mCurrentPage).enqueue(callback);
        } else {
            api.getEnrolledCourses(mCurrentPage).enqueue(callback);
        }
    }

    @Override
    public void onRefresh() {
        analytic.reportEvent(Analytic.Interaction.PULL_TO_REFRESH_COURSE);
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

    public final void updateEnrollment(Course courseForUpdate, long enrollment) {
        boolean inList = false;
        for (Course courseItem : mCourses) {
            if (courseItem.getCourseId() == courseForUpdate.getCourseId()) {
                courseItem.setEnrollment((int) courseItem.getCourseId());
                courseForUpdate = courseItem;
                inList = true;
                break;
            }
        }
        if (getCourseType() == DatabaseFacade.Table.enrolled && !inList) {
            mCourses.add(courseForUpdate);
            mCoursesAdapter.notifyDataSetChanged();
            ;
        }

    }

    @Subscribe
    public void onSuccessJoin(SuccessJoinEvent e) {
        updateEnrollment(e.getCourse(), e.getCourse().getEnrollment());
    }

    public abstract void showEmptyScreen(boolean isShow);
}
