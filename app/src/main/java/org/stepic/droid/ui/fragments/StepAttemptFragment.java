package org.stepic.droid.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.NestedScrollView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.base.StepBaseFragment;
import org.stepic.droid.core.LessonSessionManager;
import org.stepic.droid.core.modules.StepModule;
import org.stepic.droid.core.presenters.NotificationTimePresenter;
import org.stepic.droid.core.presenters.StepAttemptPresenter;
import org.stepic.droid.core.presenters.contracts.StepAttemptView;
import org.stepic.droid.events.InternetIsEnabledEvent;
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.events.steps.UpdateStepEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.DiscountingPolicyType;
import org.stepic.droid.model.LessonSession;
import org.stepic.droid.model.Reply;
import org.stepic.droid.model.Submission;
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout;
import org.stepic.droid.ui.dialogs.DiscountingPolicyDialogFragment;
import org.stepic.droid.ui.dialogs.TimeIntervalPickerDialogFragment;
import org.stepic.droid.ui.util.TimeIntervalUtil;
import org.stepic.droid.util.ColorUtil;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.SnackbarExtensionKt;

import javax.inject.Inject;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

public abstract class StepAttemptFragment extends StepBaseFragment implements StepAttemptView {
    private final int DISCOUNTING_POLICY_REQUEST_CODE = 131;
    private final int NOTIFICATION_TIME_REQUEST_CODE = 11;

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

    @BindView(R.id.discounting_policy_textview)
    TextView discountingPolicyTextView;

    @BindView(R.id.submission_restrction_textview)
    TextView submissionRestrictionTextView;

    protected Attempt attempt = null;
    protected Submission submission = null;
    protected int numberOfSubmissions = -1;

    @BindDrawable(R.drawable.ic_correct)
    protected Drawable correctIcon;

    @BindDrawable(R.drawable.ic_error)
    protected Drawable wrongIcon;

    @BindView(R.id.hint_text_view)
    LatexSupportableEnhancedFrameLayout hintTextView;

    @Inject
    StepAttemptPresenter stepAttemptPresenter;

    @Inject
    LessonSessionManager lessonManager;

    @Inject
    NotificationTimePresenter notificationTimePresenter;

    @Override
    protected void injectComponent() {
        MainApplication.component().plus(new StepModule()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step_attempt, container, false);
    }

