package org.stepic.droid.ui.fragments;

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

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.core.presenters.LessonPresenter;
import org.stepic.droid.core.presenters.contracts.LessonView;
import org.stepik.android.model.Lesson;
import org.stepik.android.model.Section;
import org.stepik.android.model.Step;
import org.stepik.android.model.Unit;
import org.stepic.droid.ui.adapters.StepFragmentAdapter;
import org.stepic.droid.ui.util.ToolbarHelperKt;
import org.stepic.droid.util.ProgressHelper;
import org.stepik.android.view.fragment_pager.FragmentDelegateScrollStateChangeListener;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.BindView;

public class LessonFragment extends FragmentBase implements LessonView {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_steps, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean keepScreenOnSteps = getUserPreferences().isKeepScreenOnSteps();
        if (keepScreenOnSteps) {
            getAnalytic().reportEvent(Analytic.Steps.SHOW_KEEP_ON_SCREEN);
        } else {
            getAnalytic().reportEvent(Analytic.Steps.SHOW_KEEP_OFF_SCREEN);
        }
        view.setKeepScreenOn(keepScreenOnSteps);
        setHasOptionsMenu(true);

        viewPager.setAdapter(stepAdapter);
        viewPager.addOnPageChangeListener(new FragmentDelegateScrollStateChangeListener(viewPager, stepAdapter));
        stepsPresenter.attachView(this);
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
        stepsPresenter.detachView(this);
        tryAgain.setOnClickListener(null);
        super.onDestroyView();
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
        }
        tabLayout.setVisibility(View.VISIBLE);
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
}
