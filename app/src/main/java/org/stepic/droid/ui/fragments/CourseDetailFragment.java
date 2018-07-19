package org.stepic.droid.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import org.stepic.droid.base.App;
import org.stepic.droid.base.Client;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.core.dropping.contract.DroppingListener;
import org.stepic.droid.core.presenters.ContinueCoursePresenter;
import org.stepic.droid.core.presenters.CourseDetailAnalyticPresenter;
import org.stepic.droid.core.presenters.CourseFinderPresenter;
import org.stepic.droid.core.presenters.CourseJoinerPresenter;
import org.stepic.droid.core.presenters.InstructorsPresenter;
import org.stepic.droid.core.presenters.contracts.ContinueCourseView;
import org.stepic.droid.core.presenters.contracts.CourseDetailAnalyticView;
import org.stepic.droid.core.presenters.contracts.CourseJoinView;
import org.stepic.droid.core.presenters.contracts.InstructorsView;
import org.stepic.droid.core.presenters.contracts.LoadCourseView;
import org.stepik.android.model.Course;
import org.stepic.droid.model.CourseProperty;
import org.stepik.android.model.Section;
import org.stepik.android.model.user.User;
import org.stepik.android.model.Video;
import org.stepic.droid.ui.adapters.CoursePropertyAdapter;
import org.stepic.droid.ui.adapters.InstructorAdapter;
import org.stepic.droid.ui.dialogs.LoadingProgressDialog;
import org.stepic.droid.ui.dialogs.UnauthorizedDialogFragment;
import org.stepic.droid.ui.util.ToolbarHelperKt;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.StepikLogicHelper;
import org.stepic.droid.util.StringUtil;
import org.stepic.droid.util.ThumbnailParser;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import kotlin.Pair;
import kotlin.collections.MapsKt;