    @Override
    public final void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListenerToActionButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                stepAttemptPresenter.startLoadAttempt(step, true);
            }
        });
        stepAttemptPresenter.attachView(this);
        stepAttemptPresenter.startLoadAttempt(step);
    }

    private void makeSubmission() {
        if (attempt == null || attempt.getId() <= 0) return;

        if (section != null && section.getDiscountingPolicy() != DiscountingPolicyType.noDiscount
                && userPreferences.isShowDiscountingPolicyWarning() && !step.is_custom_passed()) {
            //showDialog
            DialogFragment dialogFragment = DiscountingPolicyDialogFragment.Companion.newInstance();
            if (!dialogFragment.isAdded()) {
                dialogFragment.setTargetFragment(this, DISCOUNTING_POLICY_REQUEST_CODE);
                dialogFragment.show(getFragmentManager(), null);
            }
        } else {
            makeSubmissionDirectly();
        }

    }

    private void makeSubmissionDirectly() {
        showActionButtonLoadState(true);
        blockUIBeforeSubmit(true);
        final long attemptId = attempt.getId();
        final Reply reply = generateReply();
        stepAttemptPresenter.postSubmission(step.getId(), reply, attemptId);
    }

    @Override
    public void onDestroyView() {
        saveSession(true);
        stepAttemptPresenter.detachView(this);
        super.onDestroyView();
    }

    protected final void fillSubmission(@org.jetbrains.annotations.Nullable Submission submission) {
        stepAttemptPresenter.handleDiscountingPolicy(numberOfSubmissions, section, step);
        stepAttemptPresenter.handleStepRestriction(step, numberOfSubmissions);
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
                discountingPolicyTextView.setVisibility(View.GONE); // remove if user was correct
                submissionRestrictionTextView.setVisibility(View.GONE);
                if (step.getHasSubmissionRestriction()) {
                    actionButton.setVisibility(View.GONE);
                }
                onCorrectSubmission();
                setTextToActionButton(tryAgainText);
                blockUIBeforeSubmit(true);

                //// FIXME: 22.11.16 transfer to after Submit not passed step
//                showStreakDialog(3);
                break;
            case WRONG:
                onWrongSubmission();
                setTextToActionButton(tryAgainText);
                blockUIBeforeSubmit(true);
                break;
        }

        onRestoreSubmission();
    }

    private void showStreakDialog(int daysOfCurrentStreakIncludeToday) {
        SpannableString streakTitle = new SpannableString(getString(R.string.streak_dialog_title));
        streakTitle.setSpan(new ForegroundColorSpan(Color.BLACK), 0, streakTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(TypefaceUtils.load(getContext().getAssets(), "fonts/NotoSans-Bold.ttf"));
        streakTitle.setSpan(typefaceSpan, 0, streakTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        String description;
        if (daysOfCurrentStreakIncludeToday > 0) {
            analytic.reportEvent(Analytic.Streak.SHOW_DIALOG_UNDEFINED_STREAKS, daysOfCurrentStreakIncludeToday + "");
            description = getResources().getQuantityString(R.plurals.streak_description, daysOfCurrentStreakIncludeToday, daysOfCurrentStreakIncludeToday);
        } else {
            analytic.reportEvent(Analytic.Streak.SHOW_DIALOG_POSITIVE_STREAKS, daysOfCurrentStreakIncludeToday + "");
            description = getString(R.string.streak_description_not_positive);
        }

        analytic.reportEvent(Analytic.Streak.SHOWN_MATERIAL_DIALOG);
        MaterialStyledDialog dialog = new MaterialStyledDialog.Builder(getContext())
                .setTitle(streakTitle)
                .setDescription(description)
                .setHeaderDrawable(R.drawable.dialog_background)
                .setPositiveText(R.string.ok)
                .setNegativeText(R.string.later_tatle)
                .setScrollable(true, 10) // number of lines lines
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        analytic.reportEvent(Analytic.Streak.POSITIVE_MATERIAL_DIALOG);
                        DialogFragment dialogFragment = TimeIntervalPickerDialogFragment.Companion.newInstance();
                        if (!dialogFragment.isAdded()) {
                            dialogFragment.setTargetFragment(StepAttemptFragment.this, NOTIFICATION_TIME_REQUEST_CODE);
                            dialogFragment.show(getFragmentManager(), null);
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        analytic.reportEvent(Analytic.Streak.NEGATIVE_MATERIAL_DIALOG);
                    }
                })
                .build();
        dialog.show();
    }

    protected final void saveSession(boolean isNeedGetFromUI) {
        if (attempt == null) return;

        if (submission == null || (isNeedGetFromUI && submission.getStatus() == Submission.Status.LOCAL)) {
            Reply reply = generateReply();
            submission = new Submission(reply, attempt.getId(), Submission.Status.LOCAL);
        }

        lessonManager.saveSession(step.getId(), attempt, submission, numberOfSubmissions);
    }

    protected final void showOnlyInternetProblem(boolean isNeedShow) {
        showActionButtonLoadState(!isNeedShow);
        if (isNeedShow) {
            actionButton.setVisibility(View.GONE);
        } else {
            actionButton.setVisibility(View.VISIBLE);
        }
        showAnswerField(!isNeedShow);
        enableInternetMessage(isNeedShow);

        if (isNeedShow) {
            discountingPolicyTextView.setVisibility(View.GONE);
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

    protected final void onCorrectSubmission() {
        if (step != null) {
            analytic.reportEvent(Analytic.Steps.CORRECT_SUBMISSION_FILL, step.getId() + "");
        }
        sharedPreferenceHelper.trackWhenUserSolved();
        markLocalProgressAsViewed();
        attemptContainer.setBackgroundResource(R.color.correct_answer_background);
        statusIcon.setImageDrawable(correctIcon);
        statusTextView.setText(getCorrectString());
        resultLine.setBackgroundResource(R.color.correct_answer_background);
        resultLine.setVisibility(View.VISIBLE);
    }

    protected final void tryAgain() {
        showActionButtonLoadState(true);
        blockUIBeforeSubmit(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            attemptContainer.setBackground(rootView.getBackground());
        } else {
            attemptContainer.setBackgroundDrawable(rootView.getBackground());
        }

        hintTextView.setVisibility(View.GONE);
        resultLine.setVisibility(View.GONE);
        actionButton.setText(submitText);

        stepAttemptPresenter.tryAgain(step.getId());
        submission = null;

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

    protected void showActionButtonLoadState(boolean isLoading) {
        if (isLoading) {
            actionButton.setVisibility(View.GONE);
            ProgressHelper.activate(progressBar);
        } else {
            ProgressHelper.dismiss(progressBar);
            actionButton.setVisibility(View.VISIBLE);
        }

    }

    private void showAttemptAbstractWrapMethod(Attempt attempt, boolean isCreatedAttempt) {
        showAttempt(attempt);
        LessonSession lessonSession = lessonManager.restoreLessonSession(step.getId());
        if ((lessonSession == null || lessonSession.getSubmission() == null) && !isCreatedAttempt) {
            stepAttemptPresenter.getStatusOfSubmission(step.getId(), attempt.getId());//fill last server submission if exist
        } else {
            // when just now created --> do not need show submission, it is not exist.
            stepAttemptPresenter.handleDiscountingPolicy(numberOfSubmissions, section, step);
            showActionButtonLoadState(false);
            showAnswerField(true);

            stepAttemptPresenter.handleStepRestriction(step, numberOfSubmissions);
        }
    }

    @Subscribe
    public void onInternetEnabled(InternetIsEnabledEvent enabledEvent) {
        if (connectionProblem.getVisibility() == View.VISIBLE) {
            stepAttemptPresenter.startLoadAttempt(step);
        }
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

    @Override
    public void onResultHandlingDiscountPolicy(boolean needShow, DiscountingPolicyType discountingPolicyType, int remainTries) {
        if (!needShow || discountingPolicyType == null) {
            discountingPolicyTextView.setVisibility(View.GONE);
            return;
        }

        String warningText;
        if (discountingPolicyType == DiscountingPolicyType.inverse) {
            warningText = getString(R.string.discount_policy_inverse_title);
        } else if (discountingPolicyType == DiscountingPolicyType.firstOne || discountingPolicyType == DiscountingPolicyType.firstThree) {
            if (remainTries > 0) {
                warningText = getResources().getQuantityString(R.plurals.discount_policy_first_n, remainTries, remainTries);
            } else {
                warningText = getString(R.string.discount_policy_no_way);
            }
        } else {
            discountingPolicyTextView.setVisibility(View.GONE);
            return;
        }

        discountingPolicyTextView.setText(warningText);
        discountingPolicyTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStartLoadingAttempt() {
        enableInternetMessage(false);
        showAnswerField(false);
        showActionButtonLoadState(true);
    }

    @Override
    public void onNeedShowAttempt(@org.jetbrains.annotations.Nullable Attempt attempt, boolean isCreated, int numberOfSubmissionsForStep) {
        this.numberOfSubmissions = numberOfSubmissionsForStep;
        this.attempt = attempt;
        showAttemptAbstractWrapMethod(this.attempt, isCreated);
    }

    @Override
    public void onConnectionFailWhenLoadAttempt() {
        showOnlyInternetProblem(true);
    }

    @Override
    public void onNeedFillSubmission(Submission submission, int numberOfSubmissions) {
        enableInternetMessage(false);
        showActionButtonLoadState(false);
        showAnswerField(true);

        showActionButtonLoadState(false);
        this.numberOfSubmissions = numberOfSubmissions;
        this.submission = submission;
        saveSession(false);
        fillSubmission(submission);
    }

    @Override
    public void onConnectionFailOnSubmit() {
        blockUIBeforeSubmit(false);
        showActionButtonLoadState(false);
        Toast.makeText(getContext(), R.string.internet_problem, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNeedShowPeerReview() {
        peerReviewIndicator.setVisibility(View.VISIBLE);
        peerReviewIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shell.getScreenProvider().openStepInWeb(getContext(), step);
            }
        });
    }

    @Override
    public void onNeedResolveActionButtonText() {
        if (submission == null || submission.getStatus() == Submission.Status.LOCAL) {
            setTextToActionButton(submitText);
        } else {
            setTextToActionButton(tryAgainText);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DISCOUNTING_POLICY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            makeSubmissionDirectly();
        } else if (requestCode == NOTIFICATION_TIME_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                int intervalCode = data.getIntExtra(TimeIntervalPickerDialogFragment.Companion.getResultIntervalCodeKey(), TimeIntervalUtil.INSTANCE.getMiddle());
                sharedPreferenceHelper.setStreakNotificationEnabled(true);
                notificationTimePresenter.setStreakTime(intervalCode); // we do not need attach this view, because we need only set in model
                analytic.reportEvent(Analytic.Streak.CHOOSE_INTERVAL, intervalCode + "");
                SnackbarExtensionKt
                        .setTextColor(
                                Snackbar.make(rootView,
                                        R.string.streak_notification_enabled_successfully,
                                        Snackbar.LENGTH_LONG),
                                ColorUtil.INSTANCE.getColorArgb(R.color.white,
                                        getContext()))
                        .show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                analytic.reportEvent(Analytic.Streak.CHOOSE_INTERVAL_CANCELED);
                SnackbarExtensionKt
                        .setTextColor(
                                Snackbar.make(rootView,
                                        R.string.streak_notification_canceled,
                                        Snackbar.LENGTH_LONG),
                                ColorUtil.INSTANCE.getColorArgb(R.color.white,
                                        getContext()))
                        .show();
            }
        }
    }

    @Override
    public void onResultHandlingSubmissionRestriction(boolean needShow, int numberForShow) {
        if (needShow) {
            String warningText;
            if (numberForShow > 0) {
                warningText = getResources().getQuantityString(R.plurals.restriction_submission, numberForShow, numberForShow);
            } else {
                warningText = getString(R.string.restriction_submission_enough);
                blockUIBeforeSubmit(true);
                actionButton.setVisibility(View.GONE); //we cant send more
            }
            submissionRestrictionTextView.setText(warningText);
            submissionRestrictionTextView.setVisibility(View.VISIBLE);
        } else {
            submissionRestrictionTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNeedShowStreakDialog(int numberOfStreakDayIncludeToday) {
        // this submission is correct and user posted it 1st time
        sharedPreferenceHelper.trackWhenUserSolved();
        showStreakDialog(numberOfStreakDayIncludeToday);
    }
}