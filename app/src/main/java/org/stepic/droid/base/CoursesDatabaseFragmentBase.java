package org.stepic.droid.base;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.core.CourseListModule;
import org.stepic.droid.core.presenters.PersistentCourseListPresenter;
import org.stepic.droid.core.presenters.contracts.FilterForCoursesView;
import org.stepic.droid.events.courses.FailDropCourseEvent;
import org.stepic.droid.events.courses.SuccessDropCourseEvent;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.Table;
import org.stepic.droid.ui.fragments.CourseListFragmentBase;
import org.stepic.droid.util.AppConstants;

import java.util.List;

import javax.inject.Inject;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public abstract class CoursesDatabaseFragmentBase extends CourseListFragmentBase implements FilterForCoursesView {
    private static final int FILTER_REQUEST_CODE = 776;

    private boolean needFilter = false;

    @Inject
    PersistentCourseListPresenter courseListPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        MainApplication.component()
                .plus(new CourseListModule())
                .inject(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.my_courses_menu, menu); //hide in 1.15
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter_menu:
                mShell.getScreenProvider().showFilterScreen(this, FILTER_REQUEST_CODE, getCourseType());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    @Subscribe
    public void onSuccessJoin(SuccessJoinEvent e) {
        //We do not upgrade database, because when
        //Only for find courses event.
        super.onSuccessJoin(e);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bus.register(this);
        courseListPresenter.attachView(this);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                courseListPresenter.downloadData(getCourseType(), needFilter);
            }
        });
        courseListPresenter.restoreState();
    }

    @Override
    public void onStart() {
        super.onStart();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        bus.unregister(this);
        courseListPresenter.detachView(this);
        super.onDestroyView();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        MenuInflater inflater = getActivity().getMenuInflater();
        if (mCourses.get(info.position).getEnrollment() != 0) {
            inflater.inflate(R.menu.course_context_menu, menu);
        } else {
            inflater.inflate(R.menu.course_context_not_enrolled_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        analytic.reportEvent(Analytic.Interaction.LONG_TAP_COURSE);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
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
        if (position >= mCourses.size() || position < 0) {
            Toast.makeText(getContext(), R.string.try_in_web_drop, Toast.LENGTH_LONG).show();
            return;
        }
        final Course course = mCourses.get(position);
        if (course.getEnrollment() == 0) {
            Toast.makeText(getContext(), R.string.you_not_enrolled, Toast.LENGTH_LONG).show();
            return;
        }
        Call<Void> drop = mShell.getApi().dropCourse(course.getCourseId());
        if (drop != null) {
            drop.enqueue(new Callback<Void>() {
                Course localRef = course;

                @Override
                public void onResponse(Response<Void> response, Retrofit retrofit) {

                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            mDatabaseFacade.deleteCourse(localRef, Table.enrolled);

                            if (mDatabaseFacade.getCourseById(course.getCourseId(), Table.featured) != null) {
                                localRef.setEnrollment(0);
                                mDatabaseFacade.addCourse(localRef, Table.featured);
                            }

                        }
                    });

                    bus.post(new SuccessDropCourseEvent(getCourseType(), localRef));
                }

                @Override
                public void onFailure(Throwable t) {
                    bus.post(new FailDropCourseEvent(getCourseType(), localRef));
                }
            });
        } else {
            Toast.makeText(MainApplication.getAppContext(), R.string.cant_drop, Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void onSuccessDrop(final SuccessDropCourseEvent e) {
        long courseId = -1L;
        if (e.getCourse() != null) {
            courseId = e.getCourse().getCourseId();
        }
        analytic.reportEvent(Analytic.Web.DROP_COURSE_SUCCESSFUL, courseId + "");
        Toast.makeText(getContext(), getContext().getString(R.string.you_dropped) + " " + e.getCourse().getTitle(), Toast.LENGTH_LONG).show();
        if (e.getType() == Table.enrolled) {
            mCourses.remove(e.getCourse());
            mCoursesAdapter.notifyDataSetChanged();
        }

        if (mCourses.size() == 0) {
            showEmptyScreen(true);
        }
    }

    @Subscribe
    public void onFailDrop(FailDropCourseEvent e) {
        long courseId = -1L;
        if (e.getCourse() != null) {
            courseId = e.getCourse().getCourseId();
        }
        analytic.reportEvent(Analytic.Web.DROP_COURSE_FAIL, courseId + "");
        Toast.makeText(getContext(), R.string.internet_problem, Toast.LENGTH_LONG).show();
    }

    private void showInfo(int position) {
        analytic.reportEvent(Analytic.Interaction.SHOW_DETAILED_INFO_CLICK);
        Course course = mCourses.get(position);
        mShell.getScreenProvider().showCourseDescription(this, course);
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
                needFilter = true; // not last filter? check it
                mCourses.clear();
                mCoursesAdapter.notifyDataSetChanged();
                courseListPresenter.refreshData(getCourseType(), needFilter);
            }
        }
    }

    @Override
    public void showEmptyScreen(boolean isShowed) {
        if (isShowed) {
            mEmptyCoursesView.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setVisibility(View.GONE);
        } else {
            mEmptyCoursesView.setVisibility(View.GONE);
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNeedDownloadNextPage() {
        courseListPresenter.downloadData(getCourseType(), needFilter);
    }

    @Override
    public void clearAndShowLoading() {
        mCourses.clear();
        mCoursesAdapter.notifyDataSetChanged();
        showLoading();
    }

    @Override
    public void showFilteredCourses(@NotNull List<Course> filteredList) {
        showCourses(filteredList);
    }

    @Override
    public void onRefresh() {
        courseListPresenter.refreshData(getCourseType(), needFilter);
    }

    @Override
    public void onDestroy() {
        mSharedPreferenceHelper.onTryDiscardFilters(getCourseType());
        super.onDestroy();
    }
}
