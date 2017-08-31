package org.stepic.droid.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.core.dropping.contract.DroppingListener;
import org.stepic.droid.core.dropping.contract.DroppingPoster;
import org.stepic.droid.core.presenters.PersistentCourseListPresenter;
import org.stepic.droid.core.presenters.contracts.FilterForCoursesView;
import org.stepic.droid.model.Course;
import org.stepic.droid.storage.operations.Table;
import org.stepic.droid.ui.adapters.CoursesAdapter;
import org.stepic.droid.ui.fragments.CourseListFragmentBase;
import org.stepic.droid.ui.util.BackButtonHandler;
import org.stepic.droid.ui.util.ContextMenuRecyclerView;
import org.stepic.droid.ui.util.OnBackClickListener;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ContextMenuCourseUtil;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class CoursesDatabaseFragmentBase extends CourseListFragmentBase implements FilterForCoursesView, OnBackClickListener, DroppingListener {
    private static final int FILTER_REQUEST_CODE = 776;

    private boolean needFilter = false;

    @Inject
    PersistentCourseListPresenter courseListPresenter;

    @Inject
    DroppingPoster droppingPoster;

    @Inject
    Client<DroppingListener> droppingClient;

    private BackButtonHandler backButtonHandler = null;
    private boolean isScreenCreated;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity rootActivity = getActivity();
        if (rootActivity != null && rootActivity instanceof BackButtonHandler) {
            backButtonHandler = ((BackButtonHandler) rootActivity);
            backButtonHandler.setBackClickListener(this);
        }
    }

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
    protected void onReleaseComponent() {
        App.Companion
                .componentManager()
                .releaseCourseGeneralComponent();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        isScreenCreated = true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.my_courses_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter_menu:
                getScreenManager().showFilterScreen(this, FILTER_REQUEST_CODE, getCourseType());
                return true;
        }

        return super.onOptionsItemSelected(item);
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
        if (isScreenCreated) {
            //reset all data
            isScreenCreated = false;
            needFilter = false;
            courses.clear();
            courseListPresenter.refreshData(getCourseType(), needFilter, false);
        } else {
            //load if not
            courseListPresenter.downloadData(getCourseType(), needFilter);
        }
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
        if (position >= courses.size() && position < 0) {
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
            Toast.makeText(getContext(), R.string.try_in_web_drop, Toast.LENGTH_SHORT).show();
            return;
        }
        final Course course = courses.get(position);
        if (course.getEnrollment() == 0) {
            Toast.makeText(getContext(), R.string.you_not_enrolled, Toast.LENGTH_SHORT).show();
            return;
        }
        Call<Void> drop = getApi().dropCourse(course.getCourseId());
        if (drop != null) {
            drop.enqueue(new Callback<Void>() {
                Course localRef = course;

                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    getThreadPoolExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            getDatabaseFacade().deleteCourse(localRef, Table.enrolled);

                            if (getDatabaseFacade().getCourseById(course.getCourseId(), Table.featured) != null) {
                                localRef.setEnrollment(0);
                                getDatabaseFacade().addCourse(localRef, Table.featured);
                            }

                        }
                    });

                    droppingPoster.successDropCourse(localRef);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    droppingPoster.failDropCourse(localRef);
                }
            });
        } else {
            Toast.makeText(getContext(), R.string.cant_drop, Toast.LENGTH_SHORT).show();
        }
    }

    private void showInfo(int position) {
        getAnalytic().reportEvent(Analytic.Interaction.SHOW_DETAILED_INFO_CLICK);
        Course course = courses.get(position);
        getScreenManager().showCourseDescription(this, course);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == FragmentActivity.RESULT_OK) {
            if (requestCode == AppConstants.REQUEST_CODE_DETAIL) {
                Course course = data.getParcelableExtra(AppConstants.COURSE_ID_KEY);
                int enrollment = data.getIntExtra(AppConstants.ENROLLMENT_KEY, 0);
                if (course != null && enrollment != 0) {
                    updateEnrollment(course, enrollment);
                }
            }

            if (requestCode == FILTER_REQUEST_CODE) {
                getAnalytic().reportEvent(Analytic.Filters.FILTERS_NEED_UPDATE);
                needFilter = true; // not last filter? check it
                courses.clear();
                coursesAdapter.notifyDataSetChanged();
                courseListPresenter.reportCurrentFiltersToAnalytic(getCourseType());
                courseListPresenter.refreshData(getCourseType(), needFilter, false);
            }
        }

        if (resultCode == FragmentActivity.RESULT_CANCELED) {
            if (requestCode == FILTER_REQUEST_CODE) {
                getAnalytic().reportEvent(Analytic.Filters.FILTERS_CANCELED);
            }
        }
    }

    @Override
    public void showEmptyScreen(boolean isShown) {
        if (isShown) {
            if (getCourseType() == Table.enrolled) {
                setBackgroundColorToRootView(R.color.old_cover);
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
                setBackgroundColorToRootView(R.color.old_cover);
                emptyCoursesView.setVisibility(View.GONE);
                emptySearch.setVisibility(View.VISIBLE);
            }
            swipeRefreshLayout.setVisibility(View.GONE);
        } else {
            setBackgroundColorToRootView(R.color.new_cover);
            emptySearch.setVisibility(View.GONE);
            emptyCoursesView.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNeedDownloadNextPage() {
        courseListPresenter.loadMore(getCourseType(), needFilter);
    }

    @Override
    public void clearAndShowLoading() {
        courses.clear();
        coursesAdapter.notifyDataSetChanged();
        showLoading();
    }

    @Override
    public void showFilteredCourses(@NotNull List<Course> filteredList) {
        showCourses(filteredList);
    }

    @Override
    public void onRefresh() {
        getAnalytic().reportEvent(Analytic.Interaction.PULL_TO_REFRESH_COURSE);
        courseListPresenter.refreshData(getCourseType(), needFilter, true);
    }

    @Override
    public boolean onBackClick() {
        getSharedPreferenceHelper().onTryDiscardFilters(getCourseType());
        return false;
    }

    @Override
    public void onDetach() {
        if (backButtonHandler != null) {
            backButtonHandler.removeBackClickListener(this);
            backButtonHandler = null;
        }
        super.onDetach();
    }

    @Override
    public void onFailDropCourse(@NotNull Course droppedCourse) {
        long courseId = -1L;
        courseId = droppedCourse.getCourseId();
        getAnalytic().reportEvent(Analytic.Web.DROP_COURSE_FAIL, courseId + "");
        Toast.makeText(getContext(), R.string.internet_problem, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuccessDropCourse(@NotNull Course droppedCourse) {
        long courseId = -1L;
        courseId = droppedCourse.getCourseId();
        getAnalytic().reportEvent(Analytic.Web.DROP_COURSE_SUCCESSFUL, courseId + "");
        Toast.makeText(getContext(), getContext().getString(R.string.you_dropped) + " " + droppedCourse.getTitle(), Toast.LENGTH_LONG).show();
        if (getCourseType() == Table.enrolled) { //why here was e.getCourseType?
            courses.remove(droppedCourse);
            coursesAdapter.notifyDataSetChanged();
        } else if (getCourseType() == Table.featured) {
            int position = -1;
            for (int i = 0; i < courses.size(); i++) {
                Course courseItem = courses.get(i);
                if (courseItem.getCourseId() == droppedCourse.getCourseId()) {
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

    @Override
    public CoursesAdapter getCoursesAdapter() {
        return new CoursesAdapter(this, courses, getCourseType(), continueCoursePresenter, droppingPoster);
    }
}
