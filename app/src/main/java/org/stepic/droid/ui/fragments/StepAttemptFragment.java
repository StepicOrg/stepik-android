package org.stepic.droid.ui.fragments;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.base.StepBaseFragment;
import org.stepic.droid.core.modules.StepModule;
import org.stepic.droid.core.presenters.StepAttemptPresenter;
import org.stepic.droid.core.presenters.contracts.StepAttemptView;
import org.stepic.droid.events.InternetIsEnabledEvent;
import org.stepic.droid.events.attempts.FailAttemptEvent;
import org.stepic.droid.events.attempts.SuccessAttemptEvent;
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.events.steps.UpdateStepEvent;
import org.stepic.droid.events.submissions.FailGettingLastSubmissionEvent;
import org.stepic.droid.events.submissions.FailSubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SuccessGettingLastSubmissionEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.DiscountingPolicyType;
import org.stepic.droid.model.LessonSession;
import org.stepic.droid.model.Reply;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Submission;
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.web.AttemptResponse;
import org.stepic.droid.web.SubmissionResponse;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public abstract class StepAttemptFragment extends StepBaseFragment implements StepAttemptView {
    protected final int FIRST_DELAY = 1000;

    @BindView(R.id.root_view)
    ViewGroup rootView;

    @BindView(R.id.result_line)
    View resultLine;

    @BindView(R.id.answer_status_icon)
    ImageView statusIcon;

    @BindView(R.id.answer_status_text)
    TextView statusTextView;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.report_problem)
    View connectionProblem;

    @BindView(R.id.root_scroll_view)
    protected NestedScrollView rootScrollView;

    @BindView(R.id.attempt_container)
    ViewGroup attemptContainer;

    @BindView(R.id.submit_button)
    Button actionButton;

    @BindView(R.id.peer_review_warning)
    View peerReviewIndicator;

    @BindString(R.string.correct)
    String correctString;

    @BindString(R.string.wrong)
    protected String wrongString;

    @BindString(R.string.submit)
    protected String submitText;

    @BindString(R.string.try_again)
    protected String tryAgainText;

    @BindView(R.id.discounting_policy_root)
    View discountingPolicyRoot;

    @BindView(R.id.discounting_policy_textview)
    TextView discountingPolicyTextView;

    protected Attempt attempt = null;
    protected Submission submission = null;
    protected int numberOfSubmissions = -1;

    protected Handler handler;

    @BindDrawable(R.drawable.ic_correct)
    protected Drawable correctIcon;

    @BindDrawable(R.drawable.ic_error)
    protected Drawable wrongIcon;

    @BindView(R.id.hint_text_view)
    LatexSupportableEnhancedFrameLayout hintTextView;

    @Inject
    StepAttemptPresenter stepAttemptPresenter;

    @Override
    protected void injectComponent() {
        MainApplication.component().plus(new StepModule()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_step_attempt, container, false);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }


    @Override
    public final void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        handler = new Handler();
        setListenerToActionButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showLoadState(true);
                if (submission == null || submission.getStatus() == Submission.Status.LOCAL) {
                    Bundle bundle = new Bundle();
                    bundle.putLong(FirebaseAnalytics.Param.VALUE, 1L);
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, step.getId() + "");
                    analytic.reportEvent(Analytic.Interaction.CLICK_SEND_SUBMISSION, bundle);//value
                    makeSubmission();
                } else {
                    analytic.reportEvent(Analytic.Interaction.CLICK_TRY_STEP_AGAIN);
                    tryAgain();
                }
            }
        });

        connectionProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWork();
            }
        });
        stepAttemptPresenter.attachView(this);
        startWork();
    }

    private void startWork() {
        connectionProblem.setVisibility(View.GONE);
        showLoadState(true);
        showAnswerField(false);
        if (!tryRestoreState()) {
            getExistingAttempts();
        }

        if (submission == null || submission.getStatus() == Submission.Status.LOCAL) {
            setTextToActionButton(submitText);
        } else {
            setTextToActionButton(tryAgainText);
        }

        if (step.getActions() != null && step.getActions().getDo_review() != null) {
            peerReviewIndicator.setVisibility(View.VISIBLE);
            peerReviewIndicator.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shell.getScreenProvider().openStepInWeb(getContext(), step);
                }
            });
        }
    }


    /**
     * @return false if restore was failed;
     */
    protected final boolean tryRestoreState() {
        final LessonSession lessonSession = lessonManager.restoreLessonSession(step.getId());
        if (lessonSession == null) return false;

        attempt = lessonSession.getAttempt();
        submission = lessonSession.getSubmission();
        numberOfSubmissions = lessonSession.getNumberOfSubmissionsOnFirstPage();
        if (submission == null || attempt == null) return false;

        showAttemptAbstractWrapMethod(attempt, true);
        fillSubmission(submission);
        return true;
    }

    protected final void getExistingAttempts() {
        shell.getApi().getExistingAttempts(step.getId()).enqueue(new Callback<AttemptResponse>() {
            Step localStep = step;

            @Override
            public void onResponse(Response<AttemptResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {

                    AttemptResponse body = response.body();
                    if (body == null) {
                        createNewAttempt();
                        return;
                    }

                    List<Attempt> attemptList = body.getAttempts();
                    if (attemptList == null || attemptList.isEmpty() || !attemptList.get(0).getStatus().equals("active")) {
                        createNewAttempt();
                    } else {
                        Attempt attempt = attemptList.get(0);
                        bus.post(new SuccessAttemptEvent(localStep.getId(), attempt, false));
                    }
                } else {
                    createNewAttempt();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                createNewAttempt();
            }
        });
    }


    protected final void createNewAttempt() {
        if (step == null) return;
        final long stepId = step.getId();
        // TODO: 30.09.16 refactor this, fix memory leaks
        shell.getApi().createNewAttempt(step.getId()).enqueue(new Callback<AttemptResponse>() {
            Step localStep = step;

            @Override
            public void onResponse(Response<AttemptResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<Attempt> attemptList = response.body().getAttempts();
                    if (attemptList != null && !attemptList.isEmpty()) {
                        final Attempt attempt = attemptList.get(0);
                        //ok we get attempt -> get number of submissions
                        threadPoolExecutor.execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            final Response<SubmissionResponse> numberResponse = shell.getApi().getSubmissionForStep(stepId).execute();
                                            if (numberResponse.isSuccess()) {
                                                mainHandler.post(new Function0<Unit>() {
                                                    @Override
                                                    public Unit invoke() {
                                                        numberOfSubmissions = numberResponse.body().getSubmissions().size();
                                                        bus.post(new SuccessAttemptEvent(localStep.getId(), attempt, true));
                                                        return Unit.INSTANCE;
                                                    }
                                                });
                                            } else {
                                                analytic.reportEvent(Analytic.Error.FAIL_GET_SUB_OF_STEP_CREATING_ATTEMPT);
                                                mainHandler.post(new Function0<Unit>() {
                                                    @Override
                                                    public Unit invoke() {
                                                        bus.post(new SuccessAttemptEvent(localStep.getId(), attempt, true));
                                                        return Unit.INSTANCE;
                                                    }
                                                });
                                            }
                                        } catch (Exception e) {
                                            analytic.reportEvent(Analytic.Error.FAIL_GET_SUB_OF_STEP_CREATING_ATTEMPT);
                                            mainHandler.post(new Function0<Unit>() {
                                                @Override
                                                public Unit invoke() {
                                                    bus.post(new SuccessAttemptEvent(localStep.getId(), attempt, true));
                                                    return Unit.INSTANCE;
                                                }
                                            });
                                        }
                                    }
                                }
                        );
                    } else {
                        bus.post(new FailAttemptEvent(localStep.getId()));
                    }

                } else {
                    bus.post(new FailAttemptEvent(localStep.getId()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                bus.post(new FailAttemptEvent(localStep.getId()));
            }
        });
    }

    protected void makeSubmission() {
        if (attempt == null || attempt.getId() <= 0) return;
        blockUIBeforeSubmit(true);
        final long attemptId = attempt.getId();
        final Reply reply = generateReply();
        shell.getApi().createNewSubmission(reply, attemptId).enqueue(new Callback<SubmissionResponse>() {
            @Override
            public void onResponse(Response<SubmissionResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    bus.post(new SubmissionCreatedEvent(attemptId, response.body()));
                } else {
                    bus.post(new FailSubmissionCreatedEvent(attemptId));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                bus.post(new FailSubmissionCreatedEvent(attemptId));
            }
        });

    }

    protected final void getStatusOfSubmission(long attemptId) {
        getStatusOfSubmission(attemptId, 0);
    }

    protected final void getStatusOfSubmission(final long attemptId, final int numberOfTry) {
        if (handler == null) return;
        handler.postDelayed(new Runnable() {
            long localAttemptId = attemptId;

            @Override
            public void run() {
                //todo refactor this hard to reading code. NB: here we hold reference on the outer class.
                shell.getApi().getSubmissions(localAttemptId).enqueue(new Callback<SubmissionResponse>() {
                    @Override
                    public void onResponse(final Response<SubmissionResponse> response, Retrofit retrofit) {
                        if (response.isSuccess()) {
                            threadPoolExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        final Response<SubmissionResponse> responseNumber = shell.getApi().getSubmissionForStep(step.getId()).execute();
                                        if (responseNumber.isSuccess()) {
                                            mainHandler.post(new Function0<Unit>() {
                                                @Override
                                                public Unit invoke() {

                                                    List<Submission> submissionList = response.body().getSubmissions();
                                                    if (submissionList == null || submissionList.isEmpty()) {
                                                        bus.post(new SuccessGettingLastSubmissionEvent(localAttemptId, null, responseNumber.body().getSubmissions().size())); //19.09.16 why? because we do not submissions for THIS ATTEMPT
                                                        return Unit.INSTANCE;
                                                    }

                                                    Submission submission = submissionList.get(0);

                                                    if (submission.getStatus() == Submission.Status.EVALUATION) {
                                                        bus.post(new FailGettingLastSubmissionEvent(localAttemptId, numberOfTry));
                                                        return Unit.INSTANCE;
                                                    }

                                                    bus.post(new SuccessGettingLastSubmissionEvent(localAttemptId, submission, responseNumber.body().getSubmissions().size()));

                                                    return Unit.INSTANCE;
                                                }
                                            });
                                        } else {
                                            analytic.reportEvent(Analytic.Error.FAIL_GET_SUBMISSIONS_OF_STEP);
                                        }
                                    } catch (Exception ignored) {
                                        analytic.reportEvent(Analytic.Error.FAIL_GET_SUBMISSIONS_OF_STEP);
                                    }
                                }
                            });

                        } else {
                            bus.post(new FailGettingLastSubmissionEvent(localAttemptId, numberOfTry));
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        //it is dirty swap of events:
                        bus.post(new FailSubmissionCreatedEvent(localAttemptId));
                    }
                });
            }
        }, numberOfTry * FIRST_DELAY);
    }

    @Override
    public void onDestroyView() {
        saveSession();
        stepAttemptPresenter.detachView(this);
        super.onDestroyView();
    }

    protected final void fillSubmission(@org.jetbrains.annotations.Nullable Submission submission) {
        handleDiscountingPolicy(numberOfSubmissions);
        if (submission == null || submission.getStatus() == null) {
            return;
        }

        if (submission.getHint() != null && !submission.getHint().isEmpty()) {
            hintTextView.setPlainOrLaTeXTextColored(submission.getHint(), R.color.white);
            hintTextView.setVisibility(View.VISIBLE);
        } else {
            hintTextView.setVisibility(View.GONE);
        }

        switch (submission.getStatus()) {
            case CORRECT:
                discountingPolicyRoot.setVisibility(View.GONE); // remove if user was correct
                onCorrectSubmission(submission);
                setTextToActionButton(tryAgainText);
                blockUIBeforeSubmit(true);
                break;
            case WRONG:
                onWrongSubmission();
                setTextToActionButton(tryAgainText);
                blockUIBeforeSubmit(true);
                break;
        }

        onRestoreSubmission();
        showLoadState(false);
    }

    protected final void saveSession() {
        if (attempt == null) return;

        if (submission == null) {
            Reply reply = generateReply();
            submission = new Submission(reply, attempt.getId(), Submission.Status.LOCAL);
        }

        lessonManager.saveSession(step.getId(), attempt, submission, numberOfSubmissions);
    }

    protected final void showOnlyInternetProblem(boolean isNeedShow) {
        showLoadState(!isNeedShow);
        if (isNeedShow) {
            //// FIXME: 17.01.16 it is bad way, because this class don't know about the button
            actionButton.setVisibility(View.GONE);
        } else {
            actionButton.setVisibility(View.VISIBLE);
        }
        showAnswerField(!isNeedShow);
        enableInternetMessage(isNeedShow);

        if (isNeedShow) {
            discountingPolicyRoot.setVisibility(View.GONE);
        }
    }

    private void setTextToActionButton(String text) {
        actionButton.setText(text);
    }

    protected final void onWrongSubmission() {
        if (step != null) {
            analytic.reportEvent(Analytic.Steps.WRONG_SUBMISSION_FILL, step.getId() + "");
        }
        attemptContainer.setBackgroundResource(R.color.wrong_answer_background);
        statusIcon.setImageDrawable(wrongIcon);
        statusTextView.setText(wrongString);
        resultLine.setBackgroundResource(R.color.wrong_answer_background);
        resultLine.setVisibility(View.VISIBLE);
    }

    protected final void onCorrectSubmission(Submission submission) {
        if (step != null) {
            analytic.reportEvent(Analytic.Steps.CORRECT_SUBMISSION_FILL, step.getId() + "");
        }
        markLocalProgressAsViewed();
        attemptContainer.setBackgroundResource(R.color.correct_answer_background);
        statusIcon.setImageDrawable(correctIcon);
        statusTextView.setText(getCorrectString());
        resultLine.setBackgroundResource(R.color.correct_answer_background);
        resultLine.setVisibility(View.VISIBLE);
    }


    protected final void tryAgain() {
        blockUIBeforeSubmit(false);

        // FIXME: 17.01.16 refactor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            attemptContainer.setBackground(rootView.getBackground());
        } else {
            attemptContainer.setBackgroundDrawable(rootView.getBackground());
        }

        createNewAttempt();
        submission = null;

        hintTextView.setVisibility(View.GONE);
        resultLine.setVisibility(View.GONE);
        actionButton.setText(submitText);
    }


    private void setListenerToActionButton(View.OnClickListener l) {
        actionButton.setOnClickListener(l);
    }


    protected final void markLocalProgressAsViewed() {
        bus.post(new UpdateStepEvent(step.getId(), true));
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            long stepId = step.getId();

            protected Void doInBackground(Void... params) {
                long assignmentId = databaseFacade.getAssignmentIdByStepId(stepId);
                databaseFacade.markProgressAsPassed(assignmentId);
                localProgressManager.checkUnitAsPassed(stepId);
                if (unit != null) {
                    localProgressManager.updateUnitProgress(unit.getId()); //// FIXME: 05.09.16 update lesson progress
                }
                return null;
            }
        };
        task.execute();
    }

    protected final void enableInternetMessage(boolean needShow) {
        if (needShow) {
            connectionProblem.setVisibility(View.VISIBLE);
        } else {
            connectionProblem.setVisibility(View.GONE);
        }
    }


    protected final void showAnswerField(boolean needShow) {
        if (needShow) {
            attemptContainer.setVisibility(View.VISIBLE);
        } else {
            attemptContainer.setVisibility(View.GONE);
        }
    }


    protected void showLoadState(boolean isLoading) {
        if (isLoading) {
            actionButton.setVisibility(View.GONE);
            ProgressHelper.activate(progressBar);
        } else {
            ProgressHelper.dismiss(progressBar);
            actionButton.setVisibility(View.VISIBLE);
            showAnswerField(true);
        }

    }

    private void showAttemptAbstractWrapMethod(Attempt attempt, boolean isCreatedAttempt) {
        showAttempt(attempt);
        LessonSession lessonSession = lessonManager.restoreLessonSession(step.getId());
        if ((lessonSession == null || lessonSession.getSubmission() == null) && !isCreatedAttempt) {
            getStatusOfSubmission(attempt.getId());//fill last server submission if exist
        } else {
            handleDiscountingPolicy(numberOfSubmissions);
            showLoadState(false);
        }
    }

    @Subscribe
    public void onSuccessCreateSubmission(SubmissionCreatedEvent e) {
        if (attempt == null || e.getAttemptId() != attempt.getId()) return;
        getStatusOfSubmission(attempt.getId());
    }

    @Subscribe
    public void onFailGettingSubmission(FailGettingLastSubmissionEvent e) {
        if (attempt == null || e.getAttemptId() != attempt.getId()) return;

        int nextTry = e.getTryNumber() + 1;

        getStatusOfSubmission(e.getAttemptId(), nextTry);
    }

    @Subscribe
    public void onGettingSubmission(SuccessGettingLastSubmissionEvent e) {
        if (attempt == null || e.getAttemptId() != attempt.getId()) return;
        if (e.getSubmission() == null || e.getSubmission().getStatus() == null) {
            showLoadState(false);
        }
        numberOfSubmissions = e.getNumberOfSubmissionsOnFirstPage();
        submission = e.getSubmission();
        saveSession();
        fillSubmission(submission);
    }

    @Subscribe
    public void onInternetEnabled(InternetIsEnabledEvent enabledEvent) {
        if (connectionProblem.getVisibility() == View.VISIBLE) {
            enableInternetMessage(false);
            startWork();
        }
    }

    @Subscribe
    public void onSuccessLoadAttempt(SuccessAttemptEvent e) {
        if (step == null || e.getStepId() != step.getId() || e.getAttempt() == null) return;

        showAttemptAbstractWrapMethod(e.getAttempt(), e.isJustCreated());
        attempt = e.getAttempt();
    }

    @Subscribe
    public void onFailCreateAttemptEvent(FailAttemptEvent event) {
        if (step == null || event.getStepId() != step.getId()) return;
        showOnlyInternetProblem(true);
    }

    @Subscribe
    public void onFailCreateSubmission(FailSubmissionCreatedEvent event) {
        if (attempt == null || event.getAttemptId() != attempt.getId()) return;
        showOnlyInternetProblem(true);
    }

    protected abstract void showAttempt(Attempt attempt);

    protected abstract Reply generateReply();

    protected abstract void blockUIBeforeSubmit(boolean needBlock);

    protected abstract void onRestoreSubmission();

    protected String getCorrectString() {
        return correctString;
    }

    @Subscribe
    public void onNewCommentWasAdded(NewCommentWasAddedOrUpdateEvent event) {
        super.onNewCommentWasAdded(event);

    }

    @Subscribe
    public void onStepWasUpdated(StepWasUpdatedEvent event) {
        super.onStepWasUpdated(event);
    }

    private void handleDiscountingPolicy(int numberOfSubmission) {
        if (section == null || section.getDiscountingPolicy() == null || section.getDiscountingPolicy() == DiscountingPolicyType.noDiscount) {
            // do nothing
            return;
        }

        DiscountingPolicyType discountingPolicyType = section.getDiscountingPolicy();
        Timber.d("try show discounting policy on 1st page %d", numberOfSubmission);
        Timber.d("discounting policy type is %s", discountingPolicyType);

        if (numberOfSubmission < 0) {
            discountingPolicyRoot.setVisibility(View.GONE);
            return;
        }

        if (discountingPolicyType == DiscountingPolicyType.inverse) {
            discountingPolicyRoot.setVisibility(View.VISIBLE);
            discountingPolicyTextView.setText(getString(R.string.discount_policy_inverse_title));
        } else if (discountingPolicyType == DiscountingPolicyType.firstOne) {
            handleDiscountingPolicyLimitedSubmission(discountingPolicyType.numberOfTries(), numberOfSubmission);
        } else if (discountingPolicyType == DiscountingPolicyType.firstThree) {
            handleDiscountingPolicyLimitedSubmission(discountingPolicyType.numberOfTries(), numberOfSubmission);
        }
    }

    private void handleDiscountingPolicyLimitedSubmission(int numberOfTries, int numberOfSubmission) {
        // last submission can be success
        int remain = numberOfTries - numberOfSubmissions;
        String warningText;
        if (remain > 0) {
            warningText = getResources().getQuantityString(R.plurals.discount_policy_first_n, remain, remain);
        } else {
            warningText = getString(R.string.discount_policy_no_way);
        }

        discountingPolicyRoot.setVisibility(View.VISIBLE);
        discountingPolicyTextView.setText(warningText);
    }
}