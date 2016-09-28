package org.stepic.droid.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.events.InternetIsEnabledEvent;
import org.stepic.droid.events.attempts.FailAttemptEvent;
import org.stepic.droid.events.attempts.SuccessAttemptEvent;
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.events.submissions.FailGettingLastSubmissionEvent;
import org.stepic.droid.events.submissions.FailSubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SuccessGettingLastSubmissionEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Dataset;
import org.stepic.droid.model.Reply;
import org.stepic.droid.ui.custom.StepikCheckBox;
import org.stepic.droid.ui.custom.StepikOptionView;
import org.stepic.droid.ui.custom.StepikRadioButton;
import org.stepic.droid.ui.custom.StepikRadioGroup;
import org.stepic.droid.util.RadioGroupHelper;

import java.util.ArrayList;
import java.util.List;

public class ChoiceStepFragment extends StepWithAttemptsFragment {

    private StepikRadioGroup choiceContainer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        choiceContainer = (StepikRadioGroup) ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_choice_attempt, attemptContainer, false);
        attemptContainer.addView(choiceContainer);
        attemptContainer.setPadding(0, 0, 0, 0);
        return v;
    }

    /**
     * it is unique for each attempt type
     *
     * @param attempt data of task
     */
    @Override
    protected void showAttempt(Attempt attempt) {
        Dataset dataset = attempt.getDataset();
        if (dataset == null) return;

        List<String> options = dataset.getOptions();
        if (options == null || options.isEmpty()) return;

        choiceContainer.removeAllViews();

        for (String option : options) {
            StepikOptionView optionViewItem;
            if (dataset.is_multiple_choice()) {
                optionViewItem = new StepikCheckBox(getActivity());
            } else {
                optionViewItem = new StepikRadioButton(getActivity());
            }
            buildChoiceItem(optionViewItem, option);
        }
    }


    //it is unique for each type
    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {
        RadioGroupHelper.setEnabled(choiceContainer, !needBlock);
    }

    //it is unique for each type of replay
    @Override
    protected Reply generateReply() {
        List<Boolean> options = new ArrayList<>();
        for (int i = 0; i < choiceContainer.getChildCount(); i++) {
            StepikOptionView view = (StepikOptionView) choiceContainer.getChildAt(i);
            options.add(view.isChecked());
        }
        return new Reply.Builder()
                .setChoices(options)
                .build();
    }


    @Override
    protected void onRestoreSubmission() {
        Reply reply = submission.getReply();
        if (reply == null) return;

        List<Boolean> choices = reply.getChoices();
        if (choices == null) return;

        for (int i = 0; i < choiceContainer.getChildCount(); i++) {
            StepikOptionView view = (StepikOptionView) choiceContainer.getChildAt(i);
            view.setChecked(choices.get(i));
        }
    }

    private void buildChoiceItem(StepikOptionView item, String rawText) {
        item.setText(rawText);
        choiceContainer.addView(item);
    }

    @Subscribe
    @Override
    public void onInternetEnabled(InternetIsEnabledEvent enabledEvent) {
        super.onInternetEnabled(enabledEvent);
    }

    @Override
    @Subscribe
    public void onSuccessLoadAttempt(SuccessAttemptEvent e) {
        super.onSuccessLoadAttempt(e);
    }

    @Override
    @Subscribe
    public void onSuccessCreateSubmission(SubmissionCreatedEvent e) {
        super.onSuccessCreateSubmission(e);
    }

    @Override
    @Subscribe
    public void onGettingSubmission(SuccessGettingLastSubmissionEvent e) {
        super.onGettingSubmission(e);
    }

    @Subscribe
    @Override
    public void onFailCreateAttemptEvent(FailAttemptEvent event) {
        super.onFailCreateAttemptEvent(event);
    }

    @Subscribe
    @Override
    public void onFailCreateSubmission(FailSubmissionCreatedEvent event) {
        super.onFailCreateSubmission(event);
    }

    @Subscribe
    public void onFailGettingSubmission(FailGettingLastSubmissionEvent e) {
        super.onFailGettingSubmission(e);
    }

    @Subscribe
    public void onNewCommentWasAdded(NewCommentWasAddedOrUpdateEvent event) {
        super.onNewCommentWasAdded(event);

    }

    @Subscribe
    public void onStepWasUpdated(StepWasUpdatedEvent event) {
        super.onStepWasUpdated(event);
    }
}
