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
import org.stepic.droid.ui.adapters.CoursesAdapter;
import org.stepic.droid.ui.custom.TouchDispatchableFrameLayout;
import org.stepic.droid.util.KotlinUtil;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.StepikUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public abstract class CourseListFragmentBase extends FragmentBase implements SwipeRefreshLayout.OnRefreshListener, CoursesView {

    @BindView(R.id.swipe_refresh_layout_mycourses)
    protected SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.list_of_courses)
    protected ListView listOfCoursesView;

    @BindView(R.id.report_problem)
    protected View reportConnectionProblem;

    @BindView(R.id.empty_courses)
    protected View emptyCoursesView;

    @BindView(R.id.empty_courses_button)
    protected Button findCourseButton;

    @BindView(R.id.root_fragment_view)
    protected TouchDispatchableFrameLayout rootView;

    @BindView(R.id.load_progressbar)
    protected ProgressBar progressBarOnEmptyScreen;

    @BindView(R.id.empty_search)
    protected ViewGroup emptySearch;

    protected List<Course> courses;
    protected CoursesAdapter coursesAdapter;

    protected boolean userScrolled;

    protected View footerDownloadingView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courses, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBarOnEmptyScreen.setVisibility(View.GONE);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon);

        if (courses == null) courses = new ArrayList<>();

        footerDownloadingView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.loading_view, null, false);
        footerDownloadingView.setVisibility(View.GONE);
        listOfCoursesView.addFooterView(footerDownloadingView);

        registerForContextMenu(listOfCoursesView);

        coursesAdapter = new CoursesAdapter(this, courses, getCourseType());
        listOfCoursesView.setAdapter(coursesAdapter);

        listOfCoursesView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true; // just for 1st creation
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount && userScrolled && StepikUtil.INSTANCE.isInternetAvailable()) {
                    //check inside more expensive condition:
                    onNeedDownloadNextPage();
                }
            }
        });

        listOfCoursesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= courses.size() || position < 0) return;
                Course course = courses.get(position);
                if (course.getEnrollment() != 0) {
                    shell.getScreenProvider().showSections(getActivity(), course);
                } else {
                    shell.getScreenProvider().showCourseDescription(CourseListFragmentBase.this, course);
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
        if (listOfCoursesView != null) {
            listOfCoursesView.setAdapter(null);
            listOfCoursesView.setOnScrollListener(null);
            listOfCoursesView.setOnItemClickListener(null);
        }
        super.onDestroyView();
    }

    protected abstract Table getCourseType();

    public final void updateEnrollment(Course courseForUpdate, long enrollment) {
        boolean inList = false;
        for (Course courseItem : courses) {
            if (courseItem.getCourseId() == courseForUpdate.getCourseId()) {
                courseItem.setEnrollment((int) courseItem.getCourseId());
                courseForUpdate = courseItem;
                inList = true;
                break;
            }
        }
        if (getCourseType() == Table.enrolled && !inList) {
            courses.add(courseForUpdate);
            coursesAdapter.notifyDataSetChanged();
        }

    }

    @Subscribe
    public void onSuccessJoin(SuccessJoinEvent e) {
        updateEnrollment(e.getCourse(), e.getCourse().getEnrollment());
    }

    @Override
    public void showLoading() {
        ProgressHelper.dismiss(progressBarOnEmptyScreen);
        ProgressHelper.dismiss(swipeRefreshLayout);
        footerDownloadingView.setVisibility(View.GONE);
        reportConnectionProblem.setVisibility(View.GONE);

        if (courses.isEmpty()) {
            ProgressHelper.activate(swipeRefreshLayout);
        } else if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            footerDownloadingView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showEmptyCourses() {
        ProgressHelper.dismiss(progressBarOnEmptyScreen);
        ProgressHelper.dismiss(swipeRefreshLayout);
        footerDownloadingView.setVisibility(View.GONE);
        reportConnectionProblem.setVisibility(View.GONE);
        if (courses.isEmpty()) {
            showEmptyScreen(true);
            localReminder.remindAboutApp();
        }
    }

    @Override
    public void showConnectionProblem() {
        ProgressHelper.dismiss(progressBarOnEmptyScreen);
        ProgressHelper.dismiss(swipeRefreshLayout);
        footerDownloadingView.setVisibility(View.GONE);

        if (courses == null || courses.isEmpty()) {
            //screen is clear due to error connection
            showEmptyScreen(false);
            reportConnectionProblem.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showCourses(List<Course> courses) {
        ProgressHelper.dismiss(progressBarOnEmptyScreen);
        ProgressHelper.dismiss(swipeRefreshLayout);
        footerDownloadingView.setVisibility(View.GONE);
        reportConnectionProblem.setVisibility(View.GONE);
        showEmptyScreen(false);
        List<Course> finalCourses = KotlinUtil.INSTANCE.getListOldPlusUpdated(this.courses, courses);
        this.courses.clear();
        this.courses.addAll(finalCourses);
        coursesAdapter.notifyDataSetChanged();
    }

    protected abstract void onNeedDownloadNextPage();

    protected abstract void showEmptyScreen(boolean isShow);
}
