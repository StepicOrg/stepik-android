package org.stepic.droid.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.appindexing.builders.Indexables;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.AmplitudeAnalytic;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.core.dropping.contract.DroppingListener;
import org.stepik.android.model.Course;
import org.stepic.droid.model.CourseProperty;
import org.stepik.android.model.user.User;
import org.stepic.droid.ui.adapters.CoursePropertyAdapter;
import org.stepic.droid.ui.adapters.InstructorAdapter;
import org.stepic.droid.ui.dialogs.LoadingProgressDialog;
import org.stepic.droid.ui.dialogs.UnauthorizedDialogFragment;
import org.stepic.droid.ui.util.ToolbarHelperKt;
import org.stepic.droid.util.AppConstants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import kotlin.Pair;
import kotlin.collections.MapsKt;

public class CourseDetailFragment extends FragmentBase {

    private static String instaEnrollKey = "instaEnrollKey";
    private View.OnClickListener onClickReportListener;
    private View header;
    private View footer;
    private DialogFragment unauthorizedDialog;
    private Intent shareIntentWithChooser;
    private GlideDrawableImageViewTarget courseTargetFigSupported;
    private boolean needInstaEnroll;


    @BindView(R.id.root_view)
    View rootView;

    @BindView(R.id.course_not_found)
    View courseNotFoundView;

    @BindView(R.id.goToCatalog)
    View goToCatalog;

    @BindDrawable(R.drawable.general_placeholder)
    Drawable coursePlaceholder;

    private TextView courseNameView;

    private RecyclerView instructorsCarousel;

    View joinCourseView;
    View continueCourseView;

    LoadingProgressDialog joinCourseSpinner;

    @BindString(R.string.join_course_impossible)
    String joinCourseImpossible;

    @BindString(R.string.join_course_exception)
    String joinCourseException;

    @BindString(R.string.join_course_web_exception)
    String joinCourseWebException;

    @BindView(R.id.list_of_course_property)
    ListView coursePropertyListView;

    @BindDrawable(R.drawable.video_placeholder_drawable)
    Drawable videoPlaceholder;

    @BindView(R.id.error)
    View errorView;

    @BindView(R.id.tryAgain)
    View tryAgain;

    ImageView courseIcon;

    ImageView thumbnail;

    View player;

    //App indexing:
    private Uri urlInWeb;
    private String titleString;


    private List<CourseProperty> coursePropertyList;
    private Course course;
    private List<User> instructorsList;
    private InstructorAdapter instructorAdapter;

