package org.stepic.droid.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import timber.log.Timber;

public abstract class CourseListFragmentBase extends FragmentBase implements SwipeRefreshLayout.OnRefreshListener, CoursesView {

    @BindView(R.id.swipe_refresh_layout_mycourses)
    protected SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.list_of_courses)
    protected RecyclerView listOfCoursesView;

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
    private RecyclerView.OnScrollListener listOfCoursesViewListener;
    private LinearLayoutManager layoutManager;

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

        registerForContextMenu(listOfCoursesView);

        coursesAdapter = new CoursesAdapter(this, courses, getCourseType());
        listOfCoursesView.setAdapter(coursesAdapter);
        layoutManager = new LinearLayoutManager(getContext());
        listOfCoursesView.setLayoutManager(layoutManager);

        listOfCoursesViewListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) //check for scroll down
                {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    Timber.d("visibleItemCount = %d, totalItemCount = %d, pastVisibleItems=%d", visibleItemCount, totalItemCount, pastVisibleItems);

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount && StepikUtil.INSTANCE.isInternetAvailable()) {
                        onNeedDownloadNextPage();
                    }
                }

            }
        };
        listOfCoursesView.addOnScrollListener(listOfCoursesViewListener);


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
        coursesAdapter.showLoadingFooter(false);
        reportConnectionProblem.setVisibility(View.GONE);

        if (courses.isEmpty()) {
            ProgressHelper.activate(swipeRefreshLayout);
        } else if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            coursesAdapter.showLoadingFooter(true);
        }
    }

    @Override
    public void showEmptyCourses() {
        ProgressHelper.dismiss(progressBarOnEmptyScreen);
        ProgressHelper.dismiss(swipeRefreshLayout);
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
        coursesAdapter.showLoadingFooter(true);

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
        coursesAdapter.showLoadingFooter(false);
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
