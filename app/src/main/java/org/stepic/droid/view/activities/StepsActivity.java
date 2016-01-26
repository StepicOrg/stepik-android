package org.stepic.droid.view.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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
import org.stepic.droid.events.steps.UpdateStepEvent;
import org.stepic.droid.events.steps.UpdateStepsState;
import org.stepic.droid.events.video.VideoQualityEvent;
import org.stepic.droid.model.Assignment;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.ProgressUtil;
import org.stepic.droid.view.adapters.StepFragmentAdapter;
import org.stepic.droid.web.AssignmentResponse;
import org.stepic.droid.web.ProgressesResponse;
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
    public final static String KEY_INDEX_CURRENT_FRAGMENT = "key_current";


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


    private volatile boolean isAssignmentsUpdated = false;
    private volatile boolean isProgressUpdated = false;


    private ToDbStepTask saveStepsTask;
    private FromDbStepTask getFromDbStepsTask;

    //    private int lastSavedPosition;
    private int mCount;
    private int mCurrent;

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
        if (savedInstanceState == null) {
//            lastSavedPosition = -1;
            mCurrent = -1;
        } else {
            mCurrent = savedInstanceState.getInt(KEY_INDEX_CURRENT_FRAGMENT);
        }

        overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);

        ButterKnife.bind(this);

        mUnit = (Unit) (getIntent().getExtras().get(AppConstants.KEY_UNIT_BUNDLE));
        mLesson = (Lesson) (getIntent().getExtras().get(AppConstants.KEY_LESSON_BUNDLE));


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
                pushState(position);
                checkOptionsMenu(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (mLesson != null && mLesson.getSteps() != null && mLesson.getSteps().length != 0 && !isLoaded)
            updateSteps();
    }

    private void checkOptionsMenu(int position) {
        if (mStepList.size() <= position) return;
        final Step step = mStepList.get(position);

        if (step.getBlock() == null || step.getBlock().getVideo() == null) {
            bus.post(new VideoQualityEvent(null, step.getId()));
        } else {
            AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    CachedVideo video = mDbManager.getCachedVideoById(step.getBlock().getVideo().getId());
                    String quality;
                    if (video == null) {
                        quality = mUserPreferences.getQualityVideo();
                    } else {
                        quality = video.getQuality();
                    }
                    return quality;
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    if (s == null) {
                        bus.post(new VideoQualityEvent(s, step.getId()));
                    } else {
                        bus.post(new VideoQualityEvent(s + "p", step.getId()));
                    }
                }
            };
            task.execute();
        }


    }

    private void pushState(int position) {
        if (mStepList.size() <= position) return;
        final Step step = mStepList.get(position);

        final int local = position;
        if (mStepResolver.isViewedStatePost(step) && !step.is_custom_passed()) {
            //try to push viewed state to the server
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                long stepId = step.getId();

                protected Void doInBackground(Void... params) {
                    long assignmentID = mDbManager.getAssignmentIdByStepId(stepId);

                    Log.i("push", "push " + local);
                    mShell.getScreenProvider().pushToViewedQueue(new ViewAssignment(assignmentID, stepId));
                    return null;
                }
            };
            task.execute();

        }
    }


    private void updateSteps() {
        ProgressHelper.activate(mProgressBar);
        getFromDbStepsTask = new FromDbStepTask(mLesson);
        getFromDbStepsTask.execute();
    }

    @Subscribe
    public void onFromDbStepEvent(FromDbStepEvent e) {
        if (e.getLesson() == null || e.getLesson().getId() != mLesson.getId()) {
            return;
        }

        if (e.getStepList() != null && e.getStepList().size() != 0 && e.getStepList().size() == mLesson.getSteps().length) {
            bus.post(new SuccessLoadStepEvent(e.getStepList()));
        } else {
            mShell.getApi().getSteps(mLesson.getSteps()).enqueue(new Callback<StepResponse>() {
                @Override
                public void onResponse(Response<StepResponse> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        bus.post(new SuccessLoadStepEvent(response.body().getSteps()));
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
        final List<Step> steps = e.getSteps();

        if (steps.isEmpty()) {
            bus.post(new FailLoadStepEvent());
        } else {
//            ToDbStepTask task = new ToDbStepTask(mLesson, steps);
//            task.execute();

            mShell.getApi().getAssignments(mUnit.getAssignments()).enqueue(new Callback<AssignmentResponse>() {
                @Override
                public void onResponse(Response<AssignmentResponse> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        final List<Assignment> assignments = response.body().getAssignments();
                        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                for (Assignment item : assignments) {
                                    mDbManager.addAssignment(item);
                                }
                                return null;
                            }
                        };
                        task.execute();

                        final String[] progressIds = ProgressUtil.getAllProgresses(assignments);
                        mShell.getApi().getProgresses(progressIds).enqueue(new Callback<ProgressesResponse>() {
                            Unit localUnit = mUnit;

                            @Override
                            public void onResponse(final Response<ProgressesResponse> response, Retrofit retrofit) {
                                if (response.isSuccess()) {
                                    AsyncTask<Void, Void, Void> task1 = new AsyncTask<Void, Void, Void>() {
                                        List<Progress> progresses;

                                        @Override
                                        protected Void doInBackground(Void... params) {
                                            progresses = response.body().getProgresses();
                                            for (Progress item : progresses) {
                                                mDbManager.addProgress(item);
                                            }
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Void aVoid) {
                                            super.onPostExecute(aVoid);
                                            bus.post(new UpdateStepsState(localUnit, steps));
                                        }
                                    };
                                    task1.execute();
                                }
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                if (steps != null && mUnit != null)
                                    bus.post(new UpdateStepsState(mUnit, steps));
                            }
                        });

                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    if (steps != null && mUnit != null)
                        bus.post(new UpdateStepsState(mUnit, steps));
                }
            });


        }
    }


    @Subscribe
    public void onUpdateStepsState(final UpdateStepsState e) {
        if (e.getUnit().getId() != mUnit.getId()) return;

        final List<Step> localSteps = e.getSteps();

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for (Step item : localSteps) {
                    item.setIs_custom_passed(mDbManager.isStepPassed(item.getId()));
                    mDbManager.addStep(item); // FIXME: 26.01.16 WARNING, this line is dangerous
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (mStepList != null && mStepAdapter != null && mTabLayout != null) {
                    Log.i("update", "update ui");
                    showSteps(localSteps);
                }
            }
        };
        task.execute();
    }

    @Subscribe
    public void onUpdateOneStep(UpdateStepEvent e) {
        long stepId = e.getStepId();
        Step step = null;
        if (mStepList != null) {
            for (Step item : mStepList) {
                if (item.getId() == stepId) {
                    step = item;
                    break;
                }
            }
        }

        if (step != null) {
            step.setIs_custom_passed(true);
            int pos = mViewPager.getCurrentItem();

            for (int i = 0; i < mTabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = mTabLayout.getTabAt(i);
                tab.setIcon(mStepAdapter.getTabDrawable(i));
            }
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
        if (mCurrent >= 0) {
            mViewPager.setCurrentItem(mCurrent, false);
//            TabLayout.Tab tab = mTabLayout.getTabAt(mCurrent);
//            tab.select();
        }
        isLoaded = true;
        pushState(mViewPager.getCurrentItem());
        checkOptionsMenu(mViewPager.getCurrentItem());

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
        outState.putInt(KEY_INDEX_CURRENT_FRAGMENT, mViewPager.getCurrentItem());
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

    @Override
    protected void onStart() {
        super.onStart();
        bus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        bus.unregister(this);
    }

    private String qualityForView;

    @Subscribe
    public void onQualityDetermined(VideoQualityEvent e) {
        int currentPosition = mViewPager.getCurrentItem();
        if (currentPosition < 0 || currentPosition >= mStepList.size()) return;
        long stepId = mStepList.get(currentPosition).getId();
        if (e.getStepId() != stepId) return;
        qualityForView = e.getQuality();
        invalidateOptionsMenu();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.video_step_menu, menu);
        MenuItem quality = menu.findItem(R.id.action_quality);
        if (qualityForView != null) {
            quality.setTitle(qualityForView);
        } else {
            quality.setTitle("");

        }
        return true;
    }

}
