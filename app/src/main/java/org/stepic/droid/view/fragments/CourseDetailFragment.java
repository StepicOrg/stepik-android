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

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.concurrency.tasks.UpdateCourseTask;
import org.stepic.droid.events.instructors.FailureLoadInstructorsEvent;
import org.stepic.droid.events.instructors.OnResponseLoadingInstructorsEvent;
import org.stepic.droid.events.instructors.StartLoadingInstructorsEvent;
import org.stepic.droid.events.joining_course.FailJoinEvent;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.CourseProperty;
import org.stepic.droid.model.User;
import org.stepic.droid.model.Video;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.ThumbnailParser;
import org.stepic.droid.view.adapters.CoursePropertyAdapter;
import org.stepic.droid.view.adapters.InstructorAdapter;
import org.stepic.droid.web.UserStepicResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class CourseDetailFragment extends FragmentBase {

    public static CourseDetailFragment newInstance(Course course) {
        Bundle args = new Bundle();
        args.putSerializable(AppConstants.KEY_COURSE_BUNDLE, course);
        CourseDetailFragment fragment = new CourseDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Bind(R.id.root_view)
    View mRootView;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    private WebView mIntroView;

    private TextView mCourseNameView;

    private RecyclerView mInstructorsCarousel;

    private ProgressBar mInstructorsProgressBar;

    @Bind(R.id.join_course_layout)
    View mJoinCourseView;

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


    ImageView mThumbnail;

    View mPlayer;


    private List<CourseProperty> mCoursePropertyList;
    private Course mCourse;
    private List<User> mUserList;
    private InstructorAdapter mInstructorAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_detailed, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mJoinCourseSpinner = new ProgressDialog(getActivity());
        mJoinCourseSpinner.setTitle(getString(R.string.loading));
        mJoinCourseSpinner.setMessage(getString(R.string.loading_message));
        mJoinCourseSpinner.setCancelable(false);

        mCourse = (Course) (getArguments().getSerializable(AppConstants.KEY_COURSE_BUNDLE));
        mCoursePropertyList = mCoursePropertyResolver.getSortedPropertyList(mCourse);

        View footer = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.fragment_course_detailed_footer, null, false);
        mCoursePropertyListView.addFooterView(footer);
        mInstructorsCarousel = ButterKnife.findById(footer, R.id.instructors_carousel);
        mInstructorsProgressBar = ButterKnife.findById(footer, R.id.load_progressbar);

        View header = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.fragment_course_detailed_header, null, false);
        mCoursePropertyListView.addHeaderView(header);
        mIntroView = ButterKnife.findById(header, R.id.intro_video);
        mThumbnail = ButterKnife.findById(header, R.id.player_thumbnail);
        mPlayer = ButterKnife.findById(header, R.id.player_layout);
        mPlayer.setVisibility(View.GONE);
        mCourseNameView = ButterKnife.findById(header, R.id.course_name);


        mCoursePropertyListView.setAdapter(new CoursePropertyAdapter(getActivity(), mCoursePropertyList));

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

        getActivity().overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);
        hideSoftKeypad();
        setUpIntroVideo();

        mUserList = new ArrayList<>();
        mInstructorAdapter = new InstructorAdapter(mUserList, getActivity());
        mInstructorsCarousel.setAdapter(mInstructorAdapter);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity(),
                        LinearLayoutManager.HORIZONTAL, false);//// TODO: 30.09.15 determine right-to-left-mode
        mInstructorsCarousel.setLayoutManager(layoutManager);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if (mCourse.getEnrollment() != 0) {
            mJoinCourseView.setVisibility(View.GONE);
        } else {
            mJoinCourseView.setVisibility(View.VISIBLE);
            mJoinCourseView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    joinCourse();
                }
            });
        }

        bus.register(this);
        fetchInstructors();
    }

    private void fetchInstructors() {
        if (mCourse.getInstructors() != null && mCourse.getInstructors().length != 0) {
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


    @Subscribe
    public void onStartLoadingInstructors(StartLoadingInstructorsEvent e) {
        if (e.getCourse() != null && mCourse != null & e.getCourse().getCourseId() == mCourse.getCourseId()) {
            ProgressHelper.activate(mInstructorsProgressBar);
        }
    }

    @Subscribe
    public void onResponseLoadingInstructors(OnResponseLoadingInstructorsEvent e) {
        if (e.getCourse() != null && mCourse != null & e.getCourse().getCourseId() == mCourse.getCourseId()) {

            List<User> users = e.getResponse().body().getUsers();

            mUserList.clear();
            mUserList.addAll(users);

            mInstructorsCarousel.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mInstructorsCarousel.getViewTreeObserver().removeOnPreDrawListener(this);
                    centeringRecycler();
                    return true;
                }
            });

            mInstructorAdapter.notifyDataSetChanged();

            ProgressHelper.dismiss(mInstructorsProgressBar);
        }
    }

    private void centeringRecycler() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int widthOfScreen = size.x;


        int widthOfAllItems = mInstructorsCarousel.getMeasuredWidth();
        if (widthOfScreen > widthOfAllItems) {
            int padding = (int) (widthOfScreen - widthOfAllItems) / 2;
            mInstructorsCarousel.setPadding(padding, 0, padding, 0);
        }
    }

    @Subscribe
    public void onFinishLoading(FailureLoadInstructorsEvent e) {
        if (e.getCourse() != null && mCourse != null & e.getCourse().getCourseId() == mCourse.getCourseId()) {
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
        super.onStop();
        bus.unregister(this);
    }

    @Override
    public void onDestroyView() {
        mIntroView.destroy();
        mIntroView = null;
        mInstructorAdapter = null;
        super.onDestroyView();
        mCourse = null;
    }

    public void finish() {
        Intent intent = new Intent();
        intent.putExtra(AppConstants.COURSE_ID_KEY, (Parcelable) mCourse);
        intent.putExtra(AppConstants.ENROLLMENT_KEY, mCourse.getEnrollment());
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }


    private void joinCourse() {
        mJoinCourseView.setEnabled(false);
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
                    bus.post(new FailJoinEvent(response));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                bus.post(new FailJoinEvent());
            }
        });
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
        if (e.getResponse() != null && e.getResponse().code() == 403) {
            Toast.makeText(getActivity(), joinCourseWebException, Toast.LENGTH_LONG).show();

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
