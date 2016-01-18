package org.stepic.droid.base;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.concurrency.FromDbCoursesTask;
import org.stepic.droid.concurrency.ToDbCoursesTask;
import org.stepic.droid.events.courses.FailCoursesDownloadEvent;
import org.stepic.droid.events.courses.FailDropCourseEvent;
import org.stepic.droid.events.courses.FinishingGetCoursesFromDbEvent;
import org.stepic.droid.events.courses.FinishingSaveCoursesToDbEvent;
import org.stepic.droid.events.courses.GettingCoursesFromDbSuccessEvent;
import org.stepic.droid.events.courses.PreLoadCoursesEvent;
import org.stepic.droid.events.courses.StartingGetCoursesFromDbEvent;
import org.stepic.droid.events.courses.StartingSaveCoursesToDbEvent;
import org.stepic.droid.events.courses.SuccessCoursesDownloadEvent;
import org.stepic.droid.events.courses.SuccessDropCourseEvent;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.JsonHelper;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.view.fragments.CourseListFragmentBase;
import org.stepic.droid.web.CoursesStepicResponse;

import java.util.List;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public abstract class CoursesDatabaseFragmentBase extends CourseListFragmentBase {
    private static final String TAG = "base_fragment";


    //    protected LoadingCoursesTask mLoadingCoursesTask;

    protected FromDbCoursesTask mDbGetCoursesTask;
    protected ToDbCoursesTask mDbSaveCoursesTask;


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        bus.register(this);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                getAndShowDataFromCache();
            }
        });
    }

    protected void showCourses(List<Course> cachedCourses) {
        if (cachedCourses == null) return;
        if (cachedCourses != null && !cachedCourses.isEmpty()) {
            showEmptyScreen(false);
            mReportConnectionProblem.setVisibility(View.GONE);
        }

        mCourses.clear();
        if (getCourseType() == DatabaseManager.Table.enrolled) {
            for (Course course : cachedCourses) {
                if (course.getEnrollment() != 0)
                    mCourses.add(course);
            }
        } else {
            mCourses.addAll(cachedCourses);
        }

        mCoursesAdapter.notifyDataSetChanged();
    }

    private void saveDataToCache(List<Course> courses) {
        mDbSaveCoursesTask = new ToDbCoursesTask(courses, getCourseType(), mCurrentPage);
        mDbSaveCoursesTask.execute();
    }


    public void getAndShowDataFromCache() {
        mDbGetCoursesTask = new FromDbCoursesTask(getCourseType()) {
            @Override
            protected void onSuccess(List<Course> courses) {
                super.onSuccess(courses);
                bus.post(new GettingCoursesFromDbSuccessEvent(getCourseType(), courses));
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

            mHasNextPage = coursesStepicResponse.getMeta().isHas_next();
            if (mHasNextPage) {
                mCurrentPage = coursesStepicResponse.getMeta().getPage() + 1;
            }
        } else {
            mHasNextPage = false;
            mReportConnectionProblem.setVisibility(View.GONE);
            showEmptyScreen(true);

            mFooterDownloadingView.setVisibility(View.GONE);
            ProgressHelper.dismiss(mSwipeRefreshLayout);
        }
        isLoading = false;
    }

    @Subscribe
    public void onFailureDataLoad(FailCoursesDownloadEvent e) {
        super.onFailureDataLoad(e);
    }


    @Subscribe
    public void onStartingSaveToDb(StartingSaveCoursesToDbEvent e) {
        ProgressHelper.activate(mSwipeRefreshLayout);
    }

    @Subscribe
    public void onFinishingSaveToDb(FinishingSaveCoursesToDbEvent e) {
        ProgressHelper.dismiss(mSwipeRefreshLayout);
        getAndShowDataFromCache();
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
        Log.d("courses database", "update after join");
        updateEnrollment(e.getCourse(), e.getCourse().getEnrollment());
    }

    private void updateEnrollment(Course courseForUpdate, long enrollment) {


        boolean inList = false;
        for (Course courseItem : mCourses) {
            if (courseItem.getCourseId() == courseForUpdate.getCourseId()) {
                courseItem.setEnrollment((int) courseItem.getCourseId());
                courseForUpdate = courseItem;
                inList = true;
                break;
            }
        }

        if (getCourseType() == DatabaseManager.Table.enrolled && !inList) {
                mCourses.add(courseForUpdate);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        mSwipeRefreshLayout.setRefreshing(false);
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
    }

    @Override
    public void onDestroyView() {
        bus.unregister(this);
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
        YandexMetrica.reportEvent(AppConstants.METRICA_LONG_TAP_COURSE);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
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
        final Course course = mCourses.get(position);
        if (course.getEnrollment() == 0) {
            Toast.makeText(getContext(), R.string.you_not_enrolled, Toast.LENGTH_LONG).show();
            return;
        }
        mShell.getApi().dropCourse(course.getCourseId()).enqueue(new Callback<Void>() {

            Course localRef = course;

            @Override
            public void onResponse(Response<Void> response, Retrofit retrofit) {

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mDatabaseManager.deleteCourse(localRef, DatabaseManager.Table.enrolled);

//                        if (!course.is_featured()){
//                            localRef.setEnrollment(0);
//                            mDatabaseManager.addCourse(localRef, DatabaseManager.Table.featured);}
//                        else{
//                            mDatabaseManager.deleteCourse(localRef, DatabaseManager.Table.featured);
//                        }

                    }
                });

                bus.post(new SuccessDropCourseEvent(getCourseType(), localRef));
            }

            @Override
            public void onFailure(Throwable t) {
                bus.post(new FailDropCourseEvent(getCourseType(), localRef));
            }
        });
    }

    @Subscribe
    public void onSuccessDrop(final SuccessDropCourseEvent e) {
        YandexMetrica.reportEvent(AppConstants.METRICA_DROP_COURSE + " successful", JsonHelper.toJson(e.getCourse()));
        Toast.makeText(getContext(), getContext().getString(R.string.you_dropped) + " " + e.getCourse().getTitle(), Toast.LENGTH_LONG).show();
        if (e.getType() == DatabaseManager.Table.enrolled) {
            mCourses.remove(e.getCourse());
            mCoursesAdapter.notifyDataSetChanged();
        }

        if (mCourses.size() == 0) {
            showEmptyScreen(true);
        }
    }

    @Subscribe
    public void onFailDrop(FailDropCourseEvent e) {
        YandexMetrica.reportEvent(AppConstants.METRICA_DROP_COURSE + " fail", JsonHelper.toJson(e.getCourse()));
        Toast.makeText(getContext(), R.string.try_in_web_drop, Toast.LENGTH_LONG).show();
    }

    private void showInfo(int position) {
        YandexMetrica.reportEvent(AppConstants.SHOW_DETAILED_INFO_CLICK);
        Course course = mCourses.get(position);
        mShell.getScreenProvider().showCourseDescription(this, course);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("result", "onActivityResult: ");
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == AppConstants.REQUEST_CODE_DETAIL) {
                Log.i("result", "reaction in courses fragment base");
                Course course = data.getParcelableExtra(AppConstants.COURSE_ID_KEY);
                int enrollment = data.getIntExtra(AppConstants.ENROLLMENT_KEY, 0);
                if (course != null && enrollment != 0) {
                    updateEnrollment(course, enrollment);
                }

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
}
