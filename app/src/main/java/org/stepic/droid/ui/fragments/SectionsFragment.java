package org.stepic.droid.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.analytic.AmplitudeAnalytic;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.base.FragmentBase;
import org.stepik.android.model.Course;
import org.stepic.droid.notifications.StepikNotificationManager;
import org.stepic.droid.ui.custom.StepikSwipeRefreshLayout;
import org.stepic.droid.ui.util.ToolbarHelperKt;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.HtmlHelper;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.StepikLogicHelper;
import org.stepic.droid.util.StringUtil;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import kotlin.Pair;
import kotlin.collections.MapsKt;

public class SectionsFragment
        extends FragmentBase
        implements SwipeRefreshLayout.OnRefreshListener {

    public static final String joinFlag = "joinFlag";
    private static final int ANIMATION_DURATION = 0;


    @NotNull
    public static SectionsFragment newInstance() {
        return new SectionsFragment();
    }


    @BindView(R.id.swipe_refresh_layout_sections)
    StepikSwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.sections_recycler_view)
    RecyclerView sectionsRecyclerView;

    @BindView(R.id.loadProgressbarOnEmptyScreen)
    ProgressBar loadOnCenterProgressBar;

    @BindView(R.id.reportProblem)
    protected View reportConnectionProblem;

    @BindView(R.id.course_not_found)
    View courseNotParsedView;

    @BindView(R.id.goToCatalog)
    View goToCatalog;

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

    boolean firstLoad;
    boolean isNeedShowCalendarInMenu = false;

    @Inject
    StepikNotificationManager stepikNotificationManager;

    private boolean wasIndexed;
    private Uri urlInWeb;
    private String title;

    private int afterUpdateModulePosition = -1;
    private int modulePosition;
    private boolean isAfterJoining;

    @Override
    protected void injectComponent() {
        App.Companion
                .componentManager()
                .courseGeneralComponent()
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

        sectionsRecyclerView.setVisibility(View.GONE);

        sectionsRecyclerView.setItemAnimator(new SlideInRightAnimator());
        sectionsRecyclerView.getItemAnimator().setRemoveDuration(ANIMATION_DURATION);
        sectionsRecyclerView.getItemAnimator().setAddDuration(ANIMATION_DURATION);
        sectionsRecyclerView.getItemAnimator().setMoveDuration(ANIMATION_DURATION);
        sectionsRecyclerView.getItemAnimator().setChangeDuration(0);

        ProgressHelper.activate(loadOnCenterProgressBar);

        ToolbarHelperKt.initCenteredToolbar(this, R.string.syllabus_title, true);
        onNewIntent(getActivity().getIntent());
    }

    private void setUpToolbarWithCourse() {
        if (course != null && course.getTitle() != null && !course.getTitle().isEmpty()) {
            ToolbarHelperKt.setTitleToCenteredToolbar(this, course.getTitle());
        }
    }

    public void initScreenByCourse() {
        reportConnectionProblem.setVisibility(View.GONE);
        courseNotParsedView.setVisibility(View.GONE);
        resolveJoinCourseView();
        setUpToolbarWithCourse();

        if (course != null && course.getSlug() != null && !wasIndexed) {
            title = getString(R.string.syllabus_title) + ": " + course.getTitle();
            urlInWeb = Uri.parse(StringUtil.getUriForSyllabus(getConfig().getBaseUrl(), course.getSlug()));
        }

        if (isAfterJoining && course != null) {
            isAfterJoining = false;
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
                        getAnalytic().reportEvent(Analytic.Interaction.JOIN_COURSE);
                        getAnalytic().reportAmplitudeEvent(AmplitudeAnalytic.Course.JOINED, MapsKt.mapOf(
                                new Pair<String, Object>(AmplitudeAnalytic.Course.Params.COURSE, course.getId()),
                                new Pair<String, Object>(AmplitudeAnalytic.Course.Params.SOURCE, AmplitudeAnalytic.Course.Values.PREVIEW)
                        ));
                    }
                }
            });
            courseName.setText(course.getTitle());
            Glide.with(this)
                    .load(StepikLogicHelper.getPathForCourseOrEmpty(course, getConfig()))
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
                    getScreenManager().showCourseDescription(this, course);
                }
                return true;


            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        getAnalytic().reportEvent(Analytic.Interaction.REFRESH_SECTIONS);
        if (course != null) {
        } else {
            onNewIntent(getActivity().getIntent());
        }
    }

    @Override
    public void onStop() {
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
        goToCatalog.setOnClickListener(null);
        swipeRefreshLayout.setOnRefreshListener(null);
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.section_unit_menu, menu);

        menu.findItem(R.id.menu_item_calendar).setVisible(isNeedShowCalendarInMenu);
    }

    public void onNewIntent(Intent intent) {
        long simpleCourseId = -1;
        int simpleModulePosition = -1;

        if (intent.getExtras() != null) {
            isAfterJoining = intent.getExtras().getBoolean(joinFlag);
            intent.putExtra(joinFlag, false);

            Course courseInBundle = intent.getExtras().getParcelable(AppConstants.KEY_COURSE_BUNDLE);
            if (courseInBundle != null) {
                course = courseInBundle;
            } else {
                try {
                    simpleCourseId = intent.getExtras().getLong(AppConstants.KEY_COURSE_LONG_ID);
                    simpleModulePosition = intent.getExtras().getInt(AppConstants.KEY_MODULE_POSITION);
                } catch (Exception ex) {
                    //cant parse -> continue
                }
            }

            long hoursDiff = intent.getLongExtra(Analytic.Deadlines.Params.BEFORE_DEADLINE, -1);
            if (hoursDiff != -1) {
                Bundle bundle = new Bundle(1);
                bundle.putLong(Analytic.Deadlines.Params.BEFORE_DEADLINE, hoursDiff);
                getAnalytic().reportEvent(Analytic.Deadlines.PERSONAL_DEADLINE_NOTIFICATION_OPENED, bundle);
            }
        }
        if (course != null) {
            final long courseId = course.getId();
            initScreenByCourse();
        } else if (simpleCourseId > 0 && simpleModulePosition > 0) {
            modulePosition = simpleModulePosition;
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
                        getAnalytic().reportEvent(Analytic.Notification.OPEN_NOTIFICATION);
                    } else if (!action.equals(AppConstants.INTERNAL_STEPIK_ACTION)) {
                        getAnalytic().reportEvent(Analytic.DeepLink.USER_OPEN_SYLLABUS_LINK, simpleCourseId + "");
                        getAnalytic().reportEvent(Analytic.DeepLink.USER_OPEN_LINK_GENERAL);
                    }
                }
            }
        }

    }
}
