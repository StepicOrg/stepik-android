package org.stepic.droid.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.modules.CourseDetailModule;
import org.stepic.droid.core.presenters.CourseFinderPresenter;
import org.stepic.droid.core.presenters.contracts.CourseJoinView;
import org.stepic.droid.core.presenters.CourseJoinerPresenter;
import org.stepic.droid.core.presenters.contracts.LoadCourseView;
import org.stepic.droid.events.courses.CourseCantLoadEvent;
import org.stepic.droid.events.courses.CourseFoundEvent;
import org.stepic.droid.events.courses.CourseUnavailableForUserEvent;
import org.stepic.droid.events.courses.SuccessDropCourseEvent;
import org.stepic.droid.events.instructors.FailureLoadInstructorsEvent;
import org.stepic.droid.events.instructors.OnResponseLoadingInstructorsEvent;
import org.stepic.droid.events.instructors.StartLoadingInstructorsEvent;
import org.stepic.droid.events.joining_course.FailJoinEvent;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.CourseProperty;
import org.stepic.droid.model.User;
import org.stepic.droid.model.Video;
import org.stepic.droid.ui.adapters.CoursePropertyAdapter;
import org.stepic.droid.ui.adapters.InstructorAdapter;
import org.stepic.droid.ui.dialogs.LoadingProgressDialog;
import org.stepic.droid.ui.dialogs.UnauthorizedDialogFragment;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.StepicLogicHelper;
import org.stepic.droid.util.StringUtil;
import org.stepic.droid.util.ThumbnailParser;
import org.stepic.droid.web.UserStepicResponse;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class CourseDetailFragment extends FragmentBase implements LoadCourseView, CourseJoinView {

    private View.OnClickListener onClickReportListener;
    private View header;
    private View footer;
    private DialogFragment unauthorizedDialog;
    private Intent shareIntentWithChooser;
    private GlideDrawableImageViewTarget courseTargetFigSupported;

    public static CourseDetailFragment newInstance(Course course) {
        Bundle args = new Bundle();
        args.putSerializable(AppConstants.KEY_COURSE_BUNDLE, course);
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

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.course_not_found)
    View courseNotFoundView;

    @BindDrawable(R.drawable.ic_course_placeholder)
    Drawable coursePlaceholder;

    private WebView introView;

    private TextView courseNameView;

    private RecyclerView instructorsCarousel;

    View joinCourseView;
    View continueCourseView;

    ProgressDialog joinCourseSpinner;

    @BindString(R.string.join_course_impossible)
    String joinCourseImpossible;

    @BindString(R.string.join_course_exception)
    String joinCourseException;

    @BindString(R.string.join_course_web_exception)
    String joinCourseWebException;

    @BindView(R.id.list_of_course_property)
    ListView coursePropertyListView;

    @BindDrawable(R.drawable.video_placeholder)
    Drawable mVideoPlaceholder;

    @BindView(R.id.report_problem)
    View reportInternetProblem;

    ImageView courseIcon;

    ImageView thumbnail;

    View player;

    //App indexing:
    private GoogleApiClient client;
    private Uri urlInApp;
    private Uri urlInWeb;
    private String mTitle;
    private String mDescription;


    private List<CourseProperty> coursePropertyList;
    private Course course;
    private List<User> instructorsList;
    private InstructorAdapter instructorAdapter;

    public Action getAction() {
        Thing object = new Thing.Builder()
                .setId(urlInWeb.toString())
                .setName(mTitle)
                .setDescription(mDescription)
                .setUrl(urlInApp)
                .build();

        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Inject
    CourseFinderPresenter courseFinderPresenter;

    @Inject
    CourseJoinerPresenter courseJoinerPresenter;

    @Override
    protected void injectComponent() {
        MainApplication.component().plus(new CourseDetailModule()).inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getActivity().overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);
        super.onCreate(savedInstanceState);
        client = new GoogleApiClient.Builder(getActivity()).addApi(AppIndex.API).build();
        setRetainInstance(true);
        instructorsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_detailed, container, false);
        unbinder = ButterKnife.bind(this, v);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //VIEW:
        coursePropertyList = new ArrayList<>();
        joinCourseSpinner = new LoadingProgressDialog(getActivity());
        footer = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.fragment_course_detailed_footer, null, false);
        coursePropertyListView.addFooterView(footer);
        instructorsCarousel = ButterKnife.findById(footer, R.id.instructors_carousel);
