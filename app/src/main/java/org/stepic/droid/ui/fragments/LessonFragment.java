package org.stepic.droid.ui.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.appindexing.builders.Indexables;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.base.Client;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.core.presenters.LessonPresenter;
import org.stepic.droid.core.presenters.StepsTrackingPresenter;
import org.stepic.droid.core.presenters.contracts.LessonTrackingView;
import org.stepic.droid.core.presenters.contracts.LessonView;
import org.stepic.droid.core.updatingstep.contract.UpdatingStepListener;
import org.stepic.droid.storage.operations.Table;
import org.stepik.android.domain.last_step.model.LastStep;
import org.stepik.android.model.Course;
import org.stepik.android.model.Lesson;
import org.stepik.android.model.Section;
import org.stepik.android.model.Step;
import org.stepik.android.model.Unit;
import org.stepic.droid.ui.adapters.StepFragmentAdapter;
import org.stepic.droid.ui.listeners.NextMoveable;
import org.stepic.droid.ui.util.ToolbarHelperKt;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.StringUtil;
import org.stepic.droid.util.resolvers.StepHelper;
import org.stepic.droid.util.resolvers.StepTypeResolver;
import org.stepic.droid.web.ViewAssignment;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.BindView;

// FIXME: 15.08.17 show title R.string.steps_title, when lesson is not loaded
public class LessonFragment extends FragmentBase implements LessonView, LessonTrackingView, NextMoveable, UpdatingStepListener {
    private static final String FROM_PREVIOUS_KEY = "fromPrevKey";
    private static final String SIMPLE_UNIT_ID_KEY = "simpleUnitId";
    private static final String SIMPLE_LESSON_ID_KEY = "simpleLessonId";
    private static final String SIMPLE_STEP_POSITION_KEY = "simpleStepPosition";
    private static final String SIMPLE_DISCUSSION_ID_KEY = "simpleDiscussionPos";
    private boolean fromPreviousLesson;
    private long discussionId = -1;
    private Lesson lesson;
    private Unit unit;
    private Section section;
    private Map<Long, String> stepToTitleMap = new HashMap<>(32);
    private Map<Long, String> stepToUrlMap = new HashMap<>(32);

