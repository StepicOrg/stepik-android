package org.stepic.droid.view.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.DraweeView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.concurrency.tasks.UpdateCourseTask;
import org.stepic.droid.events.courses.CourseCantLoadEvent;
import org.stepic.droid.events.courses.CourseFoundEvent;
import org.stepic.droid.events.courses.CourseUnavailableForUserEvent;
import org.stepic.droid.events.instructors.FailureLoadInstructorsEvent;
import org.stepic.droid.events.instructors.OnResponseLoadingInstructorsEvent;
import org.stepic.droid.events.instructors.StartLoadingInstructorsEvent;
import org.stepic.droid.events.joining_course.FailJoinEvent;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.CourseProperty;
import org.stepic.droid.model.User;
import org.stepic.droid.model.Video;
import org.stepic.droid.presenters.course_finder.CourseFinderPresenter;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.JsonHelper;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.StepicLogicHelper;
import org.stepic.droid.util.StringUtil;
import org.stepic.droid.util.ThumbnailParser;
import org.stepic.droid.view.abstraction.LoadCourseView;
import org.stepic.droid.view.adapters.CoursePropertyAdapter;
import org.stepic.droid.view.adapters.InstructorAdapter;
import org.stepic.droid.view.custom.LoadingProgressDialog;
import org.stepic.droid.view.dialogs.UnauthorizedDialogFragment;
import org.stepic.droid.web.AuthenticationStepicResponse;
import org.stepic.droid.web.UserStepicResponse;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class CourseDetailFragment extends FragmentBase implements LoadCourseView {


    private View.OnClickListener onClickReportListener;
    private View header;
    private View footer;
    private DialogFragment unauthorizedDialog;

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


    @Bind(R.id.root_view)
    View mRootView;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.course_not_found)
    View courseNotFoundView;

    private WebView mIntroView;

    private TextView mCourseNameView;

    private RecyclerView mInstructorsCarousel;

    @Deprecated
    private ProgressBar mInstructorsProgressBar; //useless

    private View mInstructorsRootView;

    View mJoinCourseView;
    View continueCourseView;

    ProgressDialog mJoinCourseSpinner;

    @BindString(R.string.join_course_impossible)
    String joinCourseImpossible;

    @BindString(R.string.join_course_exception)
    String joinCourseException;

    @BindString(R.string.join_course_web_exception)
    String joinCourseWebException;

    @Bind(R.id.list_of_course_property)
    ListView mCoursePropertyListView;

    @BindDrawable(R.drawable.video_placeholder)
    Drawable mVideoPlaceholder;

    @Bind(R.id.report_problem)
    View reportInternetProblem;

    DraweeView courseIcon;

    ImageView mThumbnail;

    View mPlayer;

    //App indexing:
    private GoogleApiClient mClient;
    private Uri mUrl;
    private String mTitle;
    private String mDescription;


    private List<CourseProperty> mCoursePropertyList;
    private Course mCourse;
    private List<User> mUserList;
    private InstructorAdapter mInstructorAdapter;

    public Action getAction() {
        Thing object = new Thing.Builder()
                .setId(mUrl.getEncodedPath())
                .setName(mTitle)
                .setDescription(mDescription)
                .setUrl(mUrl)
                .build();

        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Inject
    @Named(AppConstants.ABOUT_NAME_INJECTION_COURSE_FINDER)
    CourseFinderPresenter courseFinderPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MainApplication.component().inject(this);
        getActivity().overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);
        super.onCreate(savedInstanceState);
        mClient = new GoogleApiClient.Builder(getActivity()).addApi(AppIndex.API).build();
        //// FIXME: 17.06.16 now on rotate instructors are reloading, we should use presenter or retain instance
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_detailed, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //VIEW:
        mCoursePropertyList = new ArrayList<>();
        mJoinCourseSpinner = new LoadingProgressDialog(getActivity());
        footer = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.fragment_course_detailed_footer, null, false);
        mCoursePropertyListView.addFooterView(footer);
        mInstructorsCarousel = ButterKnife.findById(footer, R.id.instructors_carousel);
        mInstructorsProgressBar = ButterKnife.findById(footer, R.id.load_progressbar);
        mInstructorsRootView = ButterKnife.findById(footer, R.id.instructors_root_view);

        header = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.fragment_course_detailed_header, null, false);
        mCoursePropertyListView.addHeaderView(header);

        courseIcon = ButterKnife.findById(header, R.id.courseIcon);
        mJoinCourseView = ButterKnife.findById(header, R.id.join_course_layout);
        continueCourseView = ButterKnife.findById(header, R.id.go_to_learn);
        mIntroView = ButterKnife.findById(header, R.id.intro_video);
        mThumbnail = ButterKnife.findById(header, R.id.player_thumbnail);
        mPlayer = ButterKnife.findById(header, R.id.player_layout);
        mPlayer.setVisibility(View.GONE);
        mCourseNameView = ButterKnife.findById(header, R.id.course_name);
        mCoursePropertyListView.setAdapter(new CoursePropertyAdapter(getActivity(), mCoursePropertyList));
        hideSoftKeypad();
        mUserList = new ArrayList<>();
        mInstructorAdapter = new InstructorAdapter(mUserList, getActivity());
        mInstructorsCarousel.setAdapter(mInstructorAdapter);
        courseNotFoundView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSharedPreferenceHelper.getAuthResponseFromStore() != null) {
                    mShell.getScreenProvider().showFindCourses(getContext());
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
        mInstructorsCarousel.setLayoutManager(layoutManager);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mInstructorsRootView.setVisibility(View.GONE);//show only when is LOADED and EXIST!
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

        //COURSE RELATED IN ON START
    }

    private void tryToShowCourse() {
        reportInternetProblem.setVisibility(View.GONE); // now we try show -> it is not visible
        mCourse = (Course) (getArguments().getSerializable(AppConstants.KEY_COURSE_BUNDLE));
        if (mCourse == null) {
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
        if (mCourse == null) {
            mCourse = event.getCourse();
            Bundle args = getArguments();
            args.putSerializable(AppConstants.KEY_COURSE_BUNDLE, mCourse);
            initScreenByCourse();
        }
    }

    public void initScreenByCourse() {
        //todo HIDE LOADING AND ERRORS
        reportInternetProblem.setVisibility(View.GONE);
        courseNotFoundView.setVisibility(View.GONE);
        //
        header.setVisibility(View.VISIBLE);
        footer.setVisibility(View.VISIBLE);

        mTitle = mCourse.getTitle();
        mDescription = mCourse.getSummary();
        if (mCourse.getSlug() != null && !wasIndexed) {
            mUrl = Uri.parse(StringUtil.getUriForCourse(config.getBaseUrl(), mCourse.getSlug()));
            wasIndexed = true;
            AppIndex.AppIndexApi.start(mClient, getAction());
            YandexMetrica.reportEvent("appindexing", JsonHelper.toJson(mCourse.getCourseId()));
        }


        mCoursePropertyList.clear();
        mCoursePropertyList.addAll(mCoursePropertyResolver.getSortedPropertyList(mCourse));
        if (mCourse.getTitle() != null && !mCourse.getTitle().equals("")) {
            mCourseNameView.setText(mCourse.getTitle());
        } else {
            mCourseNameView.setVisibility(View.GONE);
        }

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        mIntroView.getLayoutParams().width = width;
        mIntroView.getLayoutParams().height = (9 * width) / 16;
        setUpIntroVideo();

        courseIcon.setController(StepicLogicHelper.getControllerForCourse(mCourse, config));

        if (mCourse.getEnrollment() != 0) {
            mJoinCourseView.setVisibility(View.GONE);
            continueCourseView.setVisibility(View.VISIBLE);
            continueCourseView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShell.getScreenProvider().showSections(getActivity(), mCourse);
                }
            });
        } else {
            continueCourseView.setVisibility(View.GONE);
            mJoinCourseView.setVisibility(View.VISIBLE);
            mJoinCourseView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    joinCourse();
                }
            });
        }
        fetchInstructors();
    }

    boolean wasIndexed = false;

    @Override
    public void onStart() {
        super.onStart();
        bus.register(this);
        courseFinderPresenter.onStart(this);
        tryToShowCourse();
        mClient.connect();
        if (mCourse != null && !wasIndexed && mCourse.getSlug() != null) {
            wasIndexed = true;
            AppIndex.AppIndexApi.start(mClient, getAction());
        }
    }

    private void fetchInstructors() {
        if (mCourse != null && mCourse.getInstructors() != null && mCourse.getInstructors().length != 0) {
            bus.post(new StartLoadingInstructorsEvent(mCourse));
            mShell.getApi().getUsers(mCourse.getInstructors()).enqueue(new Callback<UserStepicResponse>() {
                @Override
                public void onResponse(Response<UserStepicResponse> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        if (response.body() == null) {
                            bus.post(new FailureLoadInstructorsEvent(mCourse, null));
                        } else {
                            bus.post(new OnResponseLoadingInstructorsEvent(mCourse, response, retrofit));
                        }
                    } else {
                        bus.post(new FailureLoadInstructorsEvent(mCourse, null));
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    bus.post(new FailureLoadInstructorsEvent(mCourse, t));
                }
            });
        } else {
            mInstructorsCarousel.setVisibility(View.GONE);
        }
    }

    private void setUpIntroVideo() {
        String urlToVideo;

        Video newTypeVideo = mCourse.getIntro_video();
        if (newTypeVideo != null && newTypeVideo.getUrls() != null && !newTypeVideo.getUrls().isEmpty()) {
            urlToVideo = newTypeVideo.getUrls().get(0).getUrl();
            showNewStyleVideo(urlToVideo, newTypeVideo.getThumbnail());
        } else {
            urlToVideo = mCourse.getIntro();
            showOldStyleVideo(urlToVideo);
        }

    }

    private void showNewStyleVideo(final String urlToVideo, String pathThumbnail) {
        mIntroView.setVisibility(View.GONE);
        if (urlToVideo == null || urlToVideo.equals("") || pathThumbnail == null || pathThumbnail.equals("")) {
            mIntroView.setVisibility(View.GONE);
            mPlayer.setVisibility(View.GONE);
        } else {
            setThumbnail(pathThumbnail);
            mPlayer.setVisibility(View.VISIBLE);
            mPlayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShell.getScreenProvider().showVideo(getActivity(), urlToVideo);
                }
            });
        }
    }

    private void showOldStyleVideo(String urlToVideo) {
        mPlayer.setVisibility(View.GONE);
        if (urlToVideo == null || urlToVideo.equals("")) {
            mIntroView.setVisibility(View.GONE);
            mPlayer.setVisibility(View.GONE);
        } else {
            WebSettings webSettings = mIntroView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setAppCacheEnabled(true);
            webSettings.setDomStorageEnabled(true);

            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setPluginState(WebSettings.PluginState.ON);
            mIntroView.setWebChromeClient(new WebChromeClient());
            mIntroView.clearFocus();
            mRootView.requestFocus();

            mIntroView.loadUrl(urlToVideo);
            mIntroView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCourseUnavailable(CourseUnavailableForUserEvent event) {
        //// TODO: 16.06.16 SHOW ERROR: CAN'T OPEN COURSE, TRY TO FIND IN SEARCH (Link to featured)
        YandexMetrica.reportEvent(AppConstants.COURSE_USER_TRY_FAIL, JsonHelper.toJson(event));
        int i = 0;
        reportInternetProblem.setVisibility(View.GONE);
        courseNotFoundView.setVisibility(View.VISIBLE);

    }

    @Override
    public void onInternetFailWhenCourseIsTriedToLoad(CourseCantLoadEvent event) {
        reportInternetProblem.setVisibility(View.VISIBLE);
        reportInternetProblem.setOnClickListener(onClickReportListener);
    }

    @Subscribe
    public void onStartLoadingInstructors(StartLoadingInstructorsEvent e) {
        if (e.getCourse() != null && mCourse != null & e.getCourse().getCourseId() == mCourse.getCourseId()) {
            ProgressHelper.activate(mInstructorsProgressBar);
        }
    }

    @Subscribe
    public void onResponseLoadingInstructors(OnResponseLoadingInstructorsEvent e) {
        if (e.getCourse() != null && mCourse != null && e.getCourse().getCourseId() == mCourse.getCourseId()) {

            List<User> users = e.getResponse().body().getUsers();
            if (users != null && !users.isEmpty()) {
                mInstructorsRootView.setVisibility(View.VISIBLE);

                mUserList.clear();
                mUserList.addAll(users);
                mInstructorAdapter.notifyDataSetChanged();

                mInstructorsCarousel.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        centeringRecycler(this);
                        return true;
                    }
                });
            } else {
                mInstructorsRootView.setVisibility(View.GONE);
            }
            ProgressHelper.dismiss(mInstructorsProgressBar);
        }
    }

    private void centeringRecycler(ViewTreeObserver.OnPreDrawListener listener) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int widthOfScreen = size.x;

        int widthOfAllItems = mInstructorsCarousel.getMeasuredWidth();
        if (widthOfAllItems != 0) {
            mInstructorsCarousel.getViewTreeObserver().removeOnPreDrawListener(listener);
        }
        if (widthOfScreen > widthOfAllItems) {
            int padding = (int) (widthOfScreen - widthOfAllItems) / 2;
            mInstructorsCarousel.setPadding(padding, 0, padding, 0);
        } else {
            mInstructorsCarousel.setPadding(0, 0, 0, 0);
        }
    }

    @Subscribe
    public void onFinishLoading(FailureLoadInstructorsEvent e) {
        if (e.getCourse() != null && mCourse != null & e.getCourse().getCourseId() == mCourse.getCourseId()) {
            mInstructorsRootView.setVisibility(View.GONE);
            ProgressHelper.dismiss(mInstructorsProgressBar);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mIntroView.onPause();
    }

    @Override
    public void onStop() {
        bus.unregister(this);
        if (wasIndexed) {
            AppIndex.AppIndexApi.end(mClient, getAction());
        }
        mClient.disconnect();
        super.onStop();

    }

    @Override
    public void onDestroyView() {
        courseFinderPresenter.onDestroy();
        reportInternetProblem.setOnClickListener(null);
        courseNotFoundView.setOnClickListener(null);
        mIntroView.destroy();
        mIntroView = null;
        mInstructorAdapter = null;
        mJoinCourseView.setOnClickListener(null);
        continueCourseView.setOnClickListener(null);
        super.onDestroyView();
        mCourse = null;
    }

    public void finish() {
        Intent intent = new Intent();
        if (mCourse != null) {
            intent.putExtra(AppConstants.COURSE_ID_KEY, (Parcelable) mCourse);
            intent.putExtra(AppConstants.ENROLLMENT_KEY, mCourse.getEnrollment());
            getActivity().setResult(Activity.RESULT_OK, intent);
        }
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }

    private void joinCourse() {
        mJoinCourseView.setEnabled(false);
        AuthenticationStepicResponse response = mSharedPreferenceHelper.getAuthResponseFromStore();
        if (response != null) {
            ProgressHelper.activate(mJoinCourseSpinner);
            mShell.getApi().tryJoinCourse(mCourse).enqueue(new Callback<Void>() {
                private final Course localCopy = mCourse;

                @Override
                public void onResponse(Response<Void> response, Retrofit retrofit) {
                    if (response.isSuccess()) {

                        localCopy.setEnrollment((int) localCopy.getCourseId());

                        UpdateCourseTask updateCourseTask = new UpdateCourseTask(DatabaseFacade.Table.enrolled, localCopy);
                        updateCourseTask.executeOnExecutor(mThreadPoolExecutor);

                        UpdateCourseTask updateCourseFeaturedTask = new UpdateCourseTask(DatabaseFacade.Table.featured, localCopy);
                        updateCourseFeaturedTask.executeOnExecutor(mThreadPoolExecutor);

                        bus.post(new SuccessJoinEvent(localCopy));

                    } else {
                        bus.post(new FailJoinEvent(response.code()));
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    bus.post(new FailJoinEvent());
                }
            });
        } else {
            bus.post(new FailJoinEvent(HttpURLConnection.HTTP_UNAUTHORIZED));
        }
    }

    @Subscribe
    public void onSuccessJoin(SuccessJoinEvent e) {
        e.getCourse().setEnrollment((int) e.getCourse().getCourseId());
        mShell.getScreenProvider().showSections(getActivity(), mCourse);
        finish();
        ProgressHelper.dismiss(mJoinCourseSpinner);
    }

    @Subscribe
    public void onFailJoin(FailJoinEvent e) {
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
        ProgressHelper.dismiss(mJoinCourseSpinner);
        mJoinCourseView.setEnabled(true);

    }

    private void setThumbnail(String thumbnail) {
        Uri uri = ThumbnailParser.getUriForThumbnail(thumbnail);
        Picasso.with(getActivity())
                .load(uri)
                .placeholder(mVideoPlaceholder)
                .error(mVideoPlaceholder)
                .into(mThumbnail);
    }
}
