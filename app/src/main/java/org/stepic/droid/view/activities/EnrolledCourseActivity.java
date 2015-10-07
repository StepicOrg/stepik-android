package org.stepic.droid.view.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.StepicBaseFragmentActivity;
import org.stepic.droid.events.sections.FailureResponseSectionEvent;
import org.stepic.droid.events.sections.SuccessResponseSectionsEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Section;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.view.adapters.SectionAdapter;
import org.stepic.droid.view.decorators.DividerItemDecoration;
import org.stepic.droid.web.SectionsStepicResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class EnrolledCourseActivity extends StepicBaseFragmentActivity {
    private static final String TAG = "enrolledActivity";

    @Bind(R.id.sections_recycler_view)
    RecyclerView mSectionsRecyclerView;

    @Bind(R.id.load_sections)
    ProgressBar mProgressBar;

    @Bind(R.id.toolbar)
    android.support.v7.widget.Toolbar mToolbar;

    private Course mCourse;
    private SectionAdapter mAdapter;
    private List<Section> mSectionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrolled_course);
        ButterKnife.bind(this);
        overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);
        hideSoftKeypad();

        mCourse = (Course) (getIntent().getExtras().get(AppConstants.KEY_COURSE_BUNDLE));
    }

    @Override
    protected void onStart() {
        super.onStart();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSectionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSectionList = new ArrayList<>();
        mAdapter = new SectionAdapter(mSectionList, this);
        mSectionsRecyclerView.setAdapter(mAdapter);
        mSectionsRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        updateSections();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateSections() {
        ProgressHelper.activate(mProgressBar);
        mShell.getApi().getSections(mCourse.getSections()).enqueue(new Callback<SectionsStepicResponse>() {
            @Override
            public void onResponse(Response<SectionsStepicResponse> response, Retrofit retrofit) {
                bus.post(new SuccessResponseSectionsEvent(mCourse, response, retrofit));

            }

            @Override
            public void onFailure(Throwable t) {
                bus.post(new FailureResponseSectionEvent(mCourse));
            }
        });
    }

    @Subscribe
    public void onSuccessDownload(SuccessResponseSectionsEvent e) {
        if (mCourse.getCourseId() == e.getCourseOfSection().getCourseId()) {
            SectionsStepicResponse stepicResponse = e.getResponse().body();
            List<Section> sections = stepicResponse.getSections();

            mSectionList.clear();
            mSectionList.addAll(sections);
            mAdapter.notifyDataSetChanged();
            ProgressHelper.dismiss(mProgressBar);
        }
    }

    @Subscribe
    public void onFailureDownload(FailureResponseSectionEvent e) {
        if (mCourse.getCourseId() == e.getCourse().getCourseId()) {
            ProgressHelper.dismiss(mProgressBar);
        }
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
        if (mSectionsRecyclerView != null) mSectionsRecyclerView.setAdapter(null);
        if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }
}