//        mInstructorsProgressBar = ButterKnife.findById(footer, R.id.load_progressbar);
//        mInstructorsRootView = ButterKnife.findById(footer, R.id.instructors_root_view);

        header = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.fragment_course_detailed_header, null, false);
        coursePropertyListView.addHeaderView(header);

        courseIcon = ButterKnife.findById(header, R.id.courseIcon);
        joinCourseView = ButterKnife.findById(header, R.id.join_course_layout);
        continueCourseView = ButterKnife.findById(header, R.id.go_to_learn);
        introView = ButterKnife.findById(header, R.id.intro_video);
        thumbnail = ButterKnife.findById(header, R.id.player_thumbnail);
        player = ButterKnife.findById(header, R.id.player_layout);
        courseTargetFigSupported = new GlideDrawableImageViewTarget(courseIcon);
        player.setVisibility(View.GONE);
        courseNameView = ButterKnife.findById(header, R.id.course_name);
        coursePropertyListView.setAdapter(new CoursePropertyAdapter(getActivity(), coursePropertyList));
        hideSoftKeypad();
        instructorAdapter = new InstructorAdapter(instructorsList, getActivity());
        instructorsCarousel.setAdapter(instructorAdapter);
        courseNotFoundView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferenceHelper.getAuthResponseFromStore() != null) {
                    shell.getScreenProvider().showFindCourses(getContext());
                    finish();
                } else {
                    if (!unauthorizedDialog.isAdded()) {
                        unauthorizedDialog.show(getFragmentManager(), null);
                    }
                }
            }
        });

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity(),
                        LinearLayoutManager.HORIZONTAL, false);//// TODO: 30.09.15 determine right-to-left-mode
        instructorsCarousel.setLayoutManager(layoutManager);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        onClickReportListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setOnClickListener(null);
                tryToShowCourse();
            }
        };
        reportInternetProblem.setOnClickListener(onClickReportListener);

        header.setVisibility(View.GONE); //hide while we don't have the course
        footer.setVisibility(View.GONE);

        unauthorizedDialog = UnauthorizedDialogFragment.newInstance();

        courseFinderPresenter.attachView(this);
        courseJoinerPresenter.attachView(this);
        bus.register(this);
        //COURSE RELATED IN ON START
    }

    private void tryToShowCourse() {
        reportInternetProblem.setVisibility(View.GONE); // now we try show -> it is not visible
        course = (Course) (getArguments().getSerializable(AppConstants.KEY_COURSE_BUNDLE));
        if (course == null) {
            //it is not from our activity
            long courseId = getArguments().getLong(AppConstants.KEY_COURSE_LONG_ID);
            if (courseId < 0) {
                onCourseUnavailable(new CourseUnavailableForUserEvent());
            } else {
                //todo SHOW LOADING.
                courseFinderPresenter.findCourseById(courseId);
            }

        } else {
            initScreenByCourse();//ok if mCourse is not null;
        }
    }

    @Override
    public void onCourseFound(CourseFoundEvent event) {
        if (course == null) {
            course = event.getCourse();
            Bundle args = getArguments();
            args.putSerializable(AppConstants.KEY_COURSE_BUNDLE, course);
            initScreenByCourse();
        }
    }

    public void initScreenByCourse() {
        //todo HIDE LOADING AND ERRORS
        reportInternetProblem.setVisibility(View.GONE);
        courseNotFoundView.setVisibility(View.GONE);
        //
        header.setVisibility(View.VISIBLE);

        mTitle = course.getTitle();
        mDescription = course.getSummary();
        if (course.getSlug() != null && !wasIndexed) {
            urlInWeb = Uri.parse(StringUtil.getUriForCourse(config.getBaseUrl(), course.getSlug()));
            urlInApp = StringUtil.getAppUriForCourse(config.getBaseUrl(), course.getSlug());
            reportIndexToGoogle();
        }


        coursePropertyList.clear();
        coursePropertyList.addAll(coursePropertyResolver.getSortedPropertyList(course));
        if (course.getTitle() != null && !course.getTitle().equals("")) {
            courseNameView.setText(course.getTitle());
        } else {
            courseNameView.setVisibility(View.GONE);
        }

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        introView.getLayoutParams().width = width;
        introView.getLayoutParams().height = (9 * width) / 16;
        setUpIntroVideo();

        Glide.with(MainApplication.getAppContext())
                .load(StepicLogicHelper.getPathForCourseOrEmpty(course, config))
                .placeholder(coursePlaceholder)
                .into(courseTargetFigSupported);

        resolveJoinView();
        if (instructorsList.isEmpty()) {
            fetchInstructors();
        }
        else{
            showCurrentInstructors();
        }
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    private void reportIndexToGoogle() {
        if (course != null && !wasIndexed && course.getSlug() != null) {
            if (!client.isConnecting() && !client.isConnected()) {
                client.connect();
            }
            wasIndexed = true;
            AppIndex.AppIndexApi.start(client, getAction());
            analytic.reportEventWithIdName(Analytic.AppIndexing.COURSE_DETAIL, course.getCourseId() + "", course.getTitle());
        }
    }

    private void resolveJoinView() {
        if (course.getEnrollment() != 0) {
            joinCourseView.setVisibility(View.GONE);
            continueCourseView.setVisibility(View.VISIBLE);
            continueCourseView.setEnabled(true);
            continueCourseView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shell.getScreenProvider().showSections(getActivity(), course);
                }
            });
        } else {
            continueCourseView.setVisibility(View.GONE);
            joinCourseView.setVisibility(View.VISIBLE);
            joinCourseView.setEnabled(true);
            joinCourseView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    joinCourse();
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
        if (course != null && course.getInstructors() != null && course.getInstructors().length != 0) {
            bus.post(new StartLoadingInstructorsEvent(course));
            shell.getApi().getUsers(course.getInstructors()).enqueue(new Callback<UserStepicResponse>() {
                @Override
                public void onResponse(Response<UserStepicResponse> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        if (response.body() == null) {
                            bus.post(new FailureLoadInstructorsEvent(course, null));
                        } else {
                            bus.post(new OnResponseLoadingInstructorsEvent(course, response, retrofit));
                        }
                    } else {
                        bus.post(new FailureLoadInstructorsEvent(course, null));
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    bus.post(new FailureLoadInstructorsEvent(course, t));
                }
            });
        } else {
            instructorsCarousel.setVisibility(View.GONE);
        }
    }

    private void setUpIntroVideo() {
        String urlToVideo;

        Video newTypeVideo = course.getIntro_video();
        if (newTypeVideo != null && newTypeVideo.getUrls() != null && !newTypeVideo.getUrls().isEmpty()) {
            urlToVideo = newTypeVideo.getUrls().get(0).getUrl();
            showNewStyleVideo(urlToVideo, newTypeVideo.getThumbnail());
        } else {
            urlToVideo = course.getIntro();
            showOldStyleVideo(urlToVideo);
        }

    }

    private void showNewStyleVideo(final String urlToVideo, String pathThumbnail) {
        introView.setVisibility(View.GONE);
        if (urlToVideo == null || urlToVideo.equals("") || pathThumbnail == null || pathThumbnail.equals("")) {
            introView.setVisibility(View.GONE);
            player.setVisibility(View.GONE);
        } else {
            setThumbnail(pathThumbnail);
            player.setVisibility(View.VISIBLE);
            player.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shell.getScreenProvider().showVideo(getActivity(), urlToVideo);
                }
            });
        }
    }

    private void showOldStyleVideo(String urlToVideo) {
        player.setVisibility(View.GONE);
        if (urlToVideo == null || urlToVideo.equals("")) {
            introView.setVisibility(View.GONE);
            player.setVisibility(View.GONE);
        } else {
            WebSettings webSettings = introView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setAppCacheEnabled(true);
            webSettings.setDomStorageEnabled(true);

            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setPluginState(WebSettings.PluginState.ON);
            introView.setWebChromeClient(new WebChromeClient());
            introView.clearFocus();
            rootView.requestFocus();

            introView.loadUrl(urlToVideo);
            introView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCourseUnavailable(CourseUnavailableForUserEvent event) {
        if (course == null) {
            analytic.reportEvent(Analytic.Interaction.COURSE_USER_TRY_FAIL, event.getCourseId() + "");
            reportInternetProblem.setVisibility(View.GONE);
            courseNotFoundView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onInternetFailWhenCourseIsTriedToLoad(CourseCantLoadEvent event) {
        if (course == null) {
            courseNotFoundView.setVisibility(View.GONE);
            reportInternetProblem.setVisibility(View.VISIBLE);
            reportInternetProblem.setOnClickListener(onClickReportListener);
        }
    }

    @Subscribe
    public void onStartLoadingInstructors(StartLoadingInstructorsEvent e) {
        if (e.getCourse() != null && course != null && e.getCourse().getCourseId() == course.getCourseId()) {
//            ProgressHelper.activate(mInstructorsProgressBar);
            //not react
        }
    }

    @Subscribe
    public void onResponseLoadingInstructors(OnResponseLoadingInstructorsEvent e) {
        if (e.getCourse() != null && course != null && e.getCourse().getCourseId() == course.getCourseId()) {

            List<User> users = e.getResponse().body().getUsers();
            if (users != null && !users.isEmpty()) {
                instructorsList.clear();
                instructorsList.addAll(users);

                showCurrentInstructors();
            } else {
                footer.setVisibility(View.GONE);
            }
//            ProgressHelper.dismiss(mInstructorsProgressBar);
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
            int padding = (int) (widthOfScreen - widthOfAllItems) / 2;
            instructorsCarousel.setPadding(padding, 0, padding, 0);
        } else {
            instructorsCarousel.setPadding(0, 0, 0, 0);
        }
    }

    @Subscribe
    public void onFinishLoading(FailureLoadInstructorsEvent e) {
        if (e.getCourse() != null && course != null && e.getCourse().getCourseId() == course.getCourseId()) {
            footer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        introView.onPause();
    }

    @Override
    public void onStop() {

        if (wasIndexed) {
            AppIndex.AppIndexApi.end(client, getAction());
        }
        if (client != null && client.isConnected() && client.isConnecting()) {
            client.disconnect();
        }
        wasIndexed = false;
        super.onStop();

    }

    @Override
    public void onDestroyView() {
        bus.unregister(this);
        courseJoinerPresenter.detachView(this);
        courseFinderPresenter.detachView(this);
        reportInternetProblem.setOnClickListener(null);
        courseNotFoundView.setOnClickListener(null);
        introView.destroy();
        introView = null;
        instructorAdapter = null;
        joinCourseView.setOnClickListener(null);
        continueCourseView.setOnClickListener(null);
        super.onDestroyView();
        course = null;
    }

    public void finish() {
        Intent intent = new Intent();
        if (course != null) {
            intent.putExtra(AppConstants.COURSE_ID_KEY, (Parcelable) course);
            intent.putExtra(AppConstants.ENROLLMENT_KEY, course.getEnrollment());
            getActivity().setResult(Activity.RESULT_OK, intent);
        }
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }

    private void joinCourse() {
        if (course != null) {
            courseJoinerPresenter.joinCourse(course);
        } else {
            analytic.reportEvent(Analytic.Interaction.JOIN_COURSE_NULL);
        }
    }

    public void onSuccessJoin(SuccessJoinEvent e) {
        if (course != null && e.getCourse() != null && e.getCourse().getCourseId() == course.getCourseId()) {
            e.getCourse().setEnrollment((int) e.getCourse().getCourseId());
            shell.getScreenProvider().showSections(getActivity(), course);
            finish();
        }
        ProgressHelper.dismiss(joinCourseSpinner);
    }

    @Override
    public void showProgress() {
        ProgressHelper.activate(joinCourseSpinner);
    }

    @Override
    public void setEnabledJoinButton(boolean isEnabled) {
        joinCourseView.setEnabled(isEnabled);
    }

    @Subscribe
    public void onFailJoin(FailJoinEvent e) {
        if (course != null) {
            if (e.getCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                Toast.makeText(getActivity(), joinCourseWebException, Toast.LENGTH_LONG).show();
            } else if (e.getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                //UNAUTHORIZED
                //it is just for safety, we should detect no account before send request
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

    private void setThumbnail(String thumbnail) {
        Uri uri = ThumbnailParser.getUriForThumbnail(thumbnail);
        Glide.with(getActivity())
                .load(uri)
                .placeholder(mVideoPlaceholder)
                .error(mVideoPlaceholder)
                .into(this.thumbnail);
    }


    @Subscribe
    public void onSuccessDrop(final SuccessDropCourseEvent e) {
        if (course != null && e.getCourse().getCourseId() == course.getCourseId()) {
            course.setEnrollment(0);
            resolveJoinView();
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
                        analytic.reportEventWithIdName(Analytic.Interaction.SHARE_COURSE, course.getCourseId() + "", course.getTitle());
                    }
                    startActivity(shareIntentWithChooser);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createIntentForSharing() {
        if (course == null) return;

        shareIntentWithChooser = shareHelper.getIntentForCourseSharing(course);
    }
}
