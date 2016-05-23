package org.stepic.droid.view.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.concurrency.tasks.FromDbStepTask;
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
import org.stepic.droid.model.VideoUrl;
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

public class StepsFragment extends FragmentBase {
    private static final String TAG = "StepsFragment";

    public static StepsFragment newInstance(Unit unit, Lesson lesson) {

        Bundle args = new Bundle();
        args.putParcelable(AppConstants.KEY_UNIT_BUNDLE, unit);
        args.putParcelable(AppConstants.KEY_LESSON_BUNDLE, lesson);
        StepsFragment fragment = new StepsFragment();
        fragment.setArguments(args);
        return fragment;
    }


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
    private String qualityForView;
    private FromDbStepTask getFromDbStepsTask;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_steps, container, false);
        setHasOptionsMenu(true);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        getActivity().overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);

        mUnit = getArguments().getParcelable(AppConstants.KEY_UNIT_BUNDLE);
        mLesson = getArguments().getParcelable(AppConstants.KEY_LESSON_BUNDLE);

        mStepList = new ArrayList<>();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mStepAdapter = new StepFragmentAdapter(getActivity().getSupportFragmentManager(), mStepList, mLesson, mUnit);
        mViewPager.setAdapter(mStepAdapter);

        getActivity().setTitle(mLesson.getTitle());
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
    }

    @Override
    public void onStart() {
        super.onStart();
        bus.register(this);
        //isLoaded is retained and stepList too, but this method should be in onStart due to user can rotate device, when
        //loading is not finished. it can produce many requests, but it will be happen when user rotates device many times per second.
        if (mLesson != null && mLesson.getSteps() != null && mLesson.getSteps().length != 0 && !isLoaded) {
            updateSteps();
        } else {
            ArrayList<Step> newList = new ArrayList<>(mStepList);
            showSteps(newList);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        bus.unregister(this);
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
                    CachedVideo video = mDatabaseFacade.getCachedVideoById(step.getBlock().getVideo().getId());
                    String quality;
                    if (video == null) {
                        String resultQuality = mUserPreferences.getQualityVideo();
                        try {

                            int weWant = Integer.parseInt(mUserPreferences.getQualityVideo());
                            final List<VideoUrl> urls = step.getBlock().getVideo().getUrls();
                            int bestDelta = Integer.MAX_VALUE;
                            int bestIndex = 0;
                            for (int i = 0; i < urls.size(); i++) {
                                int current = Integer.parseInt(urls.get(i).getQuality());
                                int delta = Math.abs(current - weWant);
                                if (delta < bestDelta) {
                                    bestDelta = delta;
                                    bestIndex = i;
                                }

                            }
                            resultQuality = urls.get(bestIndex).getQuality();
                        } catch (NumberFormatException e) {
                            resultQuality = mUserPreferences.getQualityVideo();
                        }


                        quality = resultQuality;
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
                    long assignmentID = mDatabaseFacade.getAssignmentIdByStepId(stepId);

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
        getFromDbStepsTask.executeOnExecutor(mThreadPoolExecutor);
    }

    @Subscribe
    public void onFromDbStepEvent(FromDbStepEvent e) {
        if (e.getLesson() == null || e.getLesson().getId() != mLesson.getId()) {
            return;
        }

        if (e.getStepList() != null && !e.getStepList().isEmpty() && e.getStepList().size() == mLesson.getSteps().length) {

            final List<Step> stepsFromDB = e.getStepList();
            mShell.getApi().getSteps(mLesson.getSteps()).enqueue(new Callback<StepResponse>() {
                @Override
                public void onResponse(Response<StepResponse> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        bus.post(new SuccessLoadStepEvent(response.body().getSteps()));//update if we can
                    } else {
                        bus.post(new SuccessLoadStepEvent(stepsFromDB)); //if fail -> get from db
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    bus.post(new SuccessLoadStepEvent(stepsFromDB));//if fail -> get from db
                }
            });
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
//            ToDbStepTask task = new ToDbStepTask(lesson, steps);
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
                                    mDatabaseFacade.addAssignment(item);
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
                                                mDatabaseFacade.addProgress(item);
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
                    item.set_custom_passed(mDatabaseFacade.isStepPassed(item.getId()));
                    mDatabaseFacade.addStep(item); // FIXME: 26.01.16 WARNING, this line is dangerous
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (mStepList != null && mStepAdapter != null && mTabLayout != null) {
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
            step.set_custom_passed(true);
            int pos = mViewPager.getCurrentItem();

            for (int i = 0; i < mTabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = mTabLayout.getTabAt(i);
                tab.setIcon(mStepAdapter.getTabDrawable(i));
            }
        }
    }

    private void showSteps(List<Step> steps) {
        mStepList.clear();
        mStepList.addAll(steps);

        mStepAdapter.notifyDataSetChanged();
        updateTabs();
        mTabLayout.setVisibility(View.VISIBLE);
        ProgressHelper.dismiss(mProgressBar);
        isLoaded = true;
        pushState(mViewPager.getCurrentItem());
        checkOptionsMenu(mViewPager.getCurrentItem());

    }

    @Subscribe
    public void onFailLoad(FailLoadStepEvent e) {
        Toast.makeText(getActivity(), notAvailable, Toast.LENGTH_LONG).show();
        isLoaded = false;
        ProgressHelper.dismiss(mProgressBar);
    }

    private void updateTabs() {
        if (mTabLayout.getTabCount() == 0) {
            mTabLayout.setupWithViewPager(mViewPager);
        }

        for (int i = 0; i < mStepAdapter.getCount(); i++) {
            if (i < mTabLayout.getTabCount() && i >= 0 && mStepAdapter != null) {
                TabLayout.Tab tab = mTabLayout.getTabAt(i);
                if (tab != null) {
                    tab.setIcon(mStepAdapter.getTabDrawable(i));
                }
            }
        }
    }

    @Subscribe
    public void onQualityDetermined(VideoQualityEvent e) {
        int currentPosition = mViewPager.getCurrentItem();
        if (currentPosition < 0 || currentPosition >= mStepList.size()) return;
        long stepId = mStepList.get(currentPosition).getId();
        if (e.getStepId() != stepId) return;
        qualityForView = e.getQuality();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.video_step_menu, menu);
        MenuItem quality = menu.findItem(R.id.action_quality);
        if (qualityForView != null) {
            quality.setTitle(qualityForView);
        } else {
            quality.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_comments:
                int position = mViewPager.getCurrentItem();
                Step step = mStepList.get(position);
                mShell.getScreenProvider().openComments(getContext(), step.getDiscussion_proxy());
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
}
