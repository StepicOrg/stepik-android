package org.stepic.droid.view.activities;

import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.stepic.droid.R;
import org.stepic.droid.base.StepicBaseFragmentActivity;
import org.stepic.droid.model.Course;
import org.stepic.droid.util.AppConstants;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UnrolledCourseDetailActivity extends StepicBaseFragmentActivity {

    private static final String TAG = "unrolled_course";

    @Bind (org.stepic.droid.R.id.actionbar_close_btn)
    View mCloseButton;

    private Course mCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_unrolled_course_detail);
        ButterKnife.bind(this);
        overridePendingTransition(org.stepic.droid.R.anim.slide_in_from_bottom, org.stepic.droid.R.anim.no_transition);
        hideSoftKeypad();

        mCourse = (Course)(getIntent().getExtras().get(AppConstants.KEY_COURSE_BUNDLE));

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
}
