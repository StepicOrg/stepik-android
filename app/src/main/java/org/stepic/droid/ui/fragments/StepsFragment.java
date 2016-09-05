package org.stepic.droid.ui.fragments;

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
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.concurrency.tasks.FromDbStepTask;
import org.stepic.droid.core.StepModule;
import org.stepic.droid.core.presenters.StepsPresenter;
import org.stepic.droid.core.presenters.contracts.StepsView;
import org.stepic.droid.events.steps.UpdateStepEvent;
import org.stepic.droid.events.video.VideoQualityEvent;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.model.VideoUrl;
import org.stepic.droid.ui.adapters.StepFragmentAdapter;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.web.ViewAssignment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StepsFragment extends FragmentBase implements StepsView {
    private static final String TAG = "StepsFragment";
    private static final String FROM_PREVIOUS_KEY = "fromPrevKey";
    private static final String SIMPLE_UNIT_ID_KEY = "simpleUnitId";
    private static final String SIMPLE_LESSON_ID_KEY = "simpleLessonId";
    private static final String SIMPLE_STEP_POSITION_KEY = "simpleStepPosition";

    public static StepsFragment newInstance(@org.jetbrains.annotations.Nullable Unit unit, Lesson lesson, boolean fromPreviousLesson) {
        Bundle args = new Bundle();
        args.putParcelable(AppConstants.KEY_UNIT_BUNDLE, unit);
        args.putParcelable(AppConstants.KEY_LESSON_BUNDLE, lesson);
        args.putBoolean(FROM_PREVIOUS_KEY, fromPreviousLesson);
        StepsFragment fragment = new StepsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public static StepsFragment newInstance(long simpleUnitId, long simpleLessonId, long simpleStepPosition) {
        Bundle args = new Bundle();
        args.putLong(SIMPLE_UNIT_ID_KEY, simpleUnitId);
        args.putLong(SIMPLE_LESSON_ID_KEY, simpleLessonId);
        args.putLong(SIMPLE_STEP_POSITION_KEY, simpleStepPosition);
        StepsFragment fragment = new StepsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.load_progressbar)
    ProgressBar progressBar;

    @BindString(R.string.not_available_lesson)
    String notAvailableLessonString;

    StepFragmentAdapter stepAdapter;
    private List<Step> stepList;

    private boolean isLoaded;
    private String qualityForView;
    private FromDbStepTask getFromDbStepsTask;

    private boolean fromPreviousLesson = false;

    @Inject
    StepsPresenter stepsPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        MainApplication
                .component()
                .plus(new StepModule())
                .inject(this);

        fromPreviousLesson = getArguments().getBoolean(FROM_PREVIOUS_KEY);
        stepList = new ArrayList<>();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_steps, container, false);
        setHasOptionsMenu(true);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initIndependentUI();
        stepsPresenter.attachView(this);
        if (stepsPresenter.getLesson() != null) {
            Lesson lesson = getArguments().getParcelable(AppConstants.KEY_LESSON_BUNDLE);
            Unit unit = getArguments().getParcelable(AppConstants.KEY_UNIT_BUNDLE);
            long unitId = getArguments().getLong(SIMPLE_UNIT_ID_KEY);
            long defaultStepPos = getArguments().getLong(SIMPLE_STEP_POSITION_KEY);
            long lessonId = getArguments().getLong(SIMPLE_LESSON_ID_KEY);
            stepsPresenter.init(lesson, unit, unitId, defaultStepPos, lessonId);
        }
        bus.register(this);
        //isLoaded is retained and stepList too, but this method should be in attachView due to user can rotate device, when
        //loading is not finished. it can produce many requests, but it will be happen when user rotates device many times per second.
//        if (lesson != null && lesson.getSteps() != null && lesson.getSteps().length != 0 && !isLoaded) {
//            updateSteps();
//        } else {
//            ArrayList<Step> newList = new ArrayList<>(stepList);
//            showSteps(newList);
//        }
    }

    @Override
    public void onLessonCorrupted() {
        // FIXME: 05.09.16 show placeholder
        Toast.makeText(getContext(), "Sorry, link was broken", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLessonUnitPrepared(Lesson lesson, @org.jetbrains.annotations.Nullable Unit unit) {
        init(lesson, unit);
    }

    private void initIndependentUI() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                hideSoftKeypad();
                pushState(position);
                checkOptionsMenu(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void init(Lesson lesson, Unit unit) {
        stepAdapter = new StepFragmentAdapter(getActivity().getSupportFragmentManager(), stepList, lesson, unit);
        viewPager.setAdapter(stepAdapter);

        getActivity().setTitle(lesson.getTitle());
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onDestroyView() {
        bus.unregister(this);
        stepsPresenter.detachView(this);
        super.onDestroyView();
    }

    private void checkOptionsMenu(int position) {
        if (stepList.size() <= position) return;
        final Step step = stepList.get(position);

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
            task.executeOnExecutor(mThreadPoolExecutor);
        }
    }

    private void pushState(int position) {
        if (stepList.size() <= position) return;
        final Step step = stepList.get(position);

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

//    private void updateSteps() {
//        ProgressHelper.activate(progressBar);
//        getFromDbStepsTask = new FromDbStepTask(lesson);
//        getFromDbStepsTask.executeOnExecutor(mThreadPoolExecutor);
//    }

//    @Subscribe
//    public void onFromDbStepEvent(FromDbStepEvent e) {
//        if (e.getLesson() == null || e.getLesson().getId() != lesson.getId()) {
//            return;
//        }
//
//        if (e.getStepList() != null && !e.getStepList().isEmpty() && lesson.getSteps() != null && e.getStepList().size() == lesson.getSteps().length) {
//
//            final List<Step> stepsFromDB = e.getStepList();
//            showSteps(stepsFromDB);
//            mShell.getApi().getSteps(lesson.getSteps()).enqueue(new Callback<StepResponse>() {
//                @Override
//                public void onResponse(Response<StepResponse> response, Retrofit retrofit) {
//                    if (response.isSuccess()) {
//                        bus.post(new SuccessLoadStepEvent(response.body().getSteps()));//update if we can
//                    }
//                }
//
//                @Override
//                public void onFailure(Throwable t) {
//                    //already show cached
//                }
//            });
//        } else {
//            mShell.getApi().getSteps(lesson.getSteps()).enqueue(new Callback<StepResponse>() {
//                @Override
//                public void onResponse(Response<StepResponse> response, Retrofit retrofit) {
//                    if (response.isSuccess()) {
//                        bus.post(new SuccessLoadStepEvent(response.body().getSteps()));
//                    } else {
//                        bus.post(new FailLoadStepEvent());
//                    }
//                }
//
//                @Override
//                public void onFailure(Throwable t) {
//                    bus.post(new FailLoadStepEvent());
//                }
//            });
//        }
//    }

//    @Subscribe
//    public void onSuccessLoad(SuccessLoadStepEvent e) {
//        //// FIXME: 10.10.15 check right lesson ?? is it need?
//        final List<Step> steps = e.getSteps();
//
//        if (steps.isEmpty()) {
//            bus.post(new FailLoadStepEvent());
//        } else {
//            mShell.getApi().getAssignments(unit.getAssignments()).enqueue(new Callback<AssignmentResponse>() {
//                @Override
//                public void onResponse(Response<AssignmentResponse> response, Retrofit retrofit) {
//                    if (response.isSuccess()) {
//                        final List<Assignment> assignments = response.body().getAssignments();
//                        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
//                            @Override
//                            protected Void doInBackground(Void... params) {
//                                for (Assignment item : assignments) {
//                                    mDatabaseFacade.addAssignment(item);
//                                }
//                                return null;
//                            }
//                        };
//                        task.executeOnExecutor(mThreadPoolExecutor);
//
//                        final String[] progressIds = ProgressUtil.getAllProgresses(assignments);
//                        mShell.getApi().getProgresses(progressIds).enqueue(new Callback<ProgressesResponse>() {
//                            Unit localUnit = unit;
//
//                            @Override
//                            public void onResponse(final Response<ProgressesResponse> response, Retrofit retrofit) {
//                                if (response.isSuccess()) {
//                                    AsyncTask<Void, Void, Void> task1 = new AsyncTask<Void, Void, Void>() {
//                                        List<Progress> progresses;
//
//                                        @Override
//                                        protected Void doInBackground(Void... params) {
//                                            progresses = response.body().getProgresses();
//                                            for (Progress item : progresses) {
//                                                mDatabaseFacade.addProgress(item);
//                                            }
//                                            return null;
//                                        }
//
//                                        @Override
//                                        protected void onPostExecute(Void aVoid) {
//                                            super.onPostExecute(aVoid);
//                                            bus.post(new UpdateStepsState(localUnit, steps));
//                                        }
//                                    };
//                                    task1.executeOnExecutor(mThreadPoolExecutor);
//                                }
//                            }
//
//                            @Override
//                            public void onFailure(Throwable t) {
//                                if (steps != null && unit != null)
//                                    bus.post(new UpdateStepsState(unit, steps));
//                            }
//                        });
//
//                    }
//                }
//
//                @Override
//                public void onFailure(Throwable t) {
//                    if (steps != null && unit != null)
//                        bus.post(new UpdateStepsState(unit, steps));
//                }
//            });
//
//
//        }
//    }
//
//    @Subscribe
//    public void onUpdateStepsState(final UpdateStepsState e) {
//        if (e.getUnit().getId() != unit.getId()) return;
//
//        final List<Step> localSteps = e.getSteps();
//
//        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                for (Step item : localSteps) {
//                    item.set_custom_passed(mDatabaseFacade.isStepPassed(item.getId()));
//                    mDatabaseFacade.addStep(item); // FIXME: 26.01.16 WARNING, this line is dangerous
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//                if (stepList != null && stepAdapter != null && tabLayout != null) {
//                    showSteps(localSteps);
//                }
//            }
//        };
//        task.executeOnExecutor(mThreadPoolExecutor);
//    }

    @Subscribe
    public void onUpdateOneStep(UpdateStepEvent e) {
        long stepId = e.getStepId();
        Step step = null;
        if (stepList != null) {
            for (Step item : stepList) {
                if (item.getId() == stepId) {
                    step = item;
                    break;
                }
            }
        }

        if (step != null) {
            step.set_custom_passed(true);
            int pos = viewPager.getCurrentItem();

            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                tab.setIcon(stepAdapter.getTabDrawable(i));
            }
        }
    }

//    private void showSteps(List<Step> steps) {
//        boolean isNumEquals = stepList.isEmpty() || steps.size() == stepList.size(); // hack for need updating view?
//        stepList.clear();
//        stepList.addAll(steps);
//
//        if (!isLoaded) {
//            stepAdapter.notifyDataSetChanged();
//        }
//        updateTabs();
//        if (fromPreviousLesson && !isLoaded) {
//            viewPager.setCurrentItem(stepList.size() - 1, false);
////            int tabLayoutWidth = tabLayout.getMeasuredWidth();
//            tabLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//                @Override
//                public boolean onPreDraw() {
//                    scrollTabLayoutToEnd(this);
//                    return true;
//                }
//            });
////            tabLayout.setScrollX(tabLayoutWidth);
//            fromPreviousLesson = false;
//        }
//        isLoaded = true;
//        tabLayout.setVisibility(View.VISIBLE);
//        ProgressHelper.dismiss(progressBar);
//        pushState(viewPager.getCurrentItem());
//        checkOptionsMenu(viewPager.getCurrentItem());
//
//
//        if (isLoaded && !isNumEquals) {
//            //it is working only if teacher add steps in lesson and user has not cached new steps, but cached old.
//            stepAdapter.notifyDataSetChanged();
//            updateTabs();
//        }
//    }
//
//    private void scrollTabLayoutToEnd(ViewTreeObserver.OnPreDrawListener listener) {
//        int tabWidth = tabLayout.getMeasuredWidth();
//        if (tabWidth > 0) {
//            tabLayout.getViewTreeObserver().removeOnPreDrawListener(listener);
//
//            int tabCount = tabLayout.getTabCount();
//            int right = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(tabCount - 1).getRight(); //workaround to get really last element
//            if (right >= tabWidth) {
//                tabLayout.setScrollX(right);
//            }
//        }
//    }

//    @Subscribe
//    public void onFailLoad(FailLoadStepEvent e) {
//        Toast.makeText(getActivity(), notAvailableLessonString, Toast.LENGTH_LONG).show();
//        isLoaded = false;
//        ProgressHelper.dismiss(progressBar);
//    }
//
//    private void updateTabs() {
//        if (tabLayout.getTabCount() == 0) {
//            tabLayout.setupWithViewPager(viewPager);
//        }
//
//        for (int i = 0; i < stepAdapter.getCount(); i++) {
//            if (i < tabLayout.getTabCount() && i >= 0 && stepAdapter != null) {
//                TabLayout.Tab tab = tabLayout.getTabAt(i);
//                if (tab != null) {
//                    tab.setIcon(stepAdapter.getTabDrawable(i));
//                }
//            }
//        }
//    }

    @Subscribe
    public void onQualityDetermined(VideoQualityEvent e) {
        int currentPosition = viewPager.getCurrentItem();
        if (currentPosition < 0 || currentPosition >= stepList.size()) return;
        long stepId = stepList.get(currentPosition).getId();
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

        MenuItem comments = menu.findItem(R.id.action_comments);
        if (stepList.isEmpty()) {
            comments.setVisible(false);
        } else {
            comments.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_comments:
                int position = viewPager.getCurrentItem();
                if (position < 0 || position >= stepList.size()) {
                    return super.onOptionsItemSelected(item);
                }

                Step step = stepList.get(position);
                mShell.getScreenProvider().openComments(getContext(), step.getDiscussion_proxy(), step.getId());
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
}
