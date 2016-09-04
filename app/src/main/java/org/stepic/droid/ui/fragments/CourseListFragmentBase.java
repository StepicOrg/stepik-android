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
import org.stepic.droid.core.presenters.contracts.CoursesView;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.Table;
import org.stepic.droid.ui.activities.MainFeedActivity;
import org.stepic.droid.ui.adapters.MyCoursesAdapter;
import org.stepic.droid.ui.custom.TouchDispatchableFrameLayout;
import org.stepic.droid.util.KotlinUtil;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.StepicUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class CourseListFragmentBase extends FragmentBase implements SwipeRefreshLayout.OnRefreshListener, CoursesView {

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

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
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount && userScrolled && StepicUtil.INSTANCE.isInternetAvailable()) {
                    //check inside more expensive condition:
                    onNeedDownloadNextPage();
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

    protected abstract Table getCourseType();

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
        if (getCourseType() == Table.enrolled && !inList) {
            mCourses.add(courseForUpdate);
            mCoursesAdapter.notifyDataSetChanged();
        }

    }

    @Subscribe
    public void onSuccessJoin(SuccessJoinEvent e) {
        updateEnrollment(e.getCourse(), e.getCourse().getEnrollment());
    }

    @Override
    public void showLoading() {
        ProgressHelper.dismiss(mProgressBarOnEmptyScreen);
        ProgressHelper.dismiss(mSwipeRefreshLayout);
        mFooterDownloadingView.setVisibility(View.GONE);
        mReportConnectionProblem.setVisibility(View.GONE);

        if (mCourses.isEmpty()) {
            ProgressHelper.activate(mSwipeRefreshLayout);
        } else if (mSwipeRefreshLayout != null && !mSwipeRefreshLayout.isRefreshing()) {
            mFooterDownloadingView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showEmptyCourses() {
        ProgressHelper.dismiss(mProgressBarOnEmptyScreen);
        ProgressHelper.dismiss(mSwipeRefreshLayout);
        mFooterDownloadingView.setVisibility(View.GONE);
        mReportConnectionProblem.setVisibility(View.GONE);
        if (mCourses.isEmpty()) {
            showEmptyScreen(true);
        }
    }

    @Override
    public void showConnectionProblem() {
        ProgressHelper.dismiss(mProgressBarOnEmptyScreen);
        ProgressHelper.dismiss(mSwipeRefreshLayout);
        mFooterDownloadingView.setVisibility(View.GONE);

        if (mCourses == null || mCourses.isEmpty()) {
            //screen is clear due to error connection
            showEmptyScreen(false);
            mReportConnectionProblem.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showCourses(List<Course> courses) {
        ProgressHelper.dismiss(mProgressBarOnEmptyScreen);
        ProgressHelper.dismiss(mSwipeRefreshLayout);
        mFooterDownloadingView.setVisibility(View.GONE);
        mReportConnectionProblem.setVisibility(View.GONE);
        showEmptyScreen(false);
        List<Course> localList = new ArrayList<>(mCourses);
        localList.addAll(courses);
        List<Course> finalCourses = KotlinUtil.INSTANCE.filterIfNotUnique(localList);
        mCourses.clear();
        mCourses.addAll(finalCourses);
        mCoursesAdapter.notifyDataSetChanged();
    }

    protected abstract void onNeedDownloadNextPage();

    protected abstract void showEmptyScreen(boolean isShow);
}
