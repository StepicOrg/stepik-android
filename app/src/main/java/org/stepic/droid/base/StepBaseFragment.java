package org.stepic.droid.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout;
import org.stepic.droid.web.StepResponse;

import butterknife.BindView;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public abstract class StepBaseFragment extends FragmentBase {

    @BindView(R.id.text_header_enhanced)
    protected LatexSupportableEnhancedFrameLayout headerWvEnhanced;

    @BindView(R.id.open_comments_root)
    protected View openCommentViewClickable;

    @BindView(R.id.open_comments_text)
    protected TextView textForComment;

    @BindView(R.id.next_lesson_root)
    protected View nextLessonRoot;

    protected Step step;
    protected Lesson lesson;
    protected Unit unit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        step = (Step) getArguments().getSerializable(AppConstants.KEY_STEP_BUNDLE);
        lesson = (Lesson) getArguments().getSerializable(AppConstants.KEY_LESSON_BUNDLE);
        unit = (Unit) getArguments().getSerializable(AppConstants.KEY_UNIT_BUNDLE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        nextLessonRoot.setVisibility(View.GONE);
        long[] stepIds = lesson.getSteps();
        if (stepIds != null && stepIds.length != 0) {
            long lastStepId = stepIds[stepIds.length - 1];
            if (lastStepId == step.getId()) {
                nextLessonRoot.setVisibility(View.VISIBLE);
            }
        }

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
                mShell.getScreenProvider().openComments(getContext(), step.getDiscussion_proxy(), step.getId());
                if (discussionCount == 0) {
                    mShell.getScreenProvider().openNewCommentForm(getActivity(), step.getId(), null); //show new form, but in back stack comment list is exist.
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
    public void onResume() {
        super.onResume();
        hideSoftKeypad();
    }

    @Override
    public void onDestroyView() {
        bus.unregister(this);
        openCommentViewClickable.setOnClickListener(null);
        super.onDestroyView();
    }

    @Subscribe
    public void onNewCommentWasAdded(NewCommentWasAddedOrUpdateEvent event) {
        if (step != null && event.getTargetId() == step.getId()) {
            long[] arr = new long[]{step.getId()};

            mShell.getApi().getSteps(arr).enqueue(new Callback<StepResponse>() {
                @Override
                public void onResponse(Response<StepResponse> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        StepResponse stepResponse = response.body();
                        if (stepResponse != null && stepResponse.getSteps() != null && !stepResponse.getSteps().isEmpty()) {
                            final Step stepFromInternet = stepResponse.getSteps().get(0);
                            if (stepFromInternet != null) {
                                mThreadPoolExecutor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        mDatabaseFacade.addStep(stepFromInternet); //fixme: fragment in closure -> leak
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
}
