package org.stepic.droid.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.LayoutInflater;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.events.InternetIsEnabledEvent;
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
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

import timber.log.Timber;

public class ChoiceStepFragment extends StepAttemptFragment {

    private StepikRadioGroup choiceContainer;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        choiceContainer = (StepikRadioGroup) ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_choice_attempt, attemptContainer, false);
        attemptContainer.addView(choiceContainer);
        attemptContainer.setPadding(0, 0, 0, 0);
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

        if (!dataset.is_multiple_choice()) {
            //radio button:
            boolean isEmpty = checkOnEmptyChecked();
            if (isEmpty) {
                choiceContainer.setOnCheckedChangeListener(new StepikRadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(StepikRadioGroup group, @IdRes int checkedId) {
                        if (choiceContainer != null && actionButton != null) {
                            Timber.d("Mark radio button 1st time");
                            actionButton.setEnabled(true);
                            choiceContainer.setOnCheckedChangeListener(null);
                        }
                    }
                });
            }
        }
    }

    /**
     * @return false if at most 1 item is checked, true otherwise
     */
    private boolean checkOnEmptyChecked() {
        for (int i = 0; i < choiceContainer.getChildCount(); i++) {
            StepikOptionView view = (StepikOptionView) choiceContainer.getChildAt(i);
            if (view.isChecked()) {
                return false;
            }
        }

        // no one is checked
        actionButton.setEnabled(false);
        return true;
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

    @Subscribe
    public void onNewCommentWasAdded(NewCommentWasAddedOrUpdateEvent event) {
        super.onNewCommentWasAdded(event);

    }

    @Subscribe
    public void onStepWasUpdated(StepWasUpdatedEvent event) {
        super.onStepWasUpdated(event);
    }
}
