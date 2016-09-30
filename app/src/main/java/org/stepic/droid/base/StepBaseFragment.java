package org.stepic.droid.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.core.modules.StepModule;
import org.stepic.droid.core.presenters.RouteStepPresenter;
import org.stepic.droid.core.presenters.contracts.RouteStepView;
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout;
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment;
import org.stepic.droid.ui.dialogs.StepShareDialogFragment;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.web.StepResponse;

import javax.inject.Inject;

import butterknife.BindView;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public abstract class StepBaseFragment extends FragmentBase implements RouteStepView {

    @BindView(R.id.text_header_enhanced)
    protected LatexSupportableEnhancedFrameLayout headerWvEnhanced;

    @BindView(R.id.open_comments_root)
    protected View openCommentViewClickable;

    @BindView(R.id.open_comments_text)
    protected TextView textForComment;

    /**
     * default: Gone
     */
    @BindView(R.id.route_lesson_root)
    protected View routeLessonRoot;

    /**
     * do not make it gone, only invisible. Default: invisible
     */
    @BindView(R.id.next_lesson_view)
    protected View nextLessonView;

    /**
     * do not make it gone, only invisible. Default: invisible
     */
    @BindView(R.id.previous_lesson_view)
    protected View previousLessonView;

    protected Step step;
    protected Lesson lesson;
    protected Section section;

    @Nullable
    protected Unit unit;

    private final static String LOAD_DIALOG_TAG = "stepBaseFragmentLoad";

    private final static String ROUTER_ROOT_VISIBILITY_KEY = "visibility_router_root";
    private final static String NEXT_LESSON_VISIBILITY_KEY = "visibility_next_lesson";
    private final static String PREVIOUS_LESSON_VISIBILITY_KEY = "visibility_previous_lesson";


    @Inject
    RouteStepPresenter routeStepPresenter;

    @Override
    protected void injectComponent() {
        MainApplication.component().plus(new StepModule()).inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        step = getArguments().getParcelable(AppConstants.KEY_STEP_BUNDLE);
        lesson = getArguments().getParcelable(AppConstants.KEY_LESSON_BUNDLE);
        unit = getArguments().getParcelable(AppConstants.KEY_UNIT_BUNDLE);
        section = getArguments().getParcelable(AppConstants.KEY_SECTION_BUNDLE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        if (step != null &&
                step.getBlock() != null &&
                step.getBlock().getText() != null &&
                !step.getBlock().getText().isEmpty()) {

            headerWvEnhanced.setText(step.getBlock().getText());
            headerWvEnhanced.setVisibility(View.VISIBLE);

        } else {
            headerWvEnhanced.setVisibility(View.GONE);
        }

        updateCommentState();

        routeStepPresenter.attachView(this);
        if (unit != null) {
            nextLessonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    routeStepPresenter.clickNextLesson(unit);
                }
            });
            previousLessonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    routeStepPresenter.clickPreviousLesson(unit);
                }
            });


            routeStepPresenter.checkStepForFirst(step.getId(), lesson, unit);
            routeStepPresenter.checkStepForLast(step.getId(), lesson, unit);
        }

        bus.register(this);
    }

    private void updateCommentState() {
        if (step != null && step.getDiscussion_proxy() != null) {
            showComment();
        } else {
            openCommentViewClickable.setVisibility(View.GONE);
        }
    }

    private void showComment() {
        openCommentViewClickable.setVisibility(View.VISIBLE);
        openCommentViewClickable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int discussionCount = step.getDiscussions_count();
                shell.getScreenProvider().openComments(getContext(), step.getDiscussion_proxy(), step.getId());
                if (discussionCount == 0) {
                    shell.getScreenProvider().openNewCommentForm(getActivity(), step.getId(), null); //show new form, but in back stack comment oldList is exist.
                }
            }
        });

        int discussionCount = step.getDiscussions_count();
        if (discussionCount > 0) {
            textForComment.setText(MainApplication.getAppContext().getResources().getQuantityString(R.plurals.open_comments, discussionCount, discussionCount));
        } else {
            textForComment.setText(MainApplication.getAppContext().getResources().getString(R.string.open_comments_zero));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                analytic.reportEvent(Analytic.Interaction.SHARE_STEP_CLICK);
                DialogFragment bottomSheetDialogFragment = StepShareDialogFragment.newInstance(step, lesson, unit);
                if (bottomSheetDialogFragment != null && !bottomSheetDialogFragment.isAdded()) {
                    bottomSheetDialogFragment.show(getFragmentManager(), null);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSoftKeypad();
    }

    @Override
    public void onDestroyView() {
        bus.unregister(this);
        openCommentViewClickable.setOnClickListener(null);
        routeStepPresenter.detachView(this);
        nextLessonView.setOnClickListener(null);
        previousLessonView.setOnClickListener(null);
        super.onDestroyView();
    }

    @Subscribe
    public void onNewCommentWasAdded(NewCommentWasAddedOrUpdateEvent event) {
        if (step != null && event.getTargetId() == step.getId()) {
            long[] arr = new long[]{step.getId()};

            shell.getApi().getSteps(arr).enqueue(new Callback<StepResponse>() {
                @Override
                public void onResponse(Response<StepResponse> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        StepResponse stepResponse = response.body();
                        if (stepResponse != null && stepResponse.getSteps() != null && !stepResponse.getSteps().isEmpty()) {
                            final Step stepFromInternet = stepResponse.getSteps().get(0);
                            if (stepFromInternet != null) {
                                threadPoolExecutor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        databaseFacade.addStep(stepFromInternet); //fixme: fragment in closure -> leak
                                    }
                                });

                                //fixme: it is so bad, we should be updated from model, not here =(
                                bus.post(new StepWasUpdatedEvent(stepFromInternet));
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Throwable t) {

                }
            });
        }

    }

    @Subscribe
    public void onStepWasUpdated(StepWasUpdatedEvent event) {
        Step eventStep = event.getStep();
        if (eventStep.getId() == step.getId()) {
            step.setDiscussion_proxy(eventStep.getDiscussion_proxy()); //fixme do it in immutable way
            step.setDiscussions_count(eventStep.getDiscussions_count());
            updateCommentState();
        }
    }

    @Override
    public final void showNextLessonView() {
        routeLessonRoot.setVisibility(View.VISIBLE);
        nextLessonView.setVisibility(View.VISIBLE);
    }

    @Override
    public final void openNextLesson(Unit nextUnit, Lesson nextLesson) {
        ProgressHelper.dismiss(getFragmentManager(), LOAD_DIALOG_TAG);
        shell.getScreenProvider().showSteps(getActivity(), nextUnit, nextLesson, section);
        getActivity().finish();
    }

    @Override
    public void showLoadDialog() {
        DialogFragment dialogFragment = LoadingProgressDialogFragment.Companion.newInstance();
        if (!dialogFragment.isAdded()) {
            dialogFragment.show(getFragmentManager(), LOAD_DIALOG_TAG);
        }
    }

    @Override
    public void showCantGoNext() {
        ProgressHelper.dismiss(getFragmentManager(), LOAD_DIALOG_TAG);
        Toast.makeText(getContext(), R.string.cant_show_next_step, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showPreviousLessonView() {
        routeLessonRoot.setVisibility(View.VISIBLE);
        previousLessonView.setVisibility(View.VISIBLE);
    }

    @Override
    public void openPreviousLesson(Unit previousUnit, Lesson previousLesson) {
        ProgressHelper.dismiss(getFragmentManager(), LOAD_DIALOG_TAG);
        shell.getScreenProvider().showSteps(getActivity(), previousUnit, previousLesson, true, section);
        getActivity().finish();
    }

    @Override
    public void showCantGoPrevious() {
        ProgressHelper.dismiss(getFragmentManager(), LOAD_DIALOG_TAG);
        Toast.makeText(getContext(), R.string.cant_show_previous_step, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ROUTER_ROOT_VISIBILITY_KEY, routeLessonRoot.getVisibility() == View.VISIBLE);
        outState.putBoolean(NEXT_LESSON_VISIBILITY_KEY, nextLessonView.getVisibility() == View.VISIBLE);
        outState.putBoolean(PREVIOUS_LESSON_VISIBILITY_KEY, previousLessonView.getVisibility() == View.VISIBLE);
    }

}
