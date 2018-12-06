package org.stepic.droid.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepik.android.model.Course;
import org.stepic.droid.model.CourseProperty;
import org.stepik.android.model.user.User;
import org.stepic.droid.ui.adapters.CoursePropertyAdapter;
import org.stepic.droid.ui.dialogs.LoadingProgressDialog;
import org.stepic.droid.ui.dialogs.UnauthorizedDialogFragment;
import org.stepic.droid.ui.util.ToolbarHelperKt;
import org.stepic.droid.util.AppConstants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;

public class CourseDetailFragment extends FragmentBase {

    private View.OnClickListener onClickReportListener;
    private View header;
    private DialogFragment unauthorizedDialog;

    @BindView(R.id.root_view)
    View rootView;

    @BindView(R.id.course_not_found)
    View courseNotFoundView;

    @BindView(R.id.goToCatalog)
    View goToCatalog;

    @BindDrawable(R.drawable.general_placeholder)
    Drawable coursePlaceholder;

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


    private List<CourseProperty> coursePropertyList;
    private Course course;
    private List<User> instructorsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        instructorsList = new ArrayList<>();
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

        coursePropertyListView.setAdapter(new CoursePropertyAdapter(getActivity(), coursePropertyList));
        hideSoftKeypad();
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
        player.setVisibility(View.GONE);
    }


    private void tryToShowCourse() {
        errorView.setVisibility(View.GONE); // now we try show -> it is not visible
        course = getArguments().getParcelable(AppConstants.KEY_COURSE_BUNDLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        tryToShowCourse();
    }

    @Override
    public void onDestroyView() {
        tryAgain.setOnClickListener(null);
        goToCatalog.setOnClickListener(null);
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
}