public class CourseDetailFragment extends FragmentBase implements
        LoadCourseView,
        CourseJoinView,
        CourseDetailAnalyticView,
        ContinueCourseView,
        DroppingListener,
        InstructorsView {

    private static String instaEnrollKey = "instaEnrollKey";
    private View.OnClickListener onClickReportListener;
    private View header;
    private View footer;
    private DialogFragment unauthorizedDialog;
    private Intent shareIntentWithChooser;
    private GlideDrawableImageViewTarget courseTargetFigSupported;
    private boolean needInstaEnroll;

    public static CourseDetailFragment newInstance(Course course, boolean instaEnroll) {
        Bundle args = new Bundle();
        args.putParcelable(AppConstants.KEY_COURSE_BUNDLE, course);
        args.putBoolean(instaEnrollKey, instaEnroll);
        CourseDetailFragment fragment = new CourseDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //can be -1 if address is incorrect
    public static CourseDetailFragment newInstance(long courseId) {
        Bundle args = new Bundle();
        args.putLong(AppConstants.KEY_COURSE_LONG_ID, courseId);
        CourseDetailFragment fragment = new CourseDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }


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

    @Inject
    CourseFinderPresenter courseFinderPresenter;

    @Inject
    CourseJoinerPresenter courseJoinerPresenter;

    @Inject
    ContinueCoursePresenter continueCoursePresenter;

    @Inject
    CourseDetailAnalyticPresenter courseDetailAnalyticPresenter;

    @Inject
    Client<DroppingListener> courseDroppingListener;

    @Inject
    InstructorsPresenter instructorsPresenter;

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

        courseFinderPresenter.attachView(this);
        courseJoinerPresenter.attachView(this);
        continueCoursePresenter.attachView(this);
        courseDetailAnalyticPresenter.attachView(this);
        instructorsPresenter.attachView(this);
        courseDroppingListener.subscribe(this);
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
        if (course == null) {
            //it is not from our activity
            long courseId = getArguments().getLong(AppConstants.KEY_COURSE_LONG_ID);
            if (courseId < 0) {
                onCourseUnavailable(-1);
            } else {
                //todo SHOW LOADING.
                courseFinderPresenter.findCourseById(courseId);
            }

        } else {
            initScreenByCourse();//ok if course is not null;
        }
    }

    @Override
    public void onCourseFound(@NotNull Course foundCourse) {
        if (course == null) {
            course = foundCourse;
            Bundle args = getArguments();
            args.putParcelable(AppConstants.KEY_COURSE_BUNDLE, course);
            initScreenByCourse();
        }
    }

    public void initScreenByCourse() {
        //todo HIDE LOADING AND ERRORS
        errorView.setVisibility(View.GONE);
        courseNotFoundView.setVisibility(View.GONE);
        //
        header.setVisibility(View.VISIBLE);

        if (course != null) {
            courseDetailAnalyticPresenter.onCourseDetailOpened(course);
        }

        titleString = course.getTitle();
        if (course.getSlug() != null && !wasIndexed) {
            urlInWeb = Uri.parse(StringUtil.getUriForCourse(getConfig().getBaseUrl(), course.getSlug()));
            reportIndexToGoogle();
        }


        coursePropertyList.clear();
        coursePropertyList.addAll(getCoursePropertyResolver().getSortedPropertyList(course));
        if (course.getTitle() != null && !course.getTitle().equals("")) {
            courseNameView.setText(course.getTitle());
        } else {
            courseNameView.setVisibility(View.GONE);
        }

        setUpIntroVideo();

        Glide.with(App.Companion.getAppContext())
                .load(StepikLogicHelper.getPathForCourseOrEmpty(course, getConfig()))
                .placeholder(coursePlaceholder)
                .into(courseTargetFigSupported);

        resolveJoinView();
        if (instructorsList.isEmpty()) {
            fetchInstructors();
        } else {
            showCurrentInstructors();
        }
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }

        if (needInstaEnroll) {
            getAnalytic().reportEvent(Analytic.Anonymous.SUCCESS_LOGIN_AND_ENROLL);
            needInstaEnroll = false;
            joinCourse(true);
        }
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

    private void fetchInstructors() {
        instructorsPresenter.fetchInstructors(course);
    }

    private void setUpIntroVideo() {
        Video newTypeVideo = course.getIntroVideo();
        if (newTypeVideo != null && newTypeVideo.getUrls() != null && !newTypeVideo.getUrls().isEmpty()) {
            showNewStyleVideo(newTypeVideo);
        }
    }

    private void showNewStyleVideo(@NotNull final Video video) {
        if (video.getThumbnail() == null || video.getThumbnail().equals("")) {
            player.setVisibility(View.GONE);
        } else {
            setThumbnail(video.getThumbnail());
            player.setVisibility(View.VISIBLE);
            player.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getScreenManager().showVideo(getActivity(), null, video);
                }
            });
        }
    }

    @Override
    public void onCourseUnavailable(long courseId) {
        if (course == null) {
            getAnalytic().reportEvent(Analytic.Interaction.COURSE_USER_TRY_FAIL, courseId + "");
            errorView.setVisibility(View.GONE);
            courseNotFoundView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onInternetFailWhenCourseIsTriedToLoad() {
        if (course == null) {
            courseNotFoundView.setVisibility(View.GONE);
            errorView.setVisibility(View.VISIBLE);
            tryAgain.setOnClickListener(onClickReportListener);
        }
    }

    private void showCurrentInstructors() {
        footer.setVisibility(View.VISIBLE);
        instructorAdapter.notifyDataSetChanged();

        instructorsCarousel.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                centeringRecycler(this);
                return true;
            }
        });
    }


    private void centeringRecycler(ViewTreeObserver.OnPreDrawListener listener) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int widthOfScreen = size.x;

        int widthOfAllItems = instructorsCarousel.getMeasuredWidth();
        if (widthOfAllItems != 0) {
            instructorsCarousel.getViewTreeObserver().removeOnPreDrawListener(listener);
        }
        if (widthOfScreen > widthOfAllItems) {
            int padding = (widthOfScreen - widthOfAllItems) / 2;
            instructorsCarousel.setPadding(padding, 0, padding, 0);
        } else {
            instructorsCarousel.setPadding(0, 0, 0, 0);
        }
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
        courseDroppingListener.unsubscribe(this);
        instructorsPresenter.detachView(this);
        courseJoinerPresenter.detachView(this);
        courseFinderPresenter.detachView(this);
        continueCoursePresenter.detachView(this);
        courseDetailAnalyticPresenter.detachView(this);
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
            courseJoinerPresenter.joinCourse(course);
            getAnalytic().reportAmplitudeEvent(AmplitudeAnalytic.Course.JOINED, MapsKt.mapOf(
                    new Pair<String, Object>(AmplitudeAnalytic.Course.Params.COURSE, course.getId()),
                    new Pair<String, Object>(AmplitudeAnalytic.Course.Params.SOURCE, isInstaEnroll ? AmplitudeAnalytic.Course.Values.WIDGET : AmplitudeAnalytic.Course.Values.PREVIEW)
            ));
        } else {
            getAnalytic().reportEvent(Analytic.Interaction.JOIN_COURSE_NULL);
        }
    }

    @Override
    public void onSuccessJoin(@NotNull Course joinedCourse) {
        ProgressHelper.dismiss(joinCourseSpinner);
        if (course != null && joinedCourse.getId() == course.getId()) {
            joinedCourse.setEnrollment((int) joinedCourse.getId());
            continueCoursePresenter.continueCourse(course);
        }
    }

    @Override
    public void showProgress() {
        ProgressHelper.activate(joinCourseSpinner);
    }

    @Override
    public void setEnabledJoinButton(boolean isEnabled) {
        joinCourseView.setEnabled(isEnabled);
    }

    @Override
    public void onFailJoin(int code) {
        if (course != null) {
            if (code == HttpURLConnection.HTTP_FORBIDDEN) {
                Toast.makeText(getActivity(), joinCourseWebException, Toast.LENGTH_LONG).show();
            } else if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                //UNAUTHORIZED
                //it is just for safety, we should detect no account before send request
                unauthorizedDialog = UnauthorizedDialogFragment.newInstance(course);
                if (!unauthorizedDialog.isAdded()) {
                    unauthorizedDialog.show(getFragmentManager(), null);
                }
            } else {
                Toast.makeText(getActivity(), joinCourseException,
                        Toast.LENGTH_LONG).show();
            }
            ProgressHelper.dismiss(joinCourseSpinner);
            joinCourseView.setEnabled(true);
        }

    }

    @Override
    public void onShowContinueCourseLoadingDialog() {
        ProgressHelper.activate(joinCourseSpinner);
    }

    @Override
    public void onOpenStep(long courseId, @NotNull Section section, long lessonId, long unitId, int stepPosition) {
        ProgressHelper.dismiss(joinCourseSpinner);
        getScreenManager().continueCourse(getActivity(), courseId, section, lessonId, unitId, stepPosition, true);
    }

    @Override
    public void onOpenAdaptiveCourse(@NotNull Course course) {
        ProgressHelper.dismiss(joinCourseSpinner);
        getScreenManager().continueAdaptiveCourse(getActivity(), course);
    }

    @Override
    public void onAnyProblemWhileContinue(@NotNull Course course) {
        ProgressHelper.dismiss(joinCourseSpinner);
        getScreenManager().showSections(getActivity(), course, true);
    }

    private void setThumbnail(String thumbnail) {
        Uri uri = ThumbnailParser.getUriForThumbnail(thumbnail);
        Glide.with(getActivity())
                .load(uri)
                .placeholder(videoPlaceholder)
                .error(videoPlaceholder)
                .into(this.thumbnail);
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

    @Override
    public void onSuccessDropCourse(@NotNull Course droppedCourse) {
        if (course != null && droppedCourse.getId() == course.getId()) {
            course.setEnrollment(0);
            resolveJoinView();
        }
    }

    @Override
    public void onFailDropCourse(@NotNull Course course) {
        //do nothing
    }

    @Override
    public void onLoadingInstructors() {
        //not react now
    }

    @Override
    public void onFailLoadInstructors() {
        footer.setVisibility(View.GONE);
    }

    @Override
    public void onInstructorsLoaded(@NotNull List<User> users) {
        instructorsList.clear();
        instructorsList.addAll(users);

        showCurrentInstructors();
    }

    @Override
    public void onHideInstructors() {
        footer.setVisibility(View.GONE);
    }
}