    public Action getAction() {
        return Actions.newView(titleString, urlInWeb.toString());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        instructorsList = new ArrayList<>();
        needInstaEnroll = getArguments().getBoolean(instaEnrollKey); //if not exist -> false
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_detailed, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        //VIEW:
        coursePropertyList = new ArrayList<>();
        joinCourseSpinner = new LoadingProgressDialog(getActivity());
        LayoutInflater layoutInflater = getLayoutInflater();
        initHeader(layoutInflater);
        initFooter(layoutInflater);

        coursePropertyListView.setAdapter(new CoursePropertyAdapter(getActivity(), coursePropertyList));
        hideSoftKeypad();
        instructorAdapter = new InstructorAdapter(instructorsList, getActivity());
        instructorsCarousel.setAdapter(instructorAdapter);
        goToCatalog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSharedPreferenceHelper().getAuthResponseFromStore() != null) {
                    getScreenManager().showCatalog(getContext());
                    finish();
                } else {
                    unauthorizedDialog = UnauthorizedDialogFragment.newInstance(course);
                    if (!unauthorizedDialog.isAdded()) {
                        unauthorizedDialog.show(getFragmentManager(), null);
                    }
                }
            }
        });

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity(),
                        LinearLayoutManager.HORIZONTAL, false);
        instructorsCarousel.setLayoutManager(layoutManager);
        ToolbarHelperKt.initCenteredToolbar(this, R.string.course_info_title, true);
        onClickReportListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setOnClickListener(null);
                tryToShowCourse();
            }
        };
        tryAgain.setOnClickListener(onClickReportListener);

        header.setVisibility(View.GONE); //hide while we don't have the course
        footer.setVisibility(View.GONE);

        //COURSE RELATED IN ON START
    }

    private void initHeader(LayoutInflater layoutInflater) {
        header = layoutInflater.inflate(R.layout.fragment_course_detailed_header, coursePropertyListView, false);
        coursePropertyListView.addHeaderView(header);

        courseIcon = header.findViewById(R.id.courseIcon);
        joinCourseView = header.findViewById(R.id.join_course_layout);
        continueCourseView = header.findViewById(R.id.go_to_learn);
        thumbnail = header.findViewById(R.id.playerThumbnail);
        player = header.findViewById(R.id.playerLayout);
        courseTargetFigSupported = new GlideDrawableImageViewTarget(courseIcon);
        player.setVisibility(View.GONE);
        courseNameView = header.findViewById(R.id.course_name);
    }

    private void initFooter(LayoutInflater layoutInflater) {
        footer = layoutInflater.inflate(R.layout.fragment_course_detailed_footer, coursePropertyListView, false);
        coursePropertyListView.addFooterView(footer);
        instructorsCarousel = footer.findViewById(R.id.instructors_carousel);
    }


    private void tryToShowCourse() {
        errorView.setVisibility(View.GONE); // now we try show -> it is not visible
        course = getArguments().getParcelable(AppConstants.KEY_COURSE_BUNDLE);
    }

    private void reportIndexToGoogle() {
        if (course != null && !wasIndexed && course.getSlug() != null) {
            wasIndexed = true;
            FirebaseAppIndex.getInstance().update(getIndexable());
            FirebaseUserActions.getInstance().start(getAction());
            getAnalytic().reportEventWithIdName(Analytic.AppIndexing.COURSE_DETAIL, course.getId() + "", course.getTitle());
        }
    }

    private Indexable getIndexable() {
        return Indexables.newSimple(titleString, urlInWeb.toString());
    }

    private void resolveJoinView() {
        if (course.getEnrollment() != 0) {
            joinCourseView.setVisibility(View.GONE);
            continueCourseView.setVisibility(View.VISIBLE);
            continueCourseView.setEnabled(true);
            continueCourseView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getScreenManager().showSections(getActivity(), course);
                }
            });
        } else {
            continueCourseView.setVisibility(View.GONE);
            joinCourseView.setVisibility(View.VISIBLE);
            joinCourseView.setEnabled(true);
            joinCourseView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getAnalytic().reportEvent(Analytic.Interaction.JOIN_COURSE);
                    joinCourse(false);
                }
            });
        }
    }

    boolean wasIndexed = false;

    @Override
    public void onStart() {
        super.onStart();
        tryToShowCourse();
        reportIndexToGoogle();
    }

    @Override
    public void onStop() {

        if (wasIndexed) {
            FirebaseUserActions.getInstance().end(getAction());
        }
        wasIndexed = false;
        super.onStop();

    }

    @Override
    public void onDestroyView() {
        tryAgain.setOnClickListener(null);
        goToCatalog.setOnClickListener(null);
        instructorAdapter = null;
        joinCourseView.setOnClickListener(null);
        continueCourseView.setOnClickListener(null);
        super.onDestroyView();
        course = null;
    }

    public void finish() {
        Intent intent = new Intent();
        if (course != null) {
            intent.putExtra(AppConstants.COURSE_ID_KEY, course);
            intent.putExtra(AppConstants.ENROLLMENT_KEY, course.getEnrollment());
            getActivity().setResult(Activity.RESULT_OK, intent);
        }
        getActivity().finish();
    }

    private void joinCourse(boolean isInstaEnroll) {
        if (course != null) {
            getAnalytic().reportAmplitudeEvent(AmplitudeAnalytic.Course.JOINED, MapsKt.mapOf(
                    new Pair<String, Object>(AmplitudeAnalytic.Course.Params.COURSE, course.getId()),
                    new Pair<String, Object>(AmplitudeAnalytic.Course.Params.SOURCE, isInstaEnroll ? AmplitudeAnalytic.Course.Values.WIDGET : AmplitudeAnalytic.Course.Values.PREVIEW)
            ));
        } else {
            getAnalytic().reportEvent(Analytic.Interaction.JOIN_COURSE_NULL);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (course != null) {
            inflater.inflate(R.menu.share_menu, menu);
            createIntentForSharing();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                if (shareIntentWithChooser != null) {
                    if (course != null && course.getTitle() != null) {
                        getAnalytic().reportEventWithIdName(Analytic.Interaction.SHARE_COURSE, course.getId() + "", course.getTitle());
                    }
                    startActivity(shareIntentWithChooser);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createIntentForSharing() {
        if (course == null) return;

        shareIntentWithChooser = getShareHelper().getIntentForCourseSharing(course);
    }
}
