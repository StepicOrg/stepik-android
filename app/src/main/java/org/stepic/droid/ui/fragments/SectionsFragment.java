package org.stepic.droid.ui.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.otto.Subscribe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.core.ShareHelper;
import org.stepic.droid.core.modules.SectionModule;
import org.stepic.droid.core.presenters.CalendarPresenter;
import org.stepic.droid.core.presenters.CourseFinderPresenter;
import org.stepic.droid.core.presenters.CourseJoinerPresenter;
import org.stepic.droid.core.presenters.SectionsPresenter;
import org.stepic.droid.core.presenters.contracts.CalendarExportableView;
import org.stepic.droid.core.presenters.contracts.CourseJoinView;
import org.stepic.droid.core.presenters.contracts.LoadCourseView;
import org.stepic.droid.core.presenters.contracts.SectionsView;
import org.stepic.droid.events.CalendarChosenEvent;
import org.stepic.droid.events.courses.CourseCantLoadEvent;
import org.stepic.droid.events.courses.CourseFoundEvent;
import org.stepic.droid.events.courses.CourseUnavailableForUserEvent;
import org.stepic.droid.events.courses.SuccessDropCourseEvent;
import org.stepic.droid.events.joining_course.FailJoinEvent;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.events.sections.NotCachedSectionEvent;
import org.stepic.droid.events.sections.SectionCachedEvent;
import org.stepic.droid.model.CalendarItem;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Section;
import org.stepic.droid.notifications.INotificationManager;
import org.stepic.droid.notifications.model.Notification;
import org.stepic.droid.ui.adapters.SectionAdapter;
import org.stepic.droid.ui.dialogs.ChooseCalendarDialog;
import org.stepic.droid.ui.dialogs.ExplainCalendarPermissionDialog;
import org.stepic.droid.ui.dialogs.LoadingProgressDialog;
import org.stepic.droid.ui.dialogs.UnauthorizedDialogFragment;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ColorUtil;
import org.stepic.droid.util.HtmlHelper;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.SnackbarExtensionKt;
import org.stepic.droid.util.StepicLogicHelper;
import org.stepic.droid.util.StringUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SectionsFragment
        extends FragmentBase
        implements SwipeRefreshLayout.OnRefreshListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LoadCourseView, CourseJoinView,
        CalendarExportableView,
        SectionsView {

    public static SectionsFragment newInstance() {
        Bundle args = new Bundle();

        SectionsFragment fragment = new SectionsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @BindView(R.id.swipe_refresh_layout_sections)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.sections_recycler_view)
    RecyclerView sectionsRecyclerView;

    @BindView(R.id.load_progressbar)
    ProgressBar loadOnCenterProgressBar;

    @BindView(R.id.toolbar)
    android.support.v7.widget.Toolbar mToolbar;

    @BindView(R.id.report_problem)
    protected View reportConnectionProblem;

    @BindView(R.id.course_not_found)
    View courseNotParsedView;

    @BindView(R.id.report_empty)
    protected View reportEmptyView;

    @BindView(R.id.join_course_root)
    protected View joinCourseRoot; // default state is gone

    @BindView(R.id.join_course_layout)
    protected View joinCourseButton;

    @BindView(R.id.courseIcon)
    protected ImageView courseIcon;

    GlideDrawableImageViewTarget imageViewTarget;

    @BindView(R.id.course_name)
    protected TextView courseName;

    @BindView(R.id.root_section_view)
    protected View rootView;

    @Nullable
    private Course course;
    private SectionAdapter adapter;
    private List<Section> sectionList;

    boolean firstLoad;
    boolean isNeedShowCalendarInMenu = false;

    LoadingProgressDialog joinCourseProgressDialog;
    private DialogFragment unauthorizedDialog;

    @Inject
    CourseFinderPresenter courseFinderPresenter;

    @Inject
    CourseJoinerPresenter courseJoinerPresenter;

    @Inject
    ShareHelper shareHelper;

    @Inject
    IConfig mConfig;

    @Inject
    CalendarPresenter calendarPresenter;

    @Inject
    SectionsPresenter sectionsPresenter;

    @Inject
    INotificationManager notificationManager;

    private GoogleApiClient googleClient;
    private boolean wasIndexed;
    private Uri urlInApp;
    private Uri urlInWeb;
    private String title;
    private String description;

    LinearLayoutManager linearLayoutManager;

    private int afterUpdateModulePosition = -1;
    private int modulePosition;

    @Override
    protected void injectComponent() {
        MainApplication.component().plus(new SectionModule()).inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @android.support.annotation.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @android.support.annotation.Nullable ViewGroup container, @android.support.annotation.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sections, container, false);
    }

    @Override
    public void onViewCreated(View view, @android.support.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        unbinder = ButterKnife.bind(this, view);
        imageViewTarget = new GlideDrawableImageViewTarget(courseIcon);
        hideSoftKeypad();
        firstLoad = true;

        googleClient = new GoogleApiClient.Builder(getActivity()).addApi(AppIndex.API).build();

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon);

        sectionsRecyclerView.setVisibility(View.GONE);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        sectionsRecyclerView.setLayoutManager(linearLayoutManager);
        sectionList = new ArrayList<>();
        adapter = new SectionAdapter(sectionList, getContext(), ((AppCompatActivity) getActivity()), calendarPresenter);
        sectionsRecyclerView.setAdapter(adapter);

        sectionsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        unauthorizedDialog = UnauthorizedDialogFragment.newInstance();
        joinCourseProgressDialog = new LoadingProgressDialog(getContext());
        ProgressHelper.activate(loadOnCenterProgressBar);
        bus.register(this);
        calendarPresenter.attachView(this);
        courseFinderPresenter.attachView(this);
        courseJoinerPresenter.attachView(this);
        sectionsPresenter.attachView(this);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        onNewIntent(((AppCompatActivity) getActivity()).getIntent());
    }

    private void setUpToolbarWithCourse() {
        if (course != null && course.getTitle() != null && !course.getTitle().isEmpty()) {
            getActivity().setTitle(course.getTitle());
        }
    }

    public void initScreenByCourse() {
        reportConnectionProblem.setVisibility(View.GONE);
        courseNotParsedView.setVisibility(View.GONE);
        adapter.setCourse(course);
        resolveJoinCourseView();
        setUpToolbarWithCourse();
        sectionsPresenter.showSections(course, false);

        if (course != null && course.getSlug() != null && !wasIndexed) {
            title = getString(R.string.syllabus_title) + ": " + course.getTitle();
            description = course.getSummary();
            urlInWeb = Uri.parse(StringUtil.getUriForSyllabus(mConfig.getBaseUrl(), course.getSlug()));
            urlInApp = StringUtil.getAppUriForCourseSyllabus(mConfig.getBaseUrl(), course.getSlug());
            reportIndexToGoogle();
        }
    }

    private void reportIndexToGoogle() {
        if (course != null && !wasIndexed && course.getSlug() != null) {
            if (!googleClient.isConnecting() && !googleClient.isConnected()) {
                googleClient.connect();
            }
            wasIndexed = true;
            AppIndex.AppIndexApi.start(googleClient, getAction());
            analytic.reportEventWithIdName(Analytic.AppIndexing.COURSE_SYLLABUS, course.getCourseId() + "", course.getTitle());
        }
    }

    public void resolveJoinCourseView() {
        if (course != null && course.getEnrollment() <= 0) {
            joinCourseRoot.setVisibility(View.VISIBLE);
            joinCourseButton.setVisibility(View.VISIBLE);
            joinCourseButton.setEnabled(true);
            joinCourseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (course != null) {
                        courseJoinerPresenter.joinCourse(course);
                    }
                }
            });
            courseName.setText(course.getTitle());
            Glide.with(this)
                    .load(StepicLogicHelper.getPathForCourseOrEmpty(course, mConfig))
                    .placeholder(R.drawable.ic_course_placeholder)
                    .into(imageViewTarget);
        } else {
            joinCourseRoot.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                if (course != null) {
                    shell.getScreenProvider().showCourseDescription(this, course);
                }
                return true;

            case R.id.menu_item_share:
                if (course != null) {
                    if (course.getTitle() != null) {
                        analytic.reportEventWithIdName(Analytic.Interaction.SHARE_COURSE_SECTION, course.getCourseId() + "", course.getTitle());
                    }
                    Intent intent = shareHelper.getIntentForCourseSharing(course);
                    startActivity(intent);
                }

                return true;

            case R.id.menu_item_calendar:
                analytic.reportEventWithIdName(Analytic.Calendar.USER_CLICK_ADD_MENU, course.getCourseId() + "", course.getTitle());
                calendarPresenter.addDeadlinesToCalendar(sectionList, null);
                return true;
            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onEmptySections() {
        if (sectionList.isEmpty()) {
            dismissLoadState();
            reportEmptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onConnectionProblem() {
        if (sectionList.isEmpty()) {
            dismissLoadState();
            reportConnectionProblem.setVisibility(View.VISIBLE);
        }
    }

    public void onNeedShowSections(List<Section> sections) {
        boolean wasEmpty = sectionList.isEmpty();
        sectionList.clear();
        sectionList.addAll(sections);
        dismissReportView();
        sectionsRecyclerView.setVisibility(View.VISIBLE);
        dismissLoadState();

        calendarPresenter.checkToShowCalendar(sectionList);
        if (wasEmpty) {

            if (modulePosition > 0 && modulePosition <= sections.size()) {
                Section section = sections.get(modulePosition - 1);

                boolean userHasAccess = (section.is_active() || (section.getActions() != null && section.getActions().getTest_section() != null)) && course != null && course.getEnrollment() > 0;
                if (userHasAccess) {
                    shell.getScreenProvider().showUnitsForSection(getContext(), sections.get(modulePosition - 1));
                } else {
                    adapter.setDefaultHighlightPosition(modulePosition - 1);
                    int scrollTo = modulePosition + SectionAdapter.SECTION_LIST_DELTA - 1;
                    linearLayoutManager.scrollToPositionWithOffset(scrollTo, 0);
                    afterUpdateModulePosition = modulePosition;
                }
                modulePosition = -1;
            }
        } else {
            adapter.notifyDataSetChanged();
            adapter.setDefaultHighlightPosition(afterUpdateModulePosition - 1);
            int scrollTo = afterUpdateModulePosition + SectionAdapter.SECTION_LIST_DELTA - 1;
            linearLayoutManager.scrollToPositionWithOffset(scrollTo, 0);
            afterUpdateModulePosition = -1;
        }
    }

    @Override
    public void onLoading() {
        reportEmptyView.setVisibility(View.GONE);
        reportConnectionProblem.setVisibility(View.GONE);
        if (sectionList.isEmpty()) {
            ProgressHelper.activate(loadOnCenterProgressBar);
        }
    }

    private void dismissLoadState() {
        ProgressHelper.dismiss(loadOnCenterProgressBar);
        ProgressHelper.dismiss(swipeRefreshLayout);
    }

    private void dismissReportView() {
        if (sectionList != null && sectionList.size() != 0) {
            reportConnectionProblem.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
        analytic.reportEvent(Analytic.Interaction.REFRESH_SECTION);
        if (course != null) {
            sectionsPresenter.showSections(course, true);
        } else {
            onNewIntent(getActivity().getIntent());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        reportIndexToGoogle();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (wasIndexed) {
            AppIndex.AppIndexApi.end(googleClient, getAction());
        }

        if (googleClient != null && googleClient.isConnected() && googleClient.isConnecting()) {
            googleClient.disconnect();
        }
        wasIndexed = false;
        ProgressHelper.dismiss(swipeRefreshLayout);
    }

    public Action getAction() {
        Thing object = new Thing.Builder()
                .setId(urlInWeb.toString())
                .setName(title)
                .setDescription(description)
                .setUrl(urlInApp)
                .build();

        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onDestroyView() {
        calendarPresenter.detachView(this);
        courseJoinerPresenter.detachView(this);
        courseFinderPresenter.detachView(this);
        sectionsPresenter.detachView(this);
        bus.unregister(this);
        courseNotParsedView.setOnClickListener(null);
        super.onDestroyView();
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
        for (int i = 0; i < sectionList.size(); i++) {
            if (sectionList.get(i).getId() == sectionId) {
                position = i;
                section = sectionList.get(i);
                break;
            }
        }
        if (section == null || position == -1 || position >= sectionList.size()) return;

        //now we have not null section and correct position at oldList
        section.set_cached(isCached);
        section.set_loading(isLoading);
        adapter.notifyItemChanged(position + SectionAdapter.SECTION_LIST_DELTA);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.section_unit_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_calendar);
        if (isNeedShowCalendarInMenu) {
            menuItem.setVisible(true);
        } else {
            menuItem.setVisible(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AppConstants.REQUEST_EXTERNAL_STORAGE) {
            String permissionExternalStorage = permissions[0];
            if (permissionExternalStorage == null) return;

            if (permissionExternalStorage.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                int position = shell.getSharedPreferenceHelper().getTempPosition();
                if (adapter != null) {
                    adapter.requestClickLoad(position);
                }
            }
        }

        if (requestCode == AppConstants.REQUEST_CALENDAR_PERMISSION) {
            String permissionExternalStorage = permissions[0];
            if (permissionExternalStorage == null) return;

            if (permissionExternalStorage.equals(Manifest.permission.WRITE_CALENDAR) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                calendarPresenter.addDeadlinesToCalendar(sectionList, null);
            }
        }
    }

    @Override
    public void onCourseFound(CourseFoundEvent event) {
        if (course == null) {
            course = event.getCourse();
            Bundle args = getActivity().getIntent().getExtras();
            if (args == null) {
                args = new Bundle();
            }
            args.putSerializable(AppConstants.KEY_COURSE_BUNDLE, course);
            getActivity().getIntent().putExtras(args);
            initScreenByCourse();
        }
    }

    @Override
    public void onCourseUnavailable(CourseUnavailableForUserEvent event) {
        if (course == null) {
            ProgressHelper.dismiss(swipeRefreshLayout);
            ProgressHelper.dismiss(loadOnCenterProgressBar);
            courseNotParsedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sharedPreferenceHelper.getAuthResponseFromStore() != null) {
                        shell.getScreenProvider().showFindCourses(getActivity());
                        getActivity().finish();
                    } else {
                        if (!unauthorizedDialog.isAdded()) {
                            unauthorizedDialog.show(getFragmentManager(), null);
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
        if (course == null) {
            ProgressHelper.dismiss(swipeRefreshLayout);
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
        if (course != null) {
            if (e.getCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                Toast.makeText(getContext(), getString(R.string.join_course_web_exception), Toast.LENGTH_LONG).show();
            } else if (e.getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                //UNAUTHORIZED
                //it is just for safety, we should detect no account before send request
                if (!unauthorizedDialog.isAdded()) {
                    unauthorizedDialog.show(getFragmentManager(), null);
                }
            } else {
                Toast.makeText(getContext(), getString(R.string.join_course_exception),
                        Toast.LENGTH_SHORT).show();
            }
        }
        ProgressHelper.dismiss(joinCourseProgressDialog);
        setEnabledJoinButton(true);
    }

    @Override
    public void onSuccessJoin(SuccessJoinEvent e) {
        if (course != null && e.getCourse() != null && e.getCourse().getCourseId() == course.getCourseId() && adapter != null) {
            course = e.getCourse();
            resolveJoinCourseView();
            adapter.notifyDataSetChanged();
        }
        ProgressHelper.dismiss(joinCourseProgressDialog);
    }


    @Subscribe
    public void onSuccessDrop(final SuccessDropCourseEvent e) {
        if (course != null && e.getCourse().getCourseId() == course.getCourseId()) {
            course.setEnrollment(0);
            resolveJoinCourseView();
        }
    }

    @Override
    public void permissionNotGranted() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.WRITE_CALENDAR)) {

            DialogFragment dialog = ExplainCalendarPermissionDialog.newInstance();
            if (!dialog.isAdded()) {
                dialog.show(this.getFragmentManager(), null);
            }

        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_CALENDAR},
                    AppConstants.REQUEST_CALENDAR_PERMISSION);
        }
    }

    @Override
    public void successExported() {
        adapter.setNeedShowCalendarWidget(false);
        adapter.notifyItemChanged(0);
        SnackbarExtensionKt.setTextColor(Snackbar.make(rootView, R.string.calendar_added_message, Snackbar.LENGTH_SHORT), ColorUtil.INSTANCE.getColorArgb(R.color.white, getContext())).show();
    }

    @Override
    public void onShouldBeShownCalendar(boolean needShow) {
        adapter.setNeedShowCalendarWidget(needShow);
        adapter.notifyItemChanged(0);
    }

    @Override
    public void onShouldBeShownCalendarInMenu() {
        if (!isNeedShowCalendarInMenu) {
            isNeedShowCalendarInMenu = true;
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void onNeedToChooseCalendar(@NotNull ArrayList<CalendarItem> primariesCalendars) {
        DialogFragment chooseCalendarDialog = ChooseCalendarDialog.Companion.newInstance(primariesCalendars);
        if (!chooseCalendarDialog.isAdded()) {
            chooseCalendarDialog.show(getFragmentManager(), null);
        }
    }

    @Subscribe
    public void onCalendarChosen(CalendarChosenEvent event) {
        CalendarItem calendarItem = event.getCalendarItem();
        calendarPresenter.addDeadlinesToCalendar(sectionList, calendarItem);
    }

    @Override
    public void onUserDoesntHaveCalendar() {
        userPreferences.setNeedToShowCalendarWidget(false);
        adapter.setNeedShowCalendarWidget(false);
        adapter.notifyItemChanged(0);
        SnackbarExtensionKt.setTextColor(Snackbar.make(rootView, R.string.user_not_have_calendar, Snackbar.LENGTH_LONG), ColorUtil.INSTANCE.getColorArgb(R.color.white, getContext())).show();
    }


    public void onNewIntent(Intent intent) {
        if (intent.getExtras() != null) {
            course = (Course) (intent.getExtras().get(AppConstants.KEY_COURSE_BUNDLE));
        }
        if (course != null) {
            if (intent.getAction() != null && intent.getAction().equals(AppConstants.OPEN_NOTIFICATION)) {
                final long courseId = course.getCourseId();
                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        List<Notification> notifications = databaseFacade.getAllNotificationsOfCourse(courseId);
                        notificationManager.discardAllNotifications(courseId);
                        for (Notification notificationItem : notifications) {
                            if (notificationItem != null && notificationItem.getId() != null) {
                                try {
                                    shell.getApi().markNotificationAsRead(notificationItem.getId(), true).execute();
                                } catch (IOException e) {
                                    analytic.reportError(Analytic.Error.NOTIFICATION_NOT_POSTED_ON_CLICK, e);
                                }
                            }
                        }
                        return null;
                    }
                };
                task.executeOnExecutor(threadPoolExecutor);
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


                try {
                    String rawSectionPosition = fullUri.getQueryParameter("module");
                    modulePosition = Integer.parseInt(rawSectionPosition);
                } catch (Exception ex) {
                    modulePosition = -1;
                }

                analytic.reportEvent(Analytic.DeepLink.USER_OPEN_SYLLABUS_LINK, simpleId + "");
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

}
