package org.stepic.droid.view.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.stepic.droid.R;
import org.stepic.droid.base.StepicBaseFragmentActivity;
import org.stepic.droid.concurrency.LoadingSectionTask;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Section;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.view.adapters.SectionAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EnrolledCourseActivity extends StepicBaseFragmentActivity {
    private static final String TAG = "enrolledActivity";

    @Bind(R.id.actionbar_close_btn_layout)
    View mCloseButton;

    @Bind(R.id.sections_recycler_view)
    RecyclerView mSectionsRecyclerView;

    @Bind(R.id.load_sections)
    ProgressBar mProgressBar;

    private Course mCourse;
    private LoadingSectionTask mLoadingSectionTask;
    private SectionAdapter mAdapter;
    private List<Section> mSectionList;

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
        mSectionList = new ArrayList<>();
        mAdapter = new SectionAdapter(mSectionList, this);
        mSectionsRecyclerView.setAdapter(mAdapter);

        updateSections();
    }


    private void updateSections() {
        mLoadingSectionTask = new LoadingSectionTask(this, mCourse.getSections()) {
            @Override
            protected void onSuccess(List<Section> sections) {
                super.onSuccess(sections);
                mSectionList.clear();
                mSectionList.addAll(sections);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            protected void onException(Throwable exception) {
                super.onException(exception);
                int exc;
            }
        };
        mLoadingSectionTask.setProgressBar(mProgressBar);
        mLoadingSectionTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");

        if (mLoadingSectionTask != null && mLoadingSectionTask.getStatus() != AsyncTask.Status.FINISHED) {
            mLoadingSectionTask.cancel(true);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        if (mSectionsRecyclerView != null) mSectionsRecyclerView.setAdapter(null);
        if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.slide_out_to_bottom);
    }
}
