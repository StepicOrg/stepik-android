package org.stepic.droid.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.events.InternetIsEnabledEvent;
import org.stepic.droid.events.attempts.FailAttemptEvent;
import org.stepic.droid.events.attempts.SuccessAttemptEvent;
import org.stepic.droid.events.comments.NewCommentWasAdded;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.events.submissions.FailGettingLastSubmissionEvent;
import org.stepic.droid.events.submissions.FailSubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SuccessGettingLastSubmissionEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Reply;
import org.stepic.droid.util.HtmlHelper;

import butterknife.BindString;
import butterknife.ButterKnife;

public class CodeStepFragment extends StepWithAttemptsFragment{


    @BindString(R.string.correct)
    String mCorrectString;

    EditText mAnswerField;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup viewGroup = (ViewGroup) ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.code_attempt, mAttemptContainer, false);
        mAnswerField = ButterKnife.findById(viewGroup, R.id.answer_edit_text);
        mAttemptContainer.addView(viewGroup);
        return v;
    }

    @Override
    protected void showAttempt(Attempt attempt) {
        //do nothing, because this attempt doesn't have any specific.
        // TODO: 29.03.16 we need render code for showing
        mAnswerField.getText().clear();
        mAnswerField.setText(HtmlHelper.fromHtml("#include <iostream> int main() { // put your code here return 0; }")); // TODO: 29.03.16  choose and after that get from step.block.options.code_templates
    }

    @Override
    protected Reply generateReply() {
        return new Reply.Builder()
                .setLanguage("c++11") // TODO: 29.03.16 choose and after that get from step.block.options.limits 
                .setCode(mAnswerField.getText().toString())
                .build();
    }

    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {
        mAnswerField.setEnabled(!needBlock);
    }

    @Override
    protected void onRestoreSubmission() {
        Reply reply = mSubmission.getReply();
        if (reply == null) return;

        String text = reply.getCode();
        mAnswerField.setText(HtmlHelper.fromHtml(text)); // TODO: 29.03.16 render code
    }

    @Override
    protected String getCorrectString() {
        return mCorrectString;
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
    public void onNewCommentWasAdded(NewCommentWasAdded event) {
        super.onNewCommentWasAdded(event);

    }

    @Subscribe
    public void onStepWasUpdated(StepWasUpdatedEvent event) {
        super.onStepWasUpdated(event);
    }

}
