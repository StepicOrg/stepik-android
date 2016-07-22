package org.stepic.droid.view.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.DraweeView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.otto.Subscribe;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.concurrency.tasks.FromDbSectionTask;
import org.stepic.droid.concurrency.tasks.ToDbSectionTask;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.core.CalendarExportableView;
import org.stepic.droid.core.CalendarPresenter;
import org.stepic.droid.core.ShareHelper;
import org.stepic.droid.events.courses.CourseCantLoadEvent;
import org.stepic.droid.events.courses.CourseFoundEvent;
import org.stepic.droid.events.courses.CourseUnavailableForUserEvent;
import org.stepic.droid.events.courses.SuccessDropCourseEvent;
import org.stepic.droid.events.joining_course.FailJoinEvent;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.events.sections.FailureResponseSectionEvent;
import org.stepic.droid.events.sections.FinishingGetSectionFromDbEvent;
import org.stepic.droid.events.sections.FinishingSaveSectionToDbEvent;
import org.stepic.droid.events.sections.NotCachedSectionEvent;
import org.stepic.droid.events.sections.SectionCachedEvent;
import org.stepic.droid.events.sections.SuccessResponseSectionsEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Section;
import org.stepic.droid.notifications.model.Notification;
import org.stepic.droid.presenters.course_finder.CourseFinderPresenter;
import org.stepic.droid.presenters.course_joiner.CourseJoinerPresenter;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.HtmlHelper;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.StepicLogicHelper;
import org.stepic.droid.util.StringUtil;
import org.stepic.droid.view.abstraction.CourseJoinView;
import org.stepic.droid.view.abstraction.LoadCourseView;
import org.stepic.droid.view.adapters.SectionAdapter;
import org.stepic.droid.view.dialogs.LoadingProgressDialog;
import org.stepic.droid.view.dialogs.UnauthorizedDialogFragment;
import org.stepic.droid.web.SectionsStepicResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class SectionActivity extends FragmentActivityBase implements SwipeRefreshLayout.OnRefreshListener, OnRequestPermissionsResultCallback, LoadCourseView, CourseJoinView, CalendarExportableView {

    @BindView(R.id.swipe_refresh_layout_units)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.sections_recycler_view)
    RecyclerView mSectionsRecyclerView;

    @BindView(R.id.load_progressbar)
    ProgressBar loadOnCenterProgressBar;

    @BindView(R.id.toolbar)
    android.support.v7.widget.Toolbar mToolbar;

    @BindView(R.id.report_problem)
    protected View reportConnectionProblem;

    @BindView(R.id.course_not_found)
    View courseNotParsedView;

    @BindView(R.id.report_empty)
    protected View mReportEmptyView;

    @BindView(R.id.join_course_root)
    protected View joinCourseRoot; // default state is gone

    @BindView(R.id.join_course_layout)
    protected View joinCourseButton;

    @BindView(R.id.courseIcon)
    protected DraweeView courseIcon;

    @BindView(R.id.course_name)
    protected TextView courseName;

    @Nullable
    private Course mCourse;
    private SectionAdapter mAdapter;
    private List<Section> mSectionList;

    boolean isScreenEmpty;
    boolean firstLoad;
    boolean isNeedShowCalendarInMenu = false;

    LoadingProgressDialog joinCourseProgressDialog;
    private DialogFragment unauthorizedDialog;

    @Inject
    @Named(AppConstants.SECTION_NAMED_INJECTION_COURSE_FINDER)
    CourseFinderPresenter courseFinderPresenter;

    @Inject
    CourseJoinerPresenter courseJoinerPresenter;

    @Inject
    ShareHelper shareHelper;

    @Inject
    IConfig mConfig;

    @Inject
    CalendarPresenter calendarPresenter;

    private GoogleApiClient mClient;
    private boolean wasIndexed;
    private Uri mUrlInApp;
    private Uri mUrlInWeb;
    private String mTitle;
    private String mDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainApplication.component().inject(this);
        setContentView(R.layout.activity_section);
        unbinder = ButterKnife.bind(this);
        hideSoftKeypad();
        isScreenEmpty = true;
        firstLoad = true;

        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon);

        mSectionsRecyclerView.setVisibility(View.GONE);
        mSectionsRecyclerView.setNestedScrollingEnabled(false);
        mSectionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSectionList = new ArrayList<>();
        mAdapter = new SectionAdapter(mSectionList, this, this);
        mSectionsRecyclerView.setAdapter(mAdapter);
        unauthorizedDialog = UnauthorizedDialogFragment.newInstance();
        joinCourseProgressDialog = new LoadingProgressDialog(this);
        ProgressHelper.activate(loadOnCenterProgressBar);
        bus.register(this);
        calendarPresenter.onStart(this);
        courseFinderPresenter.onStart(this);
        courseJoinerPresenter.onStart(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        onNewIntent(getIntent());
    }

    private void setUpToolbarWithCourse() {
        if (mCourse != null && mCourse.getTitle() != null && !mCourse.getTitle().isEmpty()) {
            setTitle(mCourse.getTitle());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);

        if (intent.getExtras() != null) {
            mCourse = (Course) (intent.getExtras().get(AppConstants.KEY_COURSE_BUNDLE));
        }
        if (mCourse != null) {
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
                                    analytic.reportError(Analytic.Error.NOTIFICATION_NOT_POSTED_ON_CLICK, e);
                                }
                            }
                        }
                        return null;
                    }
                };
                task.executeOnExecutor(mThreadPoolExecutor);
            }

            initScreenByCourse();
        } else {
            Uri fullUri = intent.getData();
            List<String> pathSegments = fullUri.getPathSegments();
            // 0 is "course", 1 is our slug
            if (pathSegments.size() > 1) {
                String pathFromWeb = pathSegments.get(1);
                Long id = HtmlHelper.parseIdFromSlug(pathFromWeb);
                long simpleId;
                if (id == null) {
                    simpleId = -1;
                } else {
                    simpleId = id;
                }
                if (simpleId < 0) {
                    onCourseUnavailable(new CourseUnavailableForUserEvent());
                } else {
                    courseFinderPresenter.findCourseById(simpleId);
                }
            } else {
                onCourseUnavailable(new CourseUnavailableForUserEvent());
            }
        }
    }

    public void initScreenByCourse() {
        reportConnectionProblem.setVisibility(View.GONE);
        courseNotParsedView.setVisibility(View.GONE);
        mAdapter.setCourse(mCourse);
        resolveJoinCourseView();
        setUpToolbarWithCourse();
        getAndShowSectionsFromCache();

        if (mCourse != null && mCourse.getSlug() != null && !wasIndexed) {
            mTitle = getString(R.string.syllabus_title) + ": " + mCourse.getTitle();
            mDescription = mCourse.getSummary();
            mUrlInWeb = Uri.parse(StringUtil.getUriForSyllabus(mConfig.getBaseUrl(), mCourse.getSlug()));
            mUrlInApp = StringUtil.getAppUriForCourseSyllabus(mConfig.getBaseUrl(), mCourse.getSlug());
            reportIndexToGoogle();
        }
    }

    private void reportIndexToGoogle() {
        if (mCourse != null && !wasIndexed && mCourse.getSlug() != null) {
            if (!mClient.isConnecting() && !mClient.isConnected()) {
                mClient.connect();
            }
            wasIndexed = true;
            AppIndex.AppIndexApi.start(mClient, getAction());
            analytic.reportEventWithIdName(Analytic.AppIndexing.COURSE_SYLLABUS, mCourse.getCourseId() + "", mCourse.getTitle());
        }
    }

    public void resolveJoinCourseView() {
        if (mCourse != null && mCourse.getEnrollment() <= 0) {
            joinCourseRoot.setVisibility(View.VISIBLE);
            joinCourseButton.setVisibility(View.VISIBLE);
            joinCourseButton.setEnabled(true);
            joinCourseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCourse != null) {
                        courseJoinerPresenter.joinCourse(mCourse);
                    }
                }
            });
            courseName.setText(mCourse.getTitle());
            courseIcon.setController(StepicLogicHelper.getControllerForCourse(mCourse, mConfig));
        } else {
            joinCourseRoot.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                if (mCourse != null) {
                    mShell.getScreenProvider().showCourseDescription(this, mCourse);
                }
                return true;

            case R.id.menu_item_share:
                if (mCourse != null) {
                    if (mCourse.getTitle() != null) {
                        analytic.reportEventWithIdName(Analytic.Interaction.SHARE_COURSE_SECTION, mCourse.getCourseId() + "", mCourse.getTitle());
                    }
                    Intent intent = shareHelper.getIntentForCourseSharing(mCourse);
                    startActivity(intent);
                }

                return true;

            case R.id.menu_item_calendar:
                calendarPresenter.addDeadlinesToCalendar(mSectionList);
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
            ProgressHelper.dismiss(loadOnCenterProgressBar);
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
        calendarPresenter.checkToShowCalendar(mSectionList);
        dismissReportView();
        mSectionsRecyclerView.setVisibility(View.VISIBLE);
        dismiss();
    }

    private void dismiss() {
        if (isScreenEmpty) {
            ProgressHelper.dismiss(loadOnCenterProgressBar);
            isScreenEmpty = false;
        } else {
            ProgressHelper.dismiss(mSwipeRefreshLayout);
        }
    }

    private void dismissReportView() {
        if (mSectionList != null && mSectionList.size() != 0) {
            reportConnectionProblem.setVisibility(View.GONE);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }

    @Override
    public void onRefresh() {
        analytic.reportEvent(Analytic.Interaction.REFRESH_SECTION);
        if (mCourse != null) {
            updateSections();
        } else {
            onNewIntent(getIntent());
        }
    }


    private void saveDataToCache(List<Section> sections) {
        ToDbSectionTask toDbSectionTask = new ToDbSectionTask(sections);
        toDbSectionTask.executeOnExecutor(mThreadPoolExecutor);
    }

    @Subscribe
    public void onFailureDownload(FailureResponseSectionEvent e) {
        if (mCourse != null && mCourse.getCourseId() == e.getCourse().getCourseId()) {
            if (mSectionList != null && mSectionList.size() == 0 && mCourse.getEnrollment() > 0) {
                reportConnectionProblem.setVisibility(View.VISIBLE);
            }
            if (mCourse.getEnrollment() <= 0) {
                Toast.makeText(this, getString(R.string.internet_problem), Toast.LENGTH_SHORT).show();
            }

            dismiss();
        }
    }

    @Subscribe
    public void onGettingFromDb(FinishingGetSectionFromDbEvent event) {
        if (mCourse == null || event.getCourse().getCourseId() != mCourse.getCourseId()) return;

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
    protected void onStart() {
        super.onStart();
        reportIndexToGoogle();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (wasIndexed) {
            AppIndex.AppIndexApi.end(mClient, getAction());
        }

        if (mClient != null && mClient.isConnected() && mClient.isConnecting()) {
            mClient.disconnect();
        }
        wasIndexed = false;
        ProgressHelper.dismiss(mSwipeRefreshLayout);
    }

    public Action getAction() {
        Thing object = new Thing.Builder()
                .setId(mUrlInWeb.toString())
                .setName(mTitle)
                .setDescription(mDescription)
                .setUrl(mUrlInApp)
                .build();

        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    protected void onDestroy() {
        calendarPresenter.onStop();
        courseJoinerPresenter.onStop();
        courseFinderPresenter.onDestroy();
        bus.unregister(this);
        courseNotParsedView.setOnClickListener(null);
        super.onDestroy();
    }

    @Subscribe
    public void onSuccessDownload(SuccessResponseSectionsEvent e) {
        if (mCourse != null && mCourse.getCourseId() == e.getCourse().getCourseId()) {
            SectionsStepicResponse stepicResponse = e.getResponse().body();
            List<Section> sections = stepicResponse.getSections();
            saveDataToCache(sections);
        }
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
        mAdapter.notifyItemChanged(position + SectionAdapter.SECTION_LIST_DELTA);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.section_unit_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_calendar);
        if (isNeedShowCalendarInMenu) {
            menuItem.setVisible(true);
        } else {
            menuItem.setVisible(false);
        }
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

    @Override
    public void onCourseFound(CourseFoundEvent event) {
        if (mCourse == null) {
            mCourse = event.getCourse();
            Bundle args = getIntent().getExtras();
            if (args == null) {
                args = new Bundle();
            }
            args.putSerializable(AppConstants.KEY_COURSE_BUNDLE, mCourse);
            getIntent().putExtras(args);
            initScreenByCourse();
        }
    }

    @Override
    public void onCourseUnavailable(CourseUnavailableForUserEvent event) {
        if (mCourse == null) {
            ProgressHelper.dismiss(mSwipeRefreshLayout);
            ProgressHelper.dismiss(loadOnCenterProgressBar);
            courseNotParsedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSharedPreferenceHelper.getAuthResponseFromStore() != null) {
                        mShell.getScreenProvider().showFindCourses(SectionActivity.this);
                        finish();
                    } else {
                        if (!unauthorizedDialog.isAdded()) {
                            unauthorizedDialog.show(getSupportFragmentManager(), null);
                        }
                    }
                }
            });
            courseNotParsedView.setVisibility(View.VISIBLE);
            reportConnectionProblem.setVisibility(View.GONE);
        }
    }

    @Override
    public void onInternetFailWhenCourseIsTriedToLoad(CourseCantLoadEvent event) {
        if (mCourse == null) {
            ProgressHelper.dismiss(mSwipeRefreshLayout);
            ProgressHelper.dismiss(loadOnCenterProgressBar);
            courseNotParsedView.setVisibility(View.GONE);
            reportConnectionProblem.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showProgress() {
        ProgressHelper.activate(joinCourseProgressDialog);
    }

    @Override
    public void setEnabledJoinButton(boolean isEnabled) {
        joinCourseButton.setEnabled(isEnabled);
    }

    @Override
    public void onFailJoin(FailJoinEvent e) {
        if (mCourse != null) {
            if (e.getCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                Toast.makeText(this, getString(R.string.join_course_web_exception), Toast.LENGTH_LONG).show();
            } else if (e.getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                //UNAUTHORIZED
                //it is just for safety, we should detect no account before send request
                if (!unauthorizedDialog.isAdded()) {
                    unauthorizedDialog.show(getSupportFragmentManager(), null);
                }
            } else {
                Toast.makeText(this, getString(R.string.join_course_exception),
                        Toast.LENGTH_SHORT).show();
            }
        }
        ProgressHelper.dismiss(joinCourseProgressDialog);
        setEnabledJoinButton(true);
    }

    @Override
    public void onSuccessJoin(SuccessJoinEvent e) {
        if (mCourse != null && e.getCourse() != null && e.getCourse().getCourseId() == mCourse.getCourseId() && mAdapter != null) {
            mCourse = e.getCourse();
            resolveJoinCourseView();
            mAdapter.notifyDataSetChanged();
        }
        ProgressHelper.dismiss(joinCourseProgressDialog);
    }


    @Subscribe
    public void onSuccessDrop(final SuccessDropCourseEvent e) {
        if (mCourse != null && e.getCourse().getCourseId() == mCourse.getCourseId()) {
            mCourse.setEnrollment(0);
            resolveJoinCourseView();
        }
    }

    @Override
    public void permissionNotGranted() {
        // FIXME: 20.07.16 implement
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_CALENDAR},
                1218);
    }

    @Override
    public void successExported() {
        mAdapter.setNeedShowCalendarWidget(false);
        mAdapter.notifyItemChanged(0);
        Toast.makeText(this, "SUCCESS", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShouldBeShownCalendar(boolean needShow) {
        mAdapter.setNeedShowCalendarWidget(needShow);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onShouldBeShownCalendarInMenu() {
        if (!isNeedShowCalendarInMenu) {
            isNeedShowCalendarInMenu = true;
            invalidateOptionsMenu();
        }
    }
}
