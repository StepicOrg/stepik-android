package org.stepic.droid.view.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.concurrency.FromDbStepTask;
import org.stepic.droid.concurrency.ToDbStepTask;
import org.stepic.droid.events.steps.FailLoadStepEvent;
import org.stepic.droid.events.steps.FromDbStepEvent;
import org.stepic.droid.events.steps.SuccessLoadStepEvent;
import org.stepic.droid.model.Assignment;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.view.adapters.StepFragmentAdapter;
import org.stepic.droid.web.AssignmentResponse;
import org.stepic.droid.web.StepResponse;
import org.stepic.droid.web.ViewAssignment;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class StepsActivity extends FragmentActivityBase {

    //    public final static String KEY_INDEX_CURRENT_FRAGMENT = "key_index";
    public final static String KEY_COUNT_CURRENT_FRAGMENT = "key_count";


    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.viewpager)
    ViewPager mViewPager;

    @Bind(R.id.tabs)
    TabLayout mTabLayout;

    @Bind(R.id.load_progressbar)
    ProgressBar mProgressBar;

    @BindString(R.string.not_available_lesson)
    String notAvailable;


    StepFragmentAdapter mStepAdapter;
    private List<Step> mStepList;
    private Unit mUnit;
    private Lesson mLesson;
    private boolean isLoaded;


    private ToDbStepTask saveStepsTask;
    private FromDbStepTask getFromDbStepsTask;

    //    private int lastSavedPosition;
    private int mCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps);
        if (savedInstanceState == null) {
//            lastSavedPosition = -1;
            mCount = -1;
        } else {
            mCount = savedInstanceState.getInt(KEY_COUNT_CURRENT_FRAGMENT);
        }
        overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);

        ButterKnife.bind(this);

        mUnit = (Unit) (getIntent().getExtras().get(AppConstants.KEY_UNIT_BUNDLE));
        mLesson = (Lesson) (getIntent().getExtras().get(AppConstants.KEY_LESSON_BUNDLE));

        mShell.getApi().getAssignments(mUnit.getAssignments()).enqueue(new Callback<AssignmentResponse>() {
            @Override
            public void onResponse(Response<AssignmentResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    final List<Assignment> assignments = response.body().getAssignments();
                    Thread thread = new Thread(new Runnable() {

                        @Override
                        public void run() {

                            for (Assignment item : assignments) {
                                mDbManager.addAssignment(item);
                            }
                        }
                    });
                    thread.start();
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });



        mStepList = new ArrayList<>();
        mStepAdapter = new StepFragmentAdapter(getSupportFragmentManager(), this, mStepList, mLesson, mUnit, mCount);
        mViewPager.setAdapter(mStepAdapter);

        setTitle(mLesson.getTitle());
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mStepList.size() <= position) return;
                final Step step = mStepList.get(position);

                if (mStepResolver.isViewiedStatePost(step)) {
                    //try to push viewed state to the server
                    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                        long stepId = step.getId();

                        protected Void doInBackground(Void... params) {
                            long assignmentID = mDbManager.getAssignmentIdByStepId(stepId);
                            mShell.getScreenProvider().pushToViewedQueue(new ViewAssignment(assignmentID, stepId));
                            return null;
                        }
                    };
                    task.execute();

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (mLesson != null && mLesson.getSteps() != null && mLesson.getSteps().length != 0 && !isLoaded)
            updateSteps();
    }


    private void updateSteps() {
        ProgressHelper.activate(mProgressBar);
        getFromDbStepsTask = new FromDbStepTask(mLesson);
        getFromDbStepsTask.execute();
    }

    @Subscribe
    public void onFromDbStepEvent(FromDbStepEvent e) {
        if (e.getLesson() != null && e.getLesson().getId() != mLesson.getId()) {
            bus.post(new FailLoadStepEvent());
            return;
        }

        if (e.getStepList() != null && e.getStepList().size() != 0) {
            showSteps(e.getStepList());
        } else {
            mShell.getApi().getSteps(mLesson.getSteps()).enqueue(new Callback<StepResponse>() {
                @Override
                public void onResponse(Response<StepResponse> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        bus.post(new SuccessLoadStepEvent(response));
                    } else {
                        bus.post(new FailLoadStepEvent());
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    bus.post(new FailLoadStepEvent());
                }
            });
        }
    }

    @Subscribe
    public void onSuccessLoad(SuccessLoadStepEvent e) {
        //// FIXME: 10.10.15 check right lesson ?? is it need?
        StepResponse stepicResponse = e.getResponse().body();
        List<Step> steps = stepicResponse.getSteps();

        if (steps.isEmpty()) {
            bus.post(new FailLoadStepEvent());
        } else {
//            ToDbStepTask task = new ToDbStepTask(mLesson, steps);
//            task.execute();
            showSteps(steps);
        }
    }

//    @Subscribe
//    public void onSuccessSaveToDb(SuccessToDbStepEvent e) {
//        if (e.getmLesson().getId() != mLesson.getId()) return;
//
//        FromDbStepTask stepTask = new FromDbStepTask(mLesson);
//        stepTask.execute();
//    }
//

    private void showSteps(List<Step> steps) {
        mStepList.clear();
        mStepList.addAll(steps);
        mStepAdapter.notifyDataSetChanged();
        updateTabs();
        mTabLayout.setVisibility(View.VISIBLE);
        ProgressHelper.dismiss(mProgressBar);
        isLoaded = true;
//        if (lastSavedPosition >= 0) {
//            mViewPager.setCurrentItem(lastSavedPosition, false);
//        }

    }

    @Subscribe
    public void onFailLoad(FailLoadStepEvent e) {
        Toast.makeText(this, notAvailable, Toast.LENGTH_LONG).show();
        isLoaded = false;
        ProgressHelper.dismiss(mProgressBar);
    }

    private void updateTabs() {
        mTabLayout.setupWithViewPager(mViewPager);
        for (int i = 0; i < mStepAdapter.getCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setIcon(mStepAdapter.getTabDrawable(i));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putInt(KEY_INDEX_CURRENT_FRAGMENT, mViewPager.getCurrentItem());
        outState.putInt(KEY_COUNT_CURRENT_FRAGMENT, mStepList.size());
    }

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
////        if (savedInstanceState != null) {
////            lastSavedPosition = savedInstanceState.getInt(KEY_INDEX_CURRENT_FRAGMENT);
////        }
//    }

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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }
}
