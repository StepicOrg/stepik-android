package org.stepic.droid.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
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
import org.stepic.droid.view.activities.MainFeedActivity;
import org.stepic.droid.view.adapters.MyCoursesAdapter;
import org.stepic.droid.view.custom.TouchDispatchableFrameLayout;
import org.stepic.droid.web.CoursesStepicResponse;
import org.stepic.droid.web.IApi;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
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

    @Bind(R.id.report_problem)
    protected View mReportConnectionProblem;

    @Bind(R.id.empty_courses)
    protected View mEmptyCoursesView;

    @Bind(R.id.root_fragment_view)
    protected TouchDispatchableFrameLayout mRootView;

    //    protected LoadingCoursesTask mLoadingCoursesTask;
    protected List<Course> mCourses;
    protected MyCoursesAdapter mCoursesAdapter;
    protected int mCurrentPage;
    protected boolean mHasNextPage;
    protected FromDbCoursesTask mDbGetCoursesTask;
    protected ToDbCoursesTask mDbSaveCoursesTask;
    protected View mFooterDownloadingView;
    protected volatile boolean isLoading;

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
                Log.i(TAG, "user scrolled " + (userScrolled ? "true" : "false"));
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!isLoading && mHasNextPage && firstVisibleItem + visibleItemCount >= totalItemCount && userScrolled) {
                    Log.i(TAG, "Go load from scroll");
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
                    mShell.getScreenProvider().showCourseDescription(CoursesFragmentBase.this, course);
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

        bus.register(this);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                getAndShowDataFromCache();
            }
        });
    }

    protected void showCourses(List<Course> cachedCourses) {
        if (cachedCourses != null || cachedCourses.size() != 0) {
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

    protected abstract DatabaseManager.Table getCourseType();

    @Override
    public final void onRefresh() {
        YandexMetrica.reportEvent(AppConstants.METRICA_REFRESH_COURSE);
        mCurrentPage = 1;
        mHasNextPage = true;
        downloadData();
    }


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


        Log.i(TAG, "post pre load");
        bus.post(new PreLoadCoursesEvent(getCourseType()));
        if (getCourseType() == DatabaseManager.Table.featured) {
            api.getFeaturedCourses(mCurrentPage).enqueue(callback);
        } else {
            api.getEnrolledCourses(mCurrentPage).enqueue(callback);
        }
        Log.i(TAG, "mLoadingCoursesTask starts to execute");

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
        ProgressHelper.dismiss(mSwipeRefreshLayout);
        mFooterDownloadingView.setVisibility(View.GONE);
        isLoading = false;

        if (mCourses == null || mCourses.size() == 0) {
            //screen is clear due to error connection
            showEmptyScreen(false);
            mReportConnectionProblem.setVisibility(View.VISIBLE);
        }
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
        if (!inList) {
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
                        localRef.setEnrollment(0);
                        mDatabaseManager.addCourse(localRef, DatabaseManager.Table.featured);
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

    void showEmptyScreen(boolean isShowed) {
        if (isShowed) {
            mEmptyCoursesView.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setVisibility(View.GONE);
        } else {
            mEmptyCoursesView.setVisibility(View.GONE);
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);

        }
    }
}
