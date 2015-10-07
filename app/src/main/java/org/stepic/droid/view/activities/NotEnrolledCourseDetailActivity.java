package org.stepic.droid.view.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.StepicBaseFragmentActivity;
import org.stepic.droid.events.instructors.FailureLoadInstrictorsEvent;
import org.stepic.droid.events.instructors.OnResponseLoadingInstructorsEvent;
import org.stepic.droid.events.instructors.StartLoadingInstructorsEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.User;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.HtmlHelper;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.view.adapters.InstructorAdapter;
import org.stepic.droid.view.layout_managers.WrapContentLinearLayoutManager;
import org.stepic.droid.web.UserStepicResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class NotEnrolledCourseDetailActivity extends StepicBaseFragmentActivity {

    private static final String TAG = "unrolled_course";

    @Bind(R.id.actionbar_close_btn_layout)
    View mCloseButton;

    @Bind(R.id.intro_video)
    ImageView mIntroView;

    @Bind(R.id.description)
    TextView mDescriptionView;

    @Bind(R.id.course_name)
    TextView mCourseNameView;

    @Bind(R.id.instructors_carousel)
    RecyclerView mInstructorsCarousel;

    @Bind(R.id.load_instructors_spinner)
    ProgressBar mInstructorsProgressBar;

    @Bind(R.id.summary)
    TextView mSummaryView;

    @Bind(R.id.requirements)
    TextView mRequirementsView;

    @Bind(R.id.join_course_layout)
    View mJoinCourseView;

    @Bind(R.id.join_course_spinner)
    ProgressBar mJoinCourseSpinner;

    @BindString(R.string.join_course_impossible)
    String joinCourseImpossible;

    @BindString(R.string.join_course_exception)
    String joinCourseException;


    private Course mCourse;
    private List<User> mUserList;
    private InstructorAdapter mInstructorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_not_enrolled_course_detail);
        ButterKnife.bind(this);
        overridePendingTransition(org.stepic.droid.R.anim.slide_in_from_bottom, org.stepic.droid.R.anim.no_transition);
        hideSoftKeypad();

        mCourse = (Course) (getIntent().getExtras().get(AppConstants.KEY_COURSE_BUNDLE));

    }

    @Override
    protected void onStart() {
        super.onStart();

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


//        String urltovideo = "https://player.vimeo.com/external/111345189.hd.mp4?s=ea9aab1c15434d7bfd3515afaf70a9de&profile_id=113&oauth2_token_id=3605157";
//        MediaController mediaController = new MediaController(this);
//        mediaController.setAnchorView(mIntroView);
//        mIntroView.setMediaController(mediaController);
//        mIntroView.setVideoURI(Uri.parse(urltovideo));
//        mIntroView.start();

        mCourseNameView.setText(mCourse.getTitle());
        mDescriptionView.setText(HtmlHelper.fromHtml(mCourse.getDescription()));
        mSummaryView.setText(HtmlHelper.fromHtml(mCourse.getSummary()));
        mRequirementsView.setText(HtmlHelper.fromHtml(mCourse.getRequirements()));
        mJoinCourseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinCourse();
            }
        });

        mUserList = new ArrayList<>();
        mInstructorAdapter = new InstructorAdapter(mUserList, this);
        mInstructorsCarousel.setAdapter(mInstructorAdapter);

        RecyclerView.LayoutManager layoutManager =
                new WrapContentLinearLayoutManager(this,
                        LinearLayoutManager.HORIZONTAL, false);//// TODO: 30.09.15 determine right-to-left-mode
        mInstructorsCarousel.setLayoutManager(layoutManager);


        if (mCourse.getInstructors() != null && mCourse.getInstructors().length != 0) {
            bus.post(new StartLoadingInstructorsEvent(mCourse));
            mShell.getApi().getUsers(mCourse.getInstructors()).enqueue(new Callback<UserStepicResponse>() {
                @Override
                public void onResponse(Response<UserStepicResponse> response, Retrofit retrofit) {
                    bus.post(new OnResponseLoadingInstructorsEvent(mCourse, response, retrofit));
                }

                @Override
                public void onFailure(Throwable t) {
                    bus.post(new FailureLoadInstrictorsEvent(mCourse, t));
                }
            });
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
            mInstructorAdapter.notifyDataSetChanged();
            ProgressHelper.dismiss(mInstructorsProgressBar);
        }
    }

    @Subscribe
    public void onFinishLoading(FailureLoadInstrictorsEvent e) {
        if (e.getCourse() != null && mCourse != null & e.getCourse().getCourseId() == mCourse.getCourseId()) {
            ProgressHelper.dismiss(mInstructorsProgressBar);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mInstructorAdapter = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCourse = null;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.slide_out_to_bottom);
    }


    private void joinCourse() {
        mJoinCourseView.setEnabled(false);
        ProgressHelper.activate(mJoinCourseSpinner);
        mShell.getApi().tryJoinCourse(mCourse).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Response<Void> response, Retrofit retrofit) {
                mShell.getScreenProvider().showCourseDescriptionForEnrolled(NotEnrolledCourseDetailActivity.this, mCourse);
                finish();
                ProgressHelper.dismiss(mJoinCourseSpinner);
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(NotEnrolledCourseDetailActivity.this, joinCourseException,
                        Toast.LENGTH_LONG).show();

                ProgressHelper.dismiss(mJoinCourseSpinner);
                mJoinCourseView.setEnabled(true);
            }
        });
    }
}
