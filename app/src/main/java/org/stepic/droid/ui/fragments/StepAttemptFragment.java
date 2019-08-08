package org.stepic.droid.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.base.Client;
import org.stepic.droid.base.StepBaseFragment;
import org.stepic.droid.core.LessonSessionManager;
import org.stepic.droid.core.internetstate.contract.InternetEnabledListener;
import org.stepic.droid.core.presenters.StepAttemptPresenter;
import org.stepic.droid.core.presenters.StreakPresenter;
import org.stepic.droid.core.presenters.contracts.StepAttemptView;
import org.stepic.droid.fonts.FontType;
import org.stepic.droid.model.LessonSession;
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout;
import org.stepic.droid.ui.dialogs.DiscountingPolicyDialogFragment;
import org.stepic.droid.ui.dialogs.TimeIntervalPickerDialogFragment;
import org.stepic.droid.ui.listeners.NextMoveable;
import org.stepic.droid.util.ColorUtil;
import org.stepic.droid.util.DeviceInfoUtil;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.RatingUtil;
import org.stepic.droid.util.RatingUtilKt;
import org.stepic.droid.util.SnackbarExtensionKt;
import org.stepic.droid.util.StepExtensionsKt;
import org.stepic.droid.util.SubmissionExtensionsKt;
import org.stepik.android.domain.feedback.model.SupportEmailData;
import org.stepik.android.domain.progress.interactor.LocalProgressInteractor;
import org.stepik.android.model.DiscountingPolicyType;
import org.stepik.android.model.Reply;
import org.stepik.android.model.Step;
import org.stepik.android.model.Submission;
import org.stepik.android.model.attempts.Attempt;
import org.stepik.android.view.app_rating.ui.dialog.RateAppDialog;

import javax.inject.Inject;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import kotlin.collections.CollectionsKt;
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

