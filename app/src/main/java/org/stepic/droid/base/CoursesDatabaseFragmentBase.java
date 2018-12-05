package org.stepic.droid.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.AmplitudeAnalytic;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.core.dropping.contract.DroppingListener;
import org.stepic.droid.core.presenters.PersistentCourseListPresenter;
import org.stepic.droid.model.CourseListType;
import org.stepik.android.model.Course;
import org.stepic.droid.ui.fragments.CourseListFragmentBase;
import org.stepic.droid.ui.util.ContextMenuRecyclerView;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ContextMenuCourseUtil;

import javax.inject.Inject;

import kotlin.Pair;
import kotlin.collections.MapsKt;

public abstract class CoursesDatabaseFragmentBase extends CourseListFragmentBase implements DroppingListener {
    @Inject
    PersistentCourseListPresenter courseListPresenter;

    @Inject
    Client<DroppingListener> droppingClient;


    @Override
    protected void injectComponent() {
        App.Companion
                .componentManager()
                .courseGeneralComponent()
                .courseListComponentBuilder()
                .build()
                .inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        nullifyActivityBackground();
        super.onViewCreated(view, savedInstanceState);
        droppingClient.subscribe(this);
        courseListPresenter.attachView(this);
        courseListPresenter.restoreState();
    }

    @Override
    public void onStart() {
        super.onStart();
        courseListPresenter.downloadData(getCourseType());
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        droppingClient.unsubscribe(this);
        courseListPresenter.detachView(this);
        super.onDestroyView();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        ContextMenuRecyclerView.RecyclerViewContextMenuInfo info = (ContextMenuRecyclerView.RecyclerViewContextMenuInfo) menuInfo;
        if (info == null) {
            return;
        }
        int position = info.position;
        if (position >= courses.size() || position < 0) {
            return; // the context will not be displayed
        }

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(ContextMenuCourseUtil.INSTANCE.getMenuResource(courses.get(position)), menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        getAnalytic().reportEvent(Analytic.Interaction.LONG_TAP_COURSE);
        ContextMenuRecyclerView.RecyclerViewContextMenuInfo info = (ContextMenuRecyclerView.RecyclerViewContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.menu_item_info:
                showInfo(info.position);
                return true;
            case R.id.menu_item_unroll:
                dropCourse(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void dropCourse(int position) {
        if (position >= courses.size() || position < 0) {
            //tbh, it should be illegal state
            return;
        }
        final Course course = courses.get(position);
        droppingPresenter.dropCourse(course);
    }

    private void showInfo(int position) {
        getAnalytic().reportEvent(Analytic.Interaction.SHOW_DETAILED_INFO_CLICK);
        Course course = courses.get(position);
        getScreenManager().showCourseDescription(this, course);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == FragmentActivity.RESULT_OK && requestCode == AppConstants.REQUEST_CODE_DETAIL) {
            Course course = data.getParcelableExtra(AppConstants.COURSE_ID_KEY);
            int enrollment = data.getIntExtra(AppConstants.ENROLLMENT_KEY, 0);
            if (course != null && enrollment != 0) {
                updateEnrollment(course, enrollment);
            }
        }
    }

    @Override
    public void showEmptyScreen(boolean isShown) {
        if (isShown) {
            if (getCourseType() == CourseListType.ENROLLED) {
                emptyCoursesView.setVisibility(View.VISIBLE);
                if (getSharedPreferenceHelper().getAuthResponseFromStore() != null) { //// TODO: 23.12.16 optimize it and do on background thread
                    //logged
                    emptyCoursesTextView.setText(R.string.empty_courses);
                    signInButton.setVisibility(View.GONE);
                } else {
                    //anonymous
                    emptyCoursesTextView.setText(R.string.empty_courses_anonymous);
                    signInButton.setVisibility(View.VISIBLE);
                }
                emptySearch.setVisibility(View.GONE);
            } else {
                emptyCoursesView.setVisibility(View.GONE);
                emptySearch.setVisibility(View.VISIBLE);
            }
            swipeRefreshLayout.setVisibility(View.GONE);
        } else {
            emptySearch.setVisibility(View.GONE);
            emptyCoursesView.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNeedDownloadNextPage() {
        courseListPresenter.loadMore(getCourseType());
    }

    @Override
    public void onRefresh() {
        getAnalytic().reportEvent(Analytic.Interaction.PULL_TO_REFRESH_COURSE);
        courseListPresenter.refreshData(getCourseType());
    }

    @Override
    public void onFailDropCourse(@NotNull Course droppedCourse) {
        long courseId = droppedCourse.getId();
        getAnalytic().reportEvent(Analytic.Course.DROP_COURSE_FAIL, courseId + "");
        Toast.makeText(getContext(), R.string.internet_problem, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuccessDropCourse(@NotNull Course droppedCourse) {
        long courseId = droppedCourse.getId();
        getAnalytic().reportEvent(Analytic.Course.DROP_COURSE_SUCCESSFUL, courseId + "");
        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Course.UNSUBSCRIBED,
                MapsKt.mapOf(new Pair<String, Object>(AmplitudeAnalytic.Course.Params.COURSE, courseId)));

        Toast.makeText(getContext(), getContext().getString(R.string.you_dropped, droppedCourse.getTitle()), Toast.LENGTH_LONG).show();
        if (getCourseType() == CourseListType.ENROLLED) { //why here was e.getCourseType?
            courses.remove(droppedCourse);
            coursesAdapter.notifyDataSetChanged();
        } else if (getCourseType() == CourseListType.FEATURED) {
            int position = -1;
            for (int i = 0; i < courses.size(); i++) {
                Course courseItem = courses.get(i);
                if (courseItem.getId() == droppedCourse.getId()) {
                    courseItem.setEnrollment(0);
                    position = i;
                    break;
                }
            }
            if (position >= 0 && position < courses.size()) {
                coursesAdapter.notifyItemChanged(position);
            }
        }


        if (courses.size() == 0) {
            showEmptyScreen(true);
        }
    }


    @NotNull
    @Override
    protected abstract CourseListType getCourseType();
}
