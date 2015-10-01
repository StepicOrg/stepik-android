package org.stepic.droid.view.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import org.stepic.droid.R;
import org.stepic.droid.base.StepicBaseFragmentActivity;
import org.stepic.droid.model.Course;
import org.stepic.droid.util.AppConstants;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EnrolledCourseActivity extends StepicBaseFragmentActivity {
    private static final String TAG = "enrolledActivity";

    @Bind(R.id.actionbar_close_btn_layout)
    View mCloseButton;

    @Bind(R.id.sections_recycler_view)
    RecyclerView mSectionsRecyclerView;


    private Course mCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrolled_course);
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

        mSectionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.slide_out_to_bottom);
    }
}
