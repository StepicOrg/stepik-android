package org.stepic.droid.view.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.base.StepicBaseFragmentActivity;
import org.stepic.droid.concurrency.LoadingUsersTask;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.User;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.view.adapters.InstructorAdapter;
import org.stepic.droid.view.layout_managers.MyLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UnrolledCourseDetailActivity extends StepicBaseFragmentActivity {

    private static final String TAG = "unrolled_course";

    @Bind(R.id.actionbar_close_btn)
    View mCloseButton;

    @Bind(R.id.intro_video)
    ImageView mIntroView;

    @Bind(R.id.description)
    TextView mDescriptionView;

    @Bind(R.id.course_name)
    TextView mCourseNameView;

    @Bind(R.id.instructors_carousel)
    RecyclerView mInstructorsCarousel;

    private Course mCourse;
    private LoadingUsersTask mLoadingUsersTask;
    private List<User> mUserList;
    private InstructorAdapter mInstructorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_unrolled_course_detail);
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
        mDescriptionView.setText(Html.fromHtml(mCourse.getDescription()));

        mUserList = new ArrayList<>();
        mInstructorAdapter = new InstructorAdapter(mUserList, this);
        mInstructorsCarousel.setAdapter(mInstructorAdapter);
        RecyclerView.LayoutManager layoutManager = new MyLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);//// TODO: 30.09.15 determine right-to-left-mode
//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mInstructorsCarousel.setLayoutManager(layoutManager);

        mLoadingUsersTask = new LoadingUsersTask(this, new int[]{38675, 12, 1322160}) {
            @Override
            protected void onSuccess(List<User> users) {
                super.onSuccess(users);
                mUserList.clear();
                mUserList.addAll(users);
                mInstructorAdapter.notifyDataSetChanged();
            }
        };
        //todo: set progress bar for carousel

        mLoadingUsersTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLoadingUsersTask != null && mLoadingUsersTask.getStatus() != AsyncTask.Status.FINISHED) {
            mLoadingUsersTask.cancel(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mLoadingUsersTask != null)
            mLoadingUsersTask.unbind();

        mInstructorAdapter = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        mCourse = null;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.slide_out_to_bottom);
    }
}
