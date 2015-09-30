package org.stepic.droid.view.activities;

import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import org.stepic.droid.R;
import org.stepic.droid.base.StepicBaseFragmentActivity;
import org.stepic.droid.model.Course;
import org.stepic.droid.util.AppConstants;
import org.w3c.dom.Text;

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

    private Course mCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_unrolled_course_detail);
        ButterKnife.bind(this);
        overridePendingTransition(org.stepic.droid.R.anim.slide_in_from_bottom, org.stepic.droid.R.anim.no_transition);
        hideSoftKeypad();

        mCourse = (Course) (getIntent().getExtras().get(AppConstants.KEY_COURSE_BUNDLE));

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