public abstract class StepAttemptFragment extends StepBaseFragment implements
        StepAttemptView,
        InternetEnabledListener,
        RateAppDialog.Companion.Callback,
        TimeIntervalPickerDialogFragment.Companion.Callback {

    private final int DISCOUNTING_POLICY_REQUEST_CODE = 131;

    @BindView(R.id.rootStepAttemptView)
    ViewGroup rootView;

    @BindView(R.id.answer_status_text)
    TextView statusTextView;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.reportProblem)
    View connectionProblem;

    @BindView(R.id.attempt_container)
    ViewGroup attemptContainer;

    @BindView(R.id.stepAttemptSubmitButton)
    Button actionButton;

    @BindView(R.id.buttonsContainer)
    ViewGroup actionButtonsContainer;

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

    @BindString(R.string.next)
    String next;

    @BindView(R.id.discounting_policy_textview)
    TextView discountingPolicyTextView;

    @BindView(R.id.submission_restriction_textview)
    TextView submissionRestrictionTextView;

    @BindView(R.id.tryAgainOnCorrectButton)
    View tryAgainOnCorrectButton;

    private View.OnClickListener onNextListener;

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
    Client<InternetEnabledListener> internetEnabledListenerClient;

    @Inject
    StreakPresenter streakPresenter;

    @Inject
    LocalProgressInteractor localProgressInteractor;

    private View.OnClickListener actionButtonGeneralListener;

    @Override
    protected void injectComponent() {
        App.Companion
                .componentManager()
                .stepComponent(step.getId())
                .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step_attempt, container, false);
    }

    @Override
    public final void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        actionButtonGeneralListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (submission == null || submission.getStatus() == Submission.Status.LOCAL) {
                    Bundle bundle = new Bundle();
                    bundle.putLong(FirebaseAnalytics.Param.VALUE, 1L);
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, step.getId() + "");
                    String codeLanguage = SubmissionExtensionsKt.getLanguage(submission);
                    if (codeLanguage != null) {
                        bundle.putString(Analytic.Steps.CODE_LANGUAGE_KEY, codeLanguage);
                    }
                    String stepType = StepExtensionsKt.getStepType(step);
                    bundle.putString(Analytic.Steps.STEP_TYPE_KEY, stepType);
                    getAnalytic().reportEventWithName(Analytic.Steps.CLICK_SEND_SUBMISSION_STEP_TYPE, stepType);

                    makeSubmission();
                } else {
                    getAnalytic().reportEvent(Analytic.Interaction.CLICK_TRY_STEP_AGAIN);
                    tryAgain();
                }
            }
        };
        setListenerToActionButton(actionButtonGeneralListener);

        View.OnClickListener onTryAgainCorrectListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAnalytic().reportEvent(Analytic.Interaction.CLICK_TRY_STEP_AGAIN_AFTER_CORRECT);
                tryAgain();
            }
        };
        tryAgainOnCorrectButton.setOnClickListener(onTryAgainCorrectListener);

        onNextListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                boolean handled = ((NextMoveable) getActivity()).moveNext();
                if (!handled) {
                    if (unit != null) {
                        routeStepPresenter.clickNextLesson(unit);
                    } else {
                        Toast.makeText(getContext(), R.string.cant_show_next_step, Toast.LENGTH_SHORT).show();
                    }
                }
                v.setEnabled(true);
            }
        };

        connectionProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepAttemptPresenter.startLoadAttempt(step, true);
            }
        });
        stepAttemptPresenter.attachView(this);
        internetEnabledListenerClient.subscribe(this);
        stepAttemptPresenter.startLoadAttempt(step);
    }

    private void makeSubmission() {
        if (attempt == null || attempt.getId() <= 0) return;

        if (section != null && section.getDiscountingPolicy() != DiscountingPolicyType.NoDiscount
                && getUserPreferences().isShowDiscountingPolicyWarning() && !step.isCustomPassed()) {
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

    @Override
    protected void attachStepTextWrapper() {
        stepTextWrapper.attach(rootView, true);
    }

    @Override
    protected void detachStepTextWrapper() {
        stepTextWrapper.detach(rootView);
    }

    private void makeSubmissionDirectly() {
        showActionButtonLoadState(true);
        blockUIBeforeSubmit(true);
        final long attemptId = attempt.getId();
        final Reply reply = generateReply();
        stepAttemptPresenter.postSubmission(step, reply, attemptId);
    }

    @Override
    public void onDestroyView() {
        saveSession(true);
        internetEnabledListenerClient.unsubscribe(this);
        stepAttemptPresenter.detachView(this);
        super.onDestroyView();
    }

    protected final void fillSubmission(@Nullable Submission submission) {
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
                tryAgainOnCorrectButton.setVisibility(View.VISIBLE);
                actionButtonsContainer.setVisibility(View.VISIBLE);
                if (step.getHasSubmissionRestriction()) {
                    tryAgainOnCorrectButton.setVisibility(View.GONE);
                }
                setListenerToActionButton(onNextListener);
                onCorrectSubmission();
                setTextToActionButton(next);
                blockUIBeforeSubmit(true);
                break;
            case WRONG:
                onWrongSubmission();
                setListenerToActionButton(actionButtonGeneralListener);
                tryAgainOnCorrectButton.setVisibility(View.GONE);
                setTextToActionButton(tryAgainText);
                actionButton.setEnabled(true); // "try again" always
                blockUIBeforeSubmit(true);
                break;
        }

        onRestoreSubmission();
    }

    private void showStreakDialog(int daysOfCurrentStreakIncludeToday) {
        SpannableString streakTitle = new SpannableString(getString(R.string.streak_dialog_title));
        streakTitle.setSpan(new ForegroundColorSpan(Color.BLACK), 0, streakTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(TypefaceUtils.load(getContext().getAssets(), getFontsProvider().provideFontPath(FontType.bold)));
        streakTitle.setSpan(typefaceSpan, 0, streakTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        String description;
        if (daysOfCurrentStreakIncludeToday > 0) {
            getAnalytic().reportEvent(Analytic.Streak.SHOW_DIALOG_UNDEFINED_STREAKS, daysOfCurrentStreakIncludeToday + "");
            description = getResources().getQuantityString(R.plurals.streak_description, daysOfCurrentStreakIncludeToday, daysOfCurrentStreakIncludeToday);
        } else {
            getAnalytic().reportEvent(Analytic.Streak.SHOW_DIALOG_POSITIVE_STREAKS, daysOfCurrentStreakIncludeToday + "");
            description = getString(R.string.streak_description_not_positive);
        }

        getAnalytic().reportEvent(Analytic.Streak.SHOWN_MATERIAL_DIALOG);
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
                        getAnalytic().reportEvent(Analytic.Streak.POSITIVE_MATERIAL_DIALOG);
                        DialogFragment dialogFragment = TimeIntervalPickerDialogFragment.Companion.newInstance();
                        if (!dialogFragment.isAdded()) {
                            dialogFragment.setTargetFragment(StepAttemptFragment.this, 0);
                            dialogFragment.show(getFragmentManager(), null);
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        getAnalytic().reportEvent(Analytic.Streak.NEGATIVE_MATERIAL_DIALOG);
                        messageOnNotEnablingNotification();
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
            tryAgainOnCorrectButton.setVisibility(View.GONE);
            actionButtonsContainer.setVisibility(View.GONE);
        } else {
            actionButtonsContainer.setVisibility(View.VISIBLE);
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
            getAnalytic().reportEvent(Analytic.Steps.WRONG_SUBMISSION_FILL, step.getId() + "");
        }
        attemptContainer.setBackgroundResource(R.color.wrong_answer_background);
        statusTextView.setCompoundDrawablesWithIntrinsicBounds(wrongIcon, null, null, null);
        statusTextView.setText(wrongString);
        statusTextView.setBackgroundResource(R.color.wrong_answer_background);
        statusTextView.setVisibility(View.VISIBLE);
    }

    protected final void onCorrectSubmission() {
        if (step != null) {
            getAnalytic().reportEvent(Analytic.Steps.CORRECT_SUBMISSION_FILL, step.getId() + "");
        }
        markLocalProgressAsViewed();
        attemptContainer.setBackgroundResource(R.color.correct_answer_background);
        statusTextView.setCompoundDrawablesWithIntrinsicBounds(correctIcon, null, null, null);
        statusTextView.setText(getCorrectString());
        statusTextView.setBackgroundResource(R.color.correct_answer_background);
        statusTextView.setVisibility(View.VISIBLE);
    }

    protected final void tryAgain() {
        showActionButtonLoadState(true);
        blockUIBeforeSubmit(false);
        setListenerToActionButton(actionButtonGeneralListener);

        resetBackgroundOfAttempt();

        hintTextView.setVisibility(View.GONE);
        statusTextView.setVisibility(View.GONE);
        tryAgainOnCorrectButton.setVisibility(View.GONE);
        setTextToActionButton(submitText);

        stepAttemptPresenter.tryAgain(step.getId());
        submission = null;

    }

    protected final void resetBackgroundOfAttempt() {
        if (attemptContainer.getBackground() == rootView.getBackground()) {
            return;
        }

        attemptContainer.setBackground(rootView.getBackground());
    }

    private void setListenerToActionButton(View.OnClickListener listener) {
        actionButton.setOnClickListener(listener);
    }

    protected final void markLocalProgressAsViewed() {
        if (!step.isCustomPassed()) {
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                private final Step step = StepAttemptFragment.this.step;

                protected Void doInBackground(Void... params) {
                    long assignmentId = getDatabaseFacade().getAssignmentIdByStepId(this.step.getId());
                    getDatabaseFacade().markProgressAsPassed(assignmentId);
                    try {
                        localProgressInteractor.updateStepsProgress(CollectionsKt.listOf(this.step)).blockingAwait();
                    } catch (Exception e) {
                        // no op
                    }
                    return null;
                }
            };
            task.executeOnExecutor(getThreadPoolExecutor());
        }
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
            actionButtonsContainer.setVisibility(View.GONE);
            ProgressHelper.activate(progressBar);
        } else {
            ProgressHelper.dismiss(progressBar);
            actionButtonsContainer.setVisibility(View.VISIBLE);
        }

    }

    private void showAttemptAbstractWrapMethod(Attempt attempt, boolean isCreatedAttempt) {
        showAttempt(attempt);
        LessonSession lessonSession = lessonManager.restoreLessonSession(step.getId());
        if ((lessonSession == null || lessonSession.getSubmission() == null) && !isCreatedAttempt) {
            stepAttemptPresenter.getStatusOfSubmission(step, attempt.getId());//fill last server submission if exist
        } else {
            // when just now created --> do not need show submission, it is not exist.
            stepAttemptPresenter.handleDiscountingPolicy(numberOfSubmissions, section, step);
            showActionButtonLoadState(false);
            showAnswerField(true);

            stepAttemptPresenter.handleStepRestriction(step, numberOfSubmissions);
        }
    }

    protected abstract void showAttempt(Attempt attempt);

    protected abstract Reply generateReply();

    protected abstract void blockUIBeforeSubmit(boolean needBlock);

    protected abstract void onRestoreSubmission();

    protected String getCorrectString() {
        return correctString;
    }

    @Override
    public void onResume() {
        super.onResume();
        rootView.requestFocus();
    }

    @Override
    public void onResultHandlingDiscountPolicy(boolean needShow, DiscountingPolicyType discountingPolicyType, int remainTries) {
        if (!needShow || discountingPolicyType == null) {
            discountingPolicyTextView.setVisibility(View.GONE);
            return;
        }

        String warningText;
        if (discountingPolicyType == DiscountingPolicyType.Inverse) {
            warningText = getString(R.string.discount_policy_inverse_title);
        } else if (discountingPolicyType == DiscountingPolicyType.FirstOne || discountingPolicyType == DiscountingPolicyType.FirstThree) {
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
    public void onNeedShowAttempt(@Nullable Attempt attempt, boolean isCreated, @Nullable Integer numberOfSubmissionsForStep) {
        if (numberOfSubmissionsForStep != null) {
            this.numberOfSubmissions = numberOfSubmissionsForStep;
        }
        this.attempt = attempt;
        showAttemptAbstractWrapMethod(this.attempt, isCreated);
    }

    @Override
    public void onConnectionFailWhenLoadAttempt() {
        showOnlyInternetProblem(true);
    }

    @Override
    public void onNeedFillSubmission(@Nullable Submission submission, int numberOfSubmissions) {
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
                getScreenManager().openStepInWeb(getContext(), step);
            }
        });
    }

    @Override
    public final void onNeedResolveActionButtonText() {
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
        }
    }

    private void messageOnNotEnablingNotification() {
        SnackbarExtensionKt
                .setTextColor(
                        Snackbar.make(rootView,
                                R.string.streak_notification_canceled,
                                Snackbar.LENGTH_LONG),
                        ColorUtil.INSTANCE.getColorArgb(R.color.white,
                                getContext()))
                .show();
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
                actionButtonsContainer.setVisibility(View.GONE); //we cant send more
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
        showStreakDialog(numberOfStreakDayIncludeToday);
    }

    @Override
    public void onInternetEnabled() {
        if (connectionProblem.getVisibility() == View.VISIBLE) {
            stepAttemptPresenter.startLoadAttempt(step);
        }
    }

    @Override
    public void onNeedShowRateDialog() {
        RateAppDialog rateAppDialog = RateAppDialog.Companion.newInstance();
        rateAppDialog.setTargetFragment(this, 0);
        if (!rateAppDialog.isAdded()) {
            getAnalytic().reportEvent(Analytic.Rating.SHOWN);
            rateAppDialog.show(getFragmentManager(), null);
        }
    }

    //Rate dialog callback:

    @Override
    public void onClickLater(int starNumber) {
        if (RatingUtil.INSTANCE.isExcellent(starNumber)) {
            RatingUtilKt.reportRateEvent(getAnalytic(), starNumber, Analytic.Rating.POSITIVE_LATER);
        } else {
            RatingUtilKt.reportRateEvent(getAnalytic(), starNumber, Analytic.Rating.NEGATIVE_LATER);
        }
    }

    @Override
    public void onClickGooglePlay(int starNumber) {
        getSharedPreferenceHelper().afterRateWasHandled();
        RatingUtilKt.reportRateEvent(getAnalytic(), starNumber, Analytic.Rating.POSITIVE_APPSTORE);

        if (getConfig().isAppInStore()) {
            getScreenManager().showStoreWithApp(getActivity());
        } else {
            setupTextFeedback();
        }
    }

    @Override
    public void onClickSupport(int starNumber) {
        getSharedPreferenceHelper().afterRateWasHandled();
        RatingUtilKt.reportRateEvent(getAnalytic(), starNumber, Analytic.Rating.NEGATIVE_EMAIL);
        setupTextFeedback();
    }

    @Override
    public void sendTextFeedback(@NotNull SupportEmailData supportEmailData) {
        screenManager.openTextFeedBack(requireContext(), supportEmailData);
    }

    @Override
    public void onTimeIntervalPicked(int chosenInterval) {
        streakPresenter.setStreakTime(chosenInterval);
        getAnalytic().reportEvent(Analytic.Streak.CHOOSE_INTERVAL, chosenInterval + "");
        SnackbarExtensionKt
                .setTextColor(
                        Snackbar.make(rootView,
                                R.string.streak_notification_enabled_successfully,
                                Snackbar.LENGTH_LONG),
                        ColorUtil.INSTANCE.getColorArgb(R.color.white,
                                getContext()))
                .show();
    }

    @Override
    public void onTimeIntervalDialogCancelled() {
        getAnalytic().reportEvent(Analytic.Streak.CHOOSE_INTERVAL_CANCELED);
        messageOnNotEnablingNotification();
    }

    protected final void hideWrongStatus() {
        statusTextView.setVisibility(View.GONE);
    }

    protected final void hideHint() {
        hintTextView.setVisibility(View.GONE);
    }

    private void setupTextFeedback() {
        stepAttemptPresenter.sendTextFeedback(
            getString(R.string.feedback_subject),
            DeviceInfoUtil.getInfosAboutDevice(getContext(), "\n")
        );
    }
}