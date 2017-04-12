package org.stepic.droid.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.appindexing.builders.Indexables;
import com.squareup.otto.Subscribe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.core.ShareHelper;
import org.stepic.droid.core.presenters.CalendarPresenter;
import org.stepic.droid.core.presenters.CourseFinderPresenter;
import org.stepic.droid.core.presenters.CourseJoinerPresenter;
import org.stepic.droid.core.presenters.DownloadingInteractionPresenter;
import org.stepic.droid.core.presenters.DownloadingProgressSectionsPresenter;
import org.stepic.droid.core.presenters.InvitationPresenter;
import org.stepic.droid.core.presenters.SectionsPresenter;
import org.stepic.droid.core.presenters.contracts.CalendarExportableView;
import org.stepic.droid.core.presenters.contracts.CourseJoinView;
import org.stepic.droid.core.presenters.contracts.DownloadingInteractionView;
import org.stepic.droid.core.presenters.contracts.DownloadingProgressSectionsView;
import org.stepic.droid.core.presenters.contracts.InvitationView;
import org.stepic.droid.core.presenters.contracts.LoadCourseView;
import org.stepic.droid.core.presenters.contracts.SectionsView;
import org.stepic.droid.events.CalendarChosenEvent;
import org.stepic.droid.events.UpdateSectionProgressEvent;
import org.stepic.droid.events.courses.CourseCantLoadEvent;
import org.stepic.droid.events.courses.CourseFoundEvent;
import org.stepic.droid.events.courses.CourseUnavailableForUserEvent;
import org.stepic.droid.events.courses.SuccessDropCourseEvent;
import org.stepic.droid.events.joining_course.FailJoinEvent;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.events.sections.NotCachedSectionEvent;
import org.stepic.droid.events.sections.SectionCachedEvent;
import org.stepic.droid.fonts.FontType;
import org.stepic.droid.model.CalendarItem;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.SectionLoadingState;
import org.stepic.droid.notifications.StepikNotificationManager;
import org.stepic.droid.notifications.model.Notification;
import org.stepic.droid.ui.adapters.SectionAdapter;
import org.stepic.droid.ui.dialogs.ChooseCalendarDialog;
import org.stepic.droid.ui.dialogs.DeleteItemDialogFragment;
import org.stepic.droid.ui.dialogs.ExplainCalendarPermissionDialog;
import org.stepic.droid.ui.dialogs.LoadingProgressDialog;
import org.stepic.droid.ui.dialogs.UnauthorizedDialogFragment;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ColorUtil;
import org.stepic.droid.util.HtmlHelper;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.SectionUtilKt;
import org.stepic.droid.util.SnackbarExtensionKt;
import org.stepic.droid.util.StepikLogicHelper;
import org.stepic.droid.util.StringUtil;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