    private final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            hideSoftKeypad();
            pushState(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public static LessonFragment newInstance(@org.jetbrains.annotations.Nullable Unit unit, Lesson lesson, boolean fromPreviousLesson, Section section) {
        Bundle args = new Bundle();
        args.putParcelable(AppConstants.KEY_UNIT_BUNDLE, unit);
        args.putParcelable(AppConstants.KEY_LESSON_BUNDLE, lesson);
        args.putParcelable(AppConstants.KEY_SECTION_BUNDLE, section);
        args.putBoolean(FROM_PREVIOUS_KEY, fromPreviousLesson);
        LessonFragment fragment = new LessonFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static LessonFragment newInstance(long simpleUnitId, long simpleLessonId, long simpleStepPosition, long discussionSampleId) {
        Bundle args = new Bundle();
        args.putLong(SIMPLE_UNIT_ID_KEY, simpleUnitId);
        args.putLong(SIMPLE_LESSON_ID_KEY, simpleLessonId);
        args.putLong(SIMPLE_STEP_POSITION_KEY, simpleStepPosition);
        args.putLong(SIMPLE_DISCUSSION_ID_KEY, discussionSampleId);
        LessonFragment fragment = new LessonFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private boolean isRestarted = false;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.stepsTabs)
    TabLayout tabLayout;

    @BindView(R.id.loadProgressbarOnEmptyScreen)
    ProgressBar progressBar;

    @BindView(R.id.error)
    View errorView;

    @BindView(R.id.tryAgain)
    View tryAgain;

    @BindView(R.id.corrupted_lesson)
    View corruptedLesson;

    @BindView(R.id.goToCatalog)
    View goToCatalog;

    @BindView(R.id.authAction)
    View authActionView;

    @BindView(R.id.needAuthView)
    View authView;

    @BindString(R.string.connectionProblems)
    String connectionProblemString;

    @BindView(R.id.empty_steps)
    View emptySteps;

    StepFragmentAdapter stepAdapter;

    @Inject
    LessonPresenter stepsPresenter;

    @Inject
    StepTypeResolver stepTypeResolver;

    @Inject
    StepsTrackingPresenter stepTrackingPresenter;

    @Inject
    Client<UpdatingStepListener> updatingStepListenerClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        fromPreviousLesson = getArguments().getBoolean(FROM_PREVIOUS_KEY);
        discussionId = getArguments().getLong(SIMPLE_DISCUSSION_ID_KEY);

        App.Companion
                .component()
                .lessonComponentBuilder()
                .build()
                .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_steps, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            isRestarted = true;
        }

        boolean keepScreenOnSteps = getUserPreferences().isKeepScreenOnSteps();
        if (keepScreenOnSteps) {
            getAnalytic().reportEvent(Analytic.Steps.SHOW_KEEP_ON_SCREEN);
        } else {
            getAnalytic().reportEvent(Analytic.Steps.SHOW_KEEP_OFF_SCREEN);
        }
        view.setKeepScreenOn(keepScreenOnSteps);
        setHasOptionsMenu(true);

        initIndependentUI();
        stepAdapter = new StepFragmentAdapter(getChildFragmentManager(), stepsPresenter.getStepList(), stepTypeResolver);
        viewPager.setAdapter(stepAdapter);
        stepsPresenter.attachView(this);
        stepTrackingPresenter.attachView(this);
        if (lesson == null) {
            Section section = getArguments().getParcelable(AppConstants.KEY_SECTION_BUNDLE);
            Lesson lesson = getArguments().getParcelable(AppConstants.KEY_LESSON_BUNDLE);
            Unit unit = getArguments().getParcelable(AppConstants.KEY_UNIT_BUNDLE);
            long unitId = getArguments().getLong(SIMPLE_UNIT_ID_KEY);
            long defaultStepPos = getArguments().getLong(SIMPLE_STEP_POSITION_KEY);
            long lessonId = getArguments().getLong(SIMPLE_LESSON_ID_KEY);
            stepsPresenter.init(lesson, unit, lessonId, unitId, defaultStepPos, fromPreviousLesson, section);
            fromPreviousLesson = false;
        } else {
            if (stepsPresenter.getStepList().isEmpty()) {
                stepsPresenter.init();
            } else {
                onLessonUnitPrepared(lesson, unit, section);
                showSteps(fromPreviousLesson, -1);
            }
        }
        updatingStepListenerClient.subscribe(this);
    }


