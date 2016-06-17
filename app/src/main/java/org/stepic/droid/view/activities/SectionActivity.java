package org.stepic.droid.view.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.concurrency.tasks.FromDbSectionTask;
import org.stepic.droid.concurrency.tasks.ToDbSectionTask;
import org.stepic.droid.events.notify_ui.NotifyUISectionsEvent;
import org.stepic.droid.events.sections.FailureResponseSectionEvent;
import org.stepic.droid.events.sections.FinishingGetSectionFromDbEvent;
import org.stepic.droid.events.sections.FinishingSaveSectionToDbEvent;
import org.stepic.droid.events.sections.NotCachedSectionEvent;
import org.stepic.droid.events.sections.SectionCachedEvent;
import org.stepic.droid.events.sections.StartingSaveSectionToDbEvent;
import org.stepic.droid.events.sections.SuccessResponseSectionsEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Section;
import org.stepic.droid.notifications.model.Notification;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.view.adapters.SectionAdapter;
import org.stepic.droid.web.SectionsStepicResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class SectionActivity extends FragmentActivityBase implements SwipeRefreshLayout.OnRefreshListener, OnRequestPermissionsResultCallback {

    @Bind(R.id.swipe_refresh_layout_units)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.sections_recycler_view)
    RecyclerView mSectionsRecyclerView;

    @Bind(R.id.load_progressbar)
    ProgressBar mProgressBar;

    @Bind(R.id.toolbar)
    android.support.v7.widget.Toolbar mToolbar;

    @Bind(R.id.report_problem)
    protected View mReportConnectionProblem;

    @Bind(R.id.report_empty)
    protected View mReportEmptyView;

    private Course mCourse;
    private SectionAdapter mAdapter;
    private List<Section> mSectionList;

    boolean isScreenEmpty;
    boolean firstLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section);
        ButterKnife.bind(this);
        overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);
        hideSoftKeypad();
        isScreenEmpty = true;
        firstLoad = true;


        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon);


        mSectionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSectionList = new ArrayList<>();
        mAdapter = new SectionAdapter(mSectionList, this, this);
        mSectionsRecyclerView.setAdapter(mAdapter);

        ProgressHelper.activate(mProgressBar);
        bus.register(this);
        onNewIntent(getIntent());
    }

    private void setUpToolbar() {
        if (mCourse != null && mCourse.getTitle() != null && !mCourse.getTitle().isEmpty()) {
            setTitle(mCourse.getTitle());
        }
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mCourse = (Course) (intent.getExtras().get(AppConstants.KEY_COURSE_BUNDLE));

        if (intent.getAction() != null && intent.getAction().equals(AppConstants.OPEN_NOTIFICATION)) {
            final long courseId = mCourse.getCourseId();
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    List<Notification> notifications = mDbManager.getAllNotificationsOfCourse(courseId);
                    notificationManager.discardAllNotifications(courseId);
                    for (Notification notificationItem : notifications) {
                        if (notificationItem != null && notificationItem.getId() != null) {
                            try {
                                mShell.getApi().markNotificationAsRead(notificationItem.getId(), true).execute();
                            } catch (IOException e) {
                                YandexMetrica.reportError("notification is not posted", e);
                            }
                        }
                    }
                    return null;
                }
            };
            task.executeOnExecutor(mThreadPoolExecutor);
        }
        setUpToolbar();
        getAndShowSectionsFromCache();
    }

    @Subscribe
    public void onNotifyUI(NotifyUISectionsEvent event) {
        dismissReportView();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                mShell.getScreenProvider().showCourseDescription(this, mCourse);
                return true;

            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void updateSections() {
        long[] sections = mCourse.getSections();
        if (sections == null || sections.length == 0) {
            mReportEmptyView.setVisibility(View.VISIBLE);
            ProgressHelper.dismiss(mProgressBar);
            ProgressHelper.dismiss(mSwipeRefreshLayout);
        } else {
            mReportEmptyView.setVisibility(View.GONE);
            mShell.getApi().getSections(mCourse.getSections()).enqueue(new Callback<SectionsStepicResponse>() {
                @Override
                public void onResponse(Response<SectionsStepicResponse> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        bus.post(new SuccessResponseSectionsEvent(mCourse, response, retrofit));
                    } else {
                        bus.post(new FailureResponseSectionEvent(mCourse));
                    }

                }

                @Override
                public void onFailure(Throwable t) {
                    bus.post(new FailureResponseSectionEvent(mCourse));
                }
            });
        }
    }

    private void getAndShowSectionsFromCache() {
        FromDbSectionTask fromDbSectionTask = new FromDbSectionTask(mCourse);
        fromDbSectionTask.executeOnExecutor(mThreadPoolExecutor);
    }

    private void showSections(List<Section> sections) {
        mSectionList.clear();
        mSectionList.addAll(sections);
        dismissReportView();
        mAdapter.notifyDataSetChanged();
        dismiss();
    }

    private void dismiss() {
        if (isScreenEmpty) {
            ProgressHelper.dismiss(mProgressBar);
            isScreenEmpty = false;
        } else {
            ProgressHelper.dismiss(mSwipeRefreshLayout);
        }
    }

    private void dismissReportView() {
        if (mSectionList != null && mSectionList.size() != 0) {
            mReportConnectionProblem.setVisibility(View.GONE);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }

    @Override
    public void onRefresh() {
        YandexMetrica.reportEvent(AppConstants.METRICA_REFRESH_SECTION);
        updateSections();
    }


    private void saveDataToCache(List<Section> sections) {
        ToDbSectionTask toDbSectionTask = new ToDbSectionTask(sections);
        toDbSectionTask.executeOnExecutor(mThreadPoolExecutor);
    }

    @Subscribe
    public void onFailureDownload(FailureResponseSectionEvent e) {
        if (mCourse.getCourseId() == e.getCourse().getCourseId()) {
            if (mSectionList != null && mSectionList.size() == 0) {
                mReportConnectionProblem.setVisibility(View.VISIBLE);
            }
            dismiss();
        }
    }

    @Subscribe
    public void onGettingFromDb(FinishingGetSectionFromDbEvent event) {
        if (event.getCourse().getCourseId() != mCourse.getCourseId()) return;

        List<Section> sections = event.getSectionList();

        if (sections != null && sections.size() != 0) {
            showSections(sections);
            if (firstLoad) {
                firstLoad = false;
                updateSections();
            }
        } else {
            updateSections();
        }

    }


    @Override
    protected void onStop() {
        super.onStop();
        ProgressHelper.dismiss(mSwipeRefreshLayout);
    }

    @Override
    protected void onDestroy() {
        bus.unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onSuccessDownload(SuccessResponseSectionsEvent e) {
        if (mCourse.getCourseId() == e.getCourse().getCourseId()) {
            SectionsStepicResponse stepicResponse = e.getResponse().body();
            List<Section> sections = stepicResponse.getSections();
            saveDataToCache(sections);
        }
    }

    @Subscribe
    public void onStartSaveToDb(StartingSaveSectionToDbEvent e) {
    }

    @Subscribe
    public void onFinishSaveToDb(FinishingSaveSectionToDbEvent e) {
        getAndShowSectionsFromCache();
    }


    @Subscribe
    public void onSectionCached(SectionCachedEvent e) {
        long sectionId = e.getSectionId();
        updateState(sectionId, true, false);
    }

    @Subscribe
    public void onNotCachedSection(NotCachedSectionEvent e) {
        long sectionId = e.getSectionId();
        updateState(sectionId, false, false);
    }


    private void updateState(long sectionId, boolean isCached, boolean isLoading) {

        int position = -1;
        Section section = null;
        for (int i = 0; i < mSectionList.size(); i++) {
            if (mSectionList.get(i).getId() == sectionId) {
                position = i;
                section = mSectionList.get(i);
                break;
            }
        }
        if (section == null || position == -1 || position >= mSectionList.size()) return;

        //now we have not null section and correct position at list
        section.set_cached(isCached);
        section.set_loading(isLoading);
        mAdapter.notifyItemChanged(position);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.section_unit_menu, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AppConstants.REQUEST_EXTERNAL_STORAGE) {
            String permissionExternalStorage = permissions[0];
            if (permissionExternalStorage == null) return;

            if (permissionExternalStorage.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                int position = mShell.getSharedPreferenceHelper().getTempPosition();
                if (mAdapter != null) {
                    mAdapter.requestClickLoad(position);
                }
            }
        }
    }
}