public class SectionsFragment
        extends FragmentBase
        implements SwipeRefreshLayout.OnRefreshListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LoadCourseView, CourseJoinView,
        CalendarExportableView,
        SectionsView,
        InvitationView,
        DownloadingProgressSectionsView,
        DownloadingInteractionView {

    public static String joinFlag = "joinFlag";
    private static int INVITE_REQUEST_CODE = 324;
    private static final int ANIMATION_DURATION = 0;
    public static final int DELETE_POSITION_REQUEST_CODE = 177;

    public static SectionsFragment newInstance() {
        return new SectionsFragment();
    }


    @BindView(R.id.swipe_refresh_layout_sections)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.sections_recycler_view)
    RecyclerView sectionsRecyclerView;

    @BindView(R.id.load_progressbar)
    ProgressBar loadOnCenterProgressBar;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

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
    CalendarPresenter calendarPresenter;

    @Inject
    SectionsPresenter sectionsPresenter;

    @Inject
    StepikNotificationManager stepikNotificationManager;

    @Inject
    InvitationPresenter invitationPresenter;

    @Inject
    DownloadingProgressSectionsPresenter downloadingProgressSectionsPresenter;

    @Inject
    DownloadingInteractionPresenter downloadingInteractionPresenter;

    private boolean wasIndexed;
    private Uri urlInWeb;
    private String title;
    private Map<Long, SectionLoadingState> sectionIdToLoadingStateMap = new HashMap<>();

    LinearLayoutManager linearLayoutManager;

    private int afterUpdateModulePosition = -1;
    private int modulePosition;
    private boolean isAfterJoining;

    @Override
    protected void injectComponent() {
        App
                .component()
                .courseComponentBuilder()
                .build()
                .inject(this);
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
        imageViewTarget = new GlideDrawableImageViewTarget(courseIcon);
        hideSoftKeypad();
        firstLoad = true;

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon);

        sectionsRecyclerView.setVisibility(View.GONE);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        sectionsRecyclerView.setLayoutManager(linearLayoutManager);
        sectionList = new ArrayList<>();
        adapter = new SectionAdapter(sectionList, ((AppCompatActivity) getActivity()), calendarPresenter, sectionsPresenter.getProgressMap(), sectionIdToLoadingStateMap, this, downloadingInteractionPresenter);
        sectionsRecyclerView.setAdapter(adapter);

        sectionsRecyclerView.setItemAnimator(new SlideInRightAnimator());
        sectionsRecyclerView.getItemAnimator().setRemoveDuration(ANIMATION_DURATION);
        sectionsRecyclerView.getItemAnimator().setAddDuration(ANIMATION_DURATION);
        sectionsRecyclerView.getItemAnimator().setMoveDuration(ANIMATION_DURATION);
        sectionsRecyclerView.getItemAnimator().setChangeDuration(0);


        joinCourseProgressDialog = new LoadingProgressDialog(getContext());
        ProgressHelper.activate(loadOnCenterProgressBar);
        bus.register(this);
        calendarPresenter.attachView(this);
        courseFinderPresenter.attachView(this);
        courseJoinerPresenter.attachView(this);
        sectionsPresenter.attachView(this);
        invitationPresenter.attachView(this);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
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
            urlInWeb = Uri.parse(StringUtil.getUriForSyllabus(config.getBaseUrl(), course.getSlug()));
            reportIndexToGoogle();
        }

        if (isAfterJoining && course != null) {
            isAfterJoining = false;
            showShareCourseWithFriendDialog(course);
        }

    }

    private void reportIndexToGoogle() {
        if (course != null && !wasIndexed && course.getSlug() != null) {
            wasIndexed = true;
            FirebaseAppIndex.getInstance().update(getIndexable());
            FirebaseUserActions.getInstance().start(getAction());
            analytic.reportEventWithIdName(Analytic.AppIndexing.COURSE_SYLLABUS, course.getCourseId() + "", course.getTitle());
        }
    }

    private Indexable getIndexable() {
        return Indexables.newSimple(title, urlInWeb.toString());
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
                        analytic.reportEvent(Analytic.Interaction.JOIN_COURSE);
                        courseJoinerPresenter.joinCourse(course);
                    }
                }
            });
            courseName.setText(course.getTitle());
            Glide.with(this)
                    .load(StepikLogicHelper.getPathForCourseOrEmpty(course, config))
                    .placeholder(R.drawable.general_placeholder)
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
                    screenManager.showCourseDescription(this, course);
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

    @Override
    public void onNeedShowSections(@NotNull List<Section> sections) {
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

                boolean userHasAccess = SectionUtilKt.hasUserAccess(section, course);
                if (userHasAccess) {
                    screenManager.showUnitsForSection(SectionsFragment.this.getActivity(), sections.get(modulePosition - 1));
                } else {
                    adapter.setDefaultHighlightPosition(modulePosition - 1);
                    int scrollTo = modulePosition + SectionAdapter.PRE_SECTION_LIST_DELTA - 1;
                    linearLayoutManager.scrollToPositionWithOffset(scrollTo, 0);
                    afterUpdateModulePosition = modulePosition;
                }
                modulePosition = -1;
            }
        } else {
            adapter.notifyDataSetChanged();
            adapter.setDefaultHighlightPosition(afterUpdateModulePosition - 1);
            int scrollTo = afterUpdateModulePosition + SectionAdapter.PRE_SECTION_LIST_DELTA - 1;
            linearLayoutManager.scrollToPositionWithOffset(scrollTo, 0);
            afterUpdateModulePosition = -1;
        }

        downloadingProgressSectionsPresenter.subscribeToProgressUpdates(sectionList);
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
        downloadingInteractionPresenter.attachView(this);
        downloadingProgressSectionsPresenter.attachView(this);
        downloadingProgressSectionsPresenter.subscribeToProgressUpdates(sectionList);
    }

    @Override
    public void onStop() {
        downloadingInteractionPresenter.detachView(this);
        downloadingProgressSectionsPresenter.detachView(this);
        super.onStop();
        if (wasIndexed) {
            FirebaseUserActions.getInstance().end(getAction());
        }
        wasIndexed = false;
        ProgressHelper.dismiss(swipeRefreshLayout);
    }

    public Action getAction() {
        return Actions.newView(title, urlInWeb.toString());
    }

    @Override
    public void onDestroyView() {
        calendarPresenter.detachView(this);
        courseJoinerPresenter.detachView(this);
        courseFinderPresenter.detachView(this);
        sectionsPresenter.detachView(this);
        invitationPresenter.detachView(this);
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

    @Subscribe
    public void onSectionProgressChanged(UpdateSectionProgressEvent event) {
        if (course != null && course.getCourseId() == event.getCourseId()) {
            sectionsPresenter.updateSectionProgress(event.getProgress());
        }
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
        adapter.notifyItemChanged(position + SectionAdapter.PRE_SECTION_LIST_DELTA);
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

        if (requestCode == AppConstants.REQUEST_EXTERNAL_STORAGE && permissions.length > 0) {
            String permissionExternalStorage = permissions[0];
            if (permissionExternalStorage == null) return;

            if (permissionExternalStorage.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                int position = sharedPreferenceHelper.getTempPosition();
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
                        screenManager.showFindCourses(getActivity());
                        getActivity().finish();
                    } else {
                        unauthorizedDialog = UnauthorizedDialogFragment.newInstance(course);
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
                unauthorizedDialog = UnauthorizedDialogFragment.newInstance(course);
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
        if (course != null) {
            showShareCourseWithFriendDialog(course);
        }
    }

    public void showShareCourseWithFriendDialog(@NotNull final Course courseForSharing) {
        isAfterJoining = false;
        invitationPresenter.needShowInvitationDialog(courseForSharing);
    }

    private void showMessageAboutSharing() {
        SnackbarExtensionKt
                .setTextColor(
                        Snackbar.make(rootView, R.string.share_course_in_menu, Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.ok, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Timber.d("set empty click listener for appearing of Action text");
                                    }
                                })
                                .setActionTextColor(ColorUtil.INSTANCE.getColorArgb(R.color.snack_action_color, getContext())),
                        ColorUtil.INSTANCE.getColorArgb(R.color.white,
                                getContext()))
                .show();
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

        long simpleCourseId = -1;
        int simpleModulePosition = -1;

        if (intent.getExtras() != null) {
            isAfterJoining = intent.getExtras().getBoolean(joinFlag);
            intent.putExtra(joinFlag, false);

            Object courseInBundle = intent.getExtras().get(AppConstants.KEY_COURSE_BUNDLE);
            if (courseInBundle != null && courseInBundle instanceof Course) {
                course = (Course) courseInBundle;
            } else {
                try {
                    simpleCourseId = intent.getExtras().getLong(AppConstants.KEY_COURSE_LONG_ID);
                    simpleModulePosition = intent.getExtras().getInt(AppConstants.KEY_MODULE_POSITION);
                } catch (Exception ex) {
                    //cant parse -> continue
                }
            }
        }
        if (course != null) {
            final long courseId = course.getCourseId();
            postNotificationAsReadIfNeed(intent, courseId);
            initScreenByCourse();
        } else if (simpleCourseId > 0 && simpleModulePosition > 0) {
            modulePosition = simpleModulePosition;
            courseFinderPresenter.findCourseById(simpleCourseId);
            postNotificationAsReadIfNeed(intent, simpleCourseId);
        } else {
            Uri fullUri = intent.getData();
            List<String> pathSegments = fullUri.getPathSegments();
            // 0 is "course", 1 is our slug
            if (pathSegments.size() > 1) {
                String pathFromWeb = pathSegments.get(1);
                Long id = HtmlHelper.parseIdFromSlug(pathFromWeb);
                if (id == null) {
                    simpleCourseId = -1;
                } else {
                    simpleCourseId = id;
                }

                try {
                    String rawSectionPosition = fullUri.getQueryParameter("module");
                    modulePosition = Integer.parseInt(rawSectionPosition);
                } catch (Exception ex) {
                    modulePosition = -1;
                }

                String action = intent.getAction();
                if (action != null) {
                    if (action.equals(AppConstants.OPEN_NOTIFICATION)) {
                        analytic.reportEvent(Analytic.Notification.OPEN_NOTIFICATION);
                    } else if (!action.equals(AppConstants.INTERNAL_STEPIK_ACTION)) {
                        analytic.reportEvent(Analytic.DeepLink.USER_OPEN_SYLLABUS_LINK, simpleCourseId + "");
                        analytic.reportEvent(Analytic.DeepLink.USER_OPEN_LINK_GENERAL);
                    }
                }

                if (simpleCourseId < 0) {
                    onCourseUnavailable(new CourseUnavailableForUserEvent());
                } else {
                    courseFinderPresenter.findCourseById(simpleCourseId);
                    postNotificationAsReadIfNeed(intent, simpleCourseId);
                }
            } else {
                onCourseUnavailable(new CourseUnavailableForUserEvent());
            }
        }

    }

    private void postNotificationAsReadIfNeed(Intent intent, final long courseId) {
        if (intent.getAction() != null && intent.getAction().equals(AppConstants.OPEN_NOTIFICATION_FOR_CHECK_COURSE)) {
            analytic.reportEvent(Analytic.Notification.OPEN_NOTIFICATION);
            analytic.reportEvent(Analytic.Notification.OPEN_NOTIFICATION_SYLLABUS, courseId + "");
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    List<Notification> notifications = databaseFacade.getAllNotificationsOfCourse(courseId);
                    stepikNotificationManager.discardAllShownNotificationsRelatedToCourse(courseId);
                    for (Notification notificationItem : notifications) {
                        if (notificationItem != null && notificationItem.getId() != null) {
                            try {
                                api.setReadStatusForNotification(notificationItem.getId(), true).execute();
                            } catch (Exception e) {
                                analytic.reportError(Analytic.Error.NOTIFICATION_NOT_POSTED_ON_CLICK, e);
                            }
                        }
                    }
                    return null;
                }
            };
            task.executeOnExecutor(threadPoolExecutor);
        }
    }

    @Override
    public void hideCalendarAfterNotNow() {
        adapter.setNeedShowCalendarWidget(false);
        adapter.notifyItemChanged(0);
        SnackbarExtensionKt.setTextColor(Snackbar.make(rootView, R.string.after_hide_calendar_message, Snackbar.LENGTH_LONG), ColorUtil.INSTANCE.getColorArgb(R.color.white, getContext())).show();
    }

    @Override
    public void updatePosition(int position) {
        if (position >= 0 && sectionList.size() > position && adapter != null) {
            try {
                adapter.notifyItemChanged(position + SectionAdapter.PRE_SECTION_LIST_DELTA);
            } catch (Exception exception) {
                Timber.d(exception);
            }
        }
    }

    @Override
    public void onShowInvitationDialog(@NotNull final Course courseForSharing) {
        SpannableString inviteTitle = new SpannableString(getString(R.string.take_course_with_fiends));
        inviteTitle.setSpan(new ForegroundColorSpan(Color.BLACK), 0, inviteTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(TypefaceUtils.load(getContext().getAssets(), fontsProvider.provideFontPath(FontType.bold)));
        inviteTitle.setSpan(typefaceSpan, 0, inviteTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        MaterialStyledDialog dialog = new MaterialStyledDialog.Builder(getContext())
                .setTitle(inviteTitle)
                .setDescription(R.string.invite_friends_description)
                .setHeaderDrawable(R.drawable.dialog_background)
                .setPositiveText(R.string.invite)
                .setNegativeText(R.string.dont_want)
                .setScrollable(true, 10) // number of lines
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        analytic.reportEvent(Analytic.Interaction.POSITIVE_MATERIAL_DIALOG_INVITATION);
                        Intent intent = shareHelper.getIntentForCourseSharing(courseForSharing);
                        SectionsFragment.this.startActivityForResult(intent, INVITE_REQUEST_CODE);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        invitationPresenter.onClickDecline();
                        showMessageAboutSharing();
                    }
                })
                .build();
        dialog.show();
    }

    @Override
    public void onNewProgressValue(@NotNull SectionLoadingState state) {
        // FIXME: 21.02.17
        int position = -1;
        for (int i = 0; i < sectionList.size(); i++) {
            Section section = sectionList.get(i);
            if (section.getId() == state.getSectionId()) {
                position = i;
            }
        }

        if (position < 0) {
            return;
        }

        position += SectionAdapter.PRE_SECTION_LIST_DELTA;

        //change state for updating in adapter
        sectionIdToLoadingStateMap.put(state.getSectionId(), state);
        adapter.notifyItemChanged(position);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (adapter != null && requestCode == DELETE_POSITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            analytic.reportEvent(Analytic.Interaction.ACCEPT_DELETING_SECTION);
            int position = data.getIntExtra(DeleteItemDialogFragment.deletePositionKey, -1);
            adapter.requestClickDeleteSilence(position);
        }
    }

    @Override
    public void onLoadingAccepted(int position) {
        adapter.loadAfterDetermineNetworkState(position);
    }

    @Override
    public void onShowPreferenceSuggestion() {
        SnackbarExtensionKt
                .setTextColor(
                        Snackbar.make(rootView, R.string.allow_mobile_snack, SnackbarExtensionKt.getDuration5Sec())
                                .setAction(R.string.settings_title, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        try {
                                            screenManager.showSettings(getActivity());
                                        } catch (NullPointerException nullPointerException) {
                                            Timber.e(nullPointerException);
                                        }
                                    }
                                })
                                .setActionTextColor(ColorUtil.INSTANCE.getColorArgb(R.color.snack_action_color, getContext())),
                        ColorUtil.INSTANCE.getColorArgb(R.color.white,
                                getContext()))
                .show();
    }

    @Override
    public void onShowInternetIsNotAvailableRetry(final int position) {
        SnackbarExtensionKt
                .setTextColor(
                        Snackbar.make(rootView, R.string.internet_problem_short, SnackbarExtensionKt.getDuration5Sec())
                                .setAction(R.string.retry_internet, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (adapter != null) {
                                            adapter.requestClickLoad(position);
                                        }
                                    }
                                })
                                .setActionTextColor(ColorUtil.INSTANCE.getColorArgb(R.color.snack_action_color, getContext())),
                        ColorUtil.INSTANCE.getColorArgb(R.color.white,
                                getContext()))
                .show();
    }
}