    private void initIndependentUI() {
        viewPager.addOnPageChangeListener(pageChangeListener);
        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Section section = getArguments().getParcelable(AppConstants.KEY_SECTION_BUNDLE);
                Lesson lesson = getArguments().getParcelable(AppConstants.KEY_LESSON_BUNDLE);
                Unit unit = getArguments().getParcelable(AppConstants.KEY_UNIT_BUNDLE);
                long unitId = getArguments().getLong(SIMPLE_UNIT_ID_KEY);
                long defaultStepPos = getArguments().getLong(SIMPLE_STEP_POSITION_KEY);
                long lessonId = getArguments().getLong(SIMPLE_LESSON_ID_KEY);
                fromPreviousLesson = getArguments().getBoolean(FROM_PREVIOUS_KEY);
                stepsPresenter.refreshWhenOnConnectionProblem(lesson, unit, lessonId, unitId, defaultStepPos, fromPreviousLesson, section);
                fromPreviousLesson = false;
            }
        });
        authActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAnalytic().reportEvent(Analytic.Interaction.CLICK_AUTH_FROM_STEPS);
                getScreenManager().showLaunchScreen(getActivity());
                getActivity().finish();
            }
        });
        goToCatalog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreenManager().showCatalog(getActivity());
            }
        });
    }

    private void init(Lesson lesson) {
        String title = lesson.getTitle();
        if (title == null || title.isEmpty()) {
            title = getString(R.string.steps_title);
        }
        ToolbarHelperKt.initCenteredToolbar(this, title, true);
    }

    @Override
    public void onDestroyView() {
        updatingStepListenerClient.unsubscribe(this);
        stepsPresenter.detachView(this);
        stepTrackingPresenter.detachView(this);
        if (pageChangeListener != null) {
            viewPager.removeOnPageChangeListener(pageChangeListener);
        }
        tryAgain.setOnClickListener(null);
        super.onDestroyView();
    }

    private void pushState(int position) {
        if (isRestarted) {
            isRestarted = false;
            return;
        }

        reportSelectedPageToGoogle(position);
        boolean isTryToPushFirstStep = position == 0;
        if (isTryToPushFirstStep && fromPreviousLesson && stepsPresenter.getStepList().size() != 1) {
            //if from previous lesson --> not mark as viewed
            return;
        }
        if (stepsPresenter.getStepList().size() <= position) return;
        final Step step = stepsPresenter.getStepList().get(position).getStep();

        if (StepHelper.isViewedStatePost(step) && !step.isCustomPassed()) {
            step.setCustomPassed(true);
            if (position <= tabLayout.getTabCount()) {
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                if (tab != null) {
                    tab.setIcon(stepAdapter.getTabDrawable(position));
                }
            }
        }

        //try to push viewed state to the server
        if (step != null) {
            //track analytic for step opening
            trackStepOpening(step);

            //always push view to server
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                long stepId = step.getId();

                protected Void doInBackground(Void... params) {
                    try {
                        long assignmentID = getDatabaseFacade().getAssignmentIdByStepId(stepId);
                        getScreenManager().pushToViewedQueue(new ViewAssignment(assignmentID, stepId));
                        if (unit != null && unit.getSection() > 0) {
                            Section section = getDatabaseFacade().getSectionById(unit.getSection());
                            if (section != null && section.getCourse() > 0) {
                                Course course = getDatabaseFacade().getCourseById(section.getCourse(), Table.enrolled);
                                if (course != null && course.getLastStepId() != null) {
                                    LastStep lastStep = new LastStep(course.getLastStepId(), unit.getId(), unit.getLesson(), stepId);
                                    getDatabaseFacade().updateLastStep(lastStep);
                                }
                            }
                        }
                    } catch (Exception exception) {
                        getAnalytic().reportError(Analytic.Error.FAIL_PUSH_STEP_VIEW, exception);
                    }
                    return null;
                }
            };
            task.executeOnExecutor(getThreadPoolExecutor());
        }
    }

    private void scrollTabLayoutToPosition(ViewTreeObserver.OnPreDrawListener listener, int finalPosition) {
        int tabWidth = tabLayout.getMeasuredWidth();
        if (tabWidth > 0) {
            tabLayout.getViewTreeObserver().removeOnPreDrawListener(listener);

            int right = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(finalPosition).getRight(); //workaround to get really last element
            if (right >= tabWidth) {
                tabLayout.setScrollX(right);
            }
        }
    }

    private void updateTabState() {
        if (tabLayout.getTabCount() == 0) {
            tabLayout.setupWithViewPager(viewPager);
        }

        for (int i = 0; i < stepAdapter.getCount(); i++) {
            if (i < tabLayout.getTabCount() && i >= 0 && stepAdapter != null) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab != null) {
                    tab.setIcon(stepAdapter.getTabDrawable(i));
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.steps_menu, menu);

        MenuItem comments = menu.findItem(R.id.action_comments);
        if (stepsPresenter.getStepList().isEmpty()) {
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
                if (position < 0 || position >= stepsPresenter.getStepList().size()) {
                    return super.onOptionsItemSelected(item);
                }

                Step step = stepsPresenter.getStepList().get(position).getStep();
                getAnalytic().reportEvent(Analytic.Comments.OPEN_FROM_OPTION_MENU);
                getScreenManager().openComments(getActivity(), step.getDiscussionProxy(), step.getId());
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLessonCorrupted() {
        ProgressHelper.dismiss(progressBar);
        errorView.setVisibility(View.GONE);
        authView.setVisibility(View.GONE);
        emptySteps.setVisibility(View.GONE);
        showViewPager(false);
        corruptedLesson.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLessonUnitPrepared(Lesson lesson, @NonNull Unit unit, Section section) {
        this.lesson = lesson;
        this.section = section;
        this.unit = unit;
        init(lesson);
    }

    @Override
    public void onConnectionProblem() {
        ProgressHelper.dismiss(progressBar);
        corruptedLesson.setVisibility(View.GONE);
        authView.setVisibility(View.GONE);
        emptySteps.setVisibility(View.GONE);
        showViewPager(false);
        if (stepsPresenter.getStepList().isEmpty()) {
            errorView.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getActivity(), connectionProblemString, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void showSteps(boolean fromPreviousLesson, long defaultStepPosition) {
        ProgressHelper.dismiss(progressBar);
        errorView.setVisibility(View.GONE);
        corruptedLesson.setVisibility(View.GONE);
        authView.setVisibility(View.GONE);
        emptySteps.setVisibility(View.GONE);
        showViewPager(true);
        stepAdapter.setDataIfNotNull(lesson, unit, section);
        stepAdapter.notifyDataSetChanged();
        updateTabState();

        int position = -1;

        if (fromPreviousLesson) {
            position = stepsPresenter.getStepList().size() - 1;
        } else {
            position = (int) defaultStepPosition - 1; //default step position is number for steps steps[0] is 1st stepPosition
        }

        if (position > 0 && position < stepsPresenter.getStepList().size()) { //0 is default, if more -> scroll
            viewPager.setCurrentItem(position, false);
            final int finalPosition = position;
            tabLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    scrollTabLayoutToPosition(this, finalPosition);
                    return true;
                }
            });
        } else {
            pushState(viewPager.getCurrentItem());
        }
        tabLayout.setVisibility(View.VISIBLE);

        if (discussionId > 0 && position >= 0 && position < stepsPresenter.getStepList().size()) {
            Step step = stepsPresenter.getStepList().get(position).getStep();
            getScreenManager().openComments(getActivity(), step.getDiscussionProxy(), step.getId());
            discussionId = -1;
        }
    }

    @Override
    public void onEmptySteps() {
        ProgressHelper.dismiss(progressBar);
        errorView.setVisibility(View.GONE);
        corruptedLesson.setVisibility(View.GONE);
        authView.setVisibility(View.GONE);
        emptySteps.setVisibility(View.VISIBLE);
        showViewPager(false);
    }

    @Override
    public void onLoading() {
        if (stepsPresenter.getStepList().isEmpty()) {
            ProgressHelper.activate(progressBar);
        }
        errorView.setVisibility(View.GONE);
        corruptedLesson.setVisibility(View.GONE);
        authView.setVisibility(View.GONE);
        emptySteps.setVisibility(View.GONE);
        showViewPager(false);
    }

    @Override
    public void onUserNotAuth() {
        ProgressHelper.dismiss(progressBar);
        errorView.setVisibility(View.GONE);
        corruptedLesson.setVisibility(View.GONE);
        emptySteps.setVisibility(View.GONE);
        authView.setVisibility(View.VISIBLE);
        showViewPager(false);
    }

    void showViewPager(boolean needShow) {
        if (needShow) {
            viewPager.setVisibility(View.VISIBLE);
//            getActivity().getWindow().setBackgroundDrawable(null); // it may produce some bugs
        } else {
//            getActivity().getWindow().setBackgroundDrawableResource(R.color.windowBackground); //it may produce some bugs
            viewPager.setVisibility(View.INVISIBLE);
        }
    }


    private void trackStepOpening(@NonNull Step step) {
        stepTrackingPresenter.trackStepType(step);
    }

    @Override
    public boolean moveNext() {
        if (viewPager == null || viewPager.getAdapter() == null) {
            return false;
        }

        int currentItem = viewPager.getCurrentItem();
        int lastIndex = viewPager.getAdapter().getCount() - 1;
        if (currentItem < lastIndex) {
            viewPager.setCurrentItem(currentItem + 1, true);
            return true;
        } else if (currentItem == lastIndex) {
            return false;
        }

        return false;
    }


    /*
     * App indexing stuff begin
     */


    private int previousGoogleIndexedPosition = -1;

    private void reportSelectedPageToGoogle(int position) {
        if (position == previousGoogleIndexedPosition) {
            // do not index twice
            return;
        }

        int stepListSize = stepsPresenter.getStepList().size();
        if (previousGoogleIndexedPosition >= 0 && previousGoogleIndexedPosition < stepListSize) {
            stopIndexStep(stepsPresenter.getStepList().get(previousGoogleIndexedPosition).getStep());
        }
        if (position >= 0 && position < stepListSize) {
            indexStep(stepsPresenter.getStepList().get(position).getStep());
        }
        previousGoogleIndexedPosition = position;
    }

    private void indexStep(@NotNull Step step) {
        FirebaseAppIndex.getInstance().update(getIndexable(step));
        FirebaseUserActions.getInstance().start(getAction(step));
    }

    private void stopIndexStep(@NotNull Step step) {
        FirebaseUserActions.getInstance().end(getAction(step));
    }

    private Indexable getIndexable(Step step) {
        String urlInWeb = getUrlInWeb(step);
        String title = getTitle(step);
        getAnalytic().reportEventWithIdName(Analytic.AppIndexing.STEP, urlInWeb, title);
        return Indexables.newSimple(title, urlInWeb);
    }

    public Action getAction(@NotNull Step step) {
        return Actions.newView(getTitle(step), getUrlInWeb(step));
    }

    @NotNull
    private String getTitle(Step step) {
        String stepTitle = stepToTitleMap.get(step.getId());
        if (stepTitle == null) {
            stepTitle = StringUtil.getTitleForStep(getContext(), lesson, step.getPosition());
            stepToTitleMap.put(step.getId(), stepTitle);
        }
        return stepTitle;
    }

    @NotNull
    private String getUrlInWeb(Step step) {
        String stepUrl = stepToUrlMap.get(step.getId());
        if (stepUrl == null) {
            stepUrl = StringUtil.getUriForStep(getConfig().getBaseUrl(), lesson, unit, step);
            stepToUrlMap.put(step.getId(), stepUrl);
        }
        return stepUrl;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (viewPager != null) {
            //when user press back from comments
            int selectedPosition = viewPager.getCurrentItem();
            if (selectedPosition >= 0 && selectedPosition < stepsPresenter.getStepList().size()) {
                indexStep(stepsPresenter.getStepList().get(selectedPosition).getStep());
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (viewPager != null) {
            int selectedPosition = viewPager.getCurrentItem();
            if (selectedPosition >= 0 && selectedPosition < stepsPresenter.getStepList().size()) {
                stopIndexStep(stepsPresenter.getStepList().get(selectedPosition).getStep());
            }
        }
        stepToTitleMap.clear();
        stepToUrlMap.clear();
    }

    /*
     * App indexing stuff end
     */


    @Override
    public void onNeedUpdate(long stepId, boolean isSuccessAttempt) {
        Step step = null;
        int position = -1;
        for (int i = 0; i < stepsPresenter.getStepList().size(); i++) {
            Step stepInList = stepsPresenter.getStepList().get(i).getStep();
            if (stepInList.getId() == stepId) {
                position = i;
                step = stepInList;
                break;
            }
        }

        if (step != null && !step.isCustomPassed() && (StepHelper.isViewedStatePost(step) || isSuccessAttempt)) {
            // if not passed yet
            step.setCustomPassed(true);
            if (position >= 0 && position < tabLayout.getTabCount()) {
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                if (tab != null) {
                    tab.setIcon(stepAdapter.getTabDrawable(position));
                }
            }
        }
    }

}
