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

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.core.commentcount.contract.CommentCountListener;
import org.stepic.droid.core.presenters.AnonymousPresenter;
import org.stepic.droid.core.presenters.RouteStepPresenter;
import org.stepic.droid.core.presenters.contracts.AnonymousView;
import org.stepic.droid.core.presenters.contracts.RouteStepView;
import org.stepik.android.model.Lesson;
import org.stepik.android.model.Section;
import org.stepik.android.model.Step;
import org.stepik.android.model.Unit;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout;
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment;
import org.stepic.droid.ui.dialogs.StepShareDialogFragment;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.web.StepResponse;

import java.lang.ref.WeakReference;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class StepBaseFragment extends FragmentBase
        implements RouteStepView,
        AnonymousView,
        CommentCountListener {

    @BindView(R.id.text_header_enhanced)
    protected LatexSupportableEnhancedFrameLayout headerWvEnhanced;

    @BindView(R.id.open_comments_text)
    protected TextView textForComment;

    @BindView(R.id.auth_line_text)
    TextView authLineText;

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
    protected RouteStepPresenter routeStepPresenter;

    @Inject
    AnonymousPresenter anonymousPresenter;

    @Inject
    Client<CommentCountListener> commentCountListenerClient;

    @Override
    protected void injectComponent() {
        App.Companion
                .componentManager()
                .stepComponent(step.getId())
                .inject(this);
    }

    @Override
    protected final void onReleaseComponent() {
        App
                .Companion
                .componentManager()
                .releaseStepComponent(step.getId());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        step = getArguments().getParcelable(AppConstants.KEY_STEP_BUNDLE);
        lesson = getArguments().getParcelable(AppConstants.KEY_LESSON_BUNDLE);
        unit = getArguments().getParcelable(AppConstants.KEY_UNIT_BUNDLE);
        section = getArguments().getParcelable(AppConstants.KEY_SECTION_BUNDLE);
        super.onCreate(savedInstanceState);
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
            headerWvEnhanced.setTextIsSelectable(true);

        } else {
            headerWvEnhanced.setVisibility(View.GONE);
        }

        updateCommentState();

        commentCountListenerClient.subscribe(this);
        routeStepPresenter.attachView(this);
        anonymousPresenter.attachView(this);
        anonymousPresenter.checkForAnonymous();
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

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authLineText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreenManager().showLaunchScreen(getActivity());
            }
        });
    }

    private void updateCommentState() {
        if (step != null && step.getDiscussionProxy() != null) {
            showComment();
        } else {
            textForComment.setVisibility(View.GONE);
        }
    }

    private void showComment() {
        textForComment.setVisibility(View.VISIBLE);
        textForComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int discussionCount = step.getDiscussionsCount();
                getAnalytic().reportEvent(Analytic.Comments.OPEN_FROM_STEP_UI);

                if (discussionCount == 0) {
                    getScreenManager().openComments(getActivity(), step.getDiscussionProxy(), step.getId(), true); //show new form, but in back stack comment oldList is exist.
                } else {
                    getScreenManager().openComments(getActivity(), step.getDiscussionProxy(), step.getId());
                }
            }
        });

        int discussionCount = step.getDiscussionsCount();
        if (discussionCount > 0) {
            textForComment.setText(App.Companion.getAppContext().getResources().getQuantityString(R.plurals.open_comments, discussionCount, discussionCount));
        } else {
            textForComment.setText(App.Companion.getAppContext().getResources().getString(R.string.open_comments_zero));
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
                getAnalytic().reportEvent(Analytic.Interaction.SHARE_STEP_CLICK);
                DialogFragment bottomSheetDialogFragment = StepShareDialogFragment.newInstance(step, lesson, unit);
                if (bottomSheetDialogFragment != null && !bottomSheetDialogFragment.isAdded()) {
                    bottomSheetDialogFragment.show(getFragmentManager(), null);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroyView() {
        authLineText.setOnClickListener(null);
        textForComment.setOnClickListener(null);
        routeStepPresenter.detachView(this);
        commentCountListenerClient.unsubscribe(this);
        anonymousPresenter.detachView(this);
        nextLessonView.setOnClickListener(null);
        previousLessonView.setOnClickListener(null);
        super.onDestroyView();
    }

    public void onDiscussionWasUpdatedFromInternet(Step updatedStep) {
        if (updatedStep.getId() == step.getId()) {
            step.setDiscussionProxy(updatedStep.getDiscussionProxy()); //fixme do it in immutable way
            step.setDiscussionsCount(updatedStep.getDiscussionsCount());
            updateCommentState();
        }
    }

    @Override
    public final void showNextLessonView() {
        routeLessonRoot.setVisibility(View.VISIBLE);
        nextLessonView.setVisibility(View.VISIBLE);
    }

    @Override
    public final void openNextLesson(Unit nextUnit, Lesson nextLesson, Section nextSection) {
        ProgressHelper.dismiss(getFragmentManager(), LOAD_DIALOG_TAG);
        getScreenManager().showSteps(getActivity(), nextUnit, nextLesson, nextSection);
        getActivity().finish();
    }

    @Override
    public void showLoading() {
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
    public void openPreviousLesson(Unit previousUnit, Lesson previousLesson, Section previousSection) {
        ProgressHelper.dismiss(getFragmentManager(), LOAD_DIALOG_TAG);
        getScreenManager().showSteps(getActivity(), previousUnit, previousLesson, true, previousSection);
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

    @Override
    public final void onShowAnonymous(boolean isAnonymous) {
        authLineText.setVisibility(isAnonymous ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSoftKeypad();
    }

    @Override
    public void onCommentCountUpdated() {
        long[] arr = new long[]{step.getId()};
        getApi().getSteps(arr).enqueue(new StepResponseCallback(getThreadPoolExecutor(), getDatabaseFacade(), this));
    }


    //// TODO: 13.06.17 rework it in MVP style
    static class StepResponseCallback implements Callback<StepResponse> {

        private final ThreadPoolExecutor threadPoolExecutor;
        private final DatabaseFacade databaseFacade;
        private final WeakReference<StepBaseFragment> stepBaseFragmentWeakReference;


        public StepResponseCallback(ThreadPoolExecutor threadPoolExecutor, DatabaseFacade databaseFacade, StepBaseFragment stepBaseFragment) {
            this.threadPoolExecutor = threadPoolExecutor;
            this.databaseFacade = databaseFacade;
            stepBaseFragmentWeakReference = new WeakReference<>(stepBaseFragment);
        }

        @Override
        public void onResponse(Call<StepResponse> call, Response<StepResponse> response) {
            if (response.isSuccessful()) {
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


                        StepBaseFragment stepBaseFragment = stepBaseFragmentWeakReference.get();
                        if (stepBaseFragment != null) {
                            stepBaseFragment.onDiscussionWasUpdatedFromInternet(stepFromInternet);
                        }
                    }
                }
            }
        }

        @Override
        public void onFailure(Call<StepResponse> call, Throwable t) {

        }
    }
}
