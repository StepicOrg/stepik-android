package org.stepic.droid.ui.fragments;

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
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.model.Attachment;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Reply;

import java.util.ArrayList;

import butterknife.BindString;

public class FreeResponseStepFragment extends StepAttemptFragment {

    @BindString(R.string.correct_free_response)
    String correctString;

    EditText answerField;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        answerField = (EditText) ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_free_answer_attempt, attemptContainer, false);
        attemptContainer.addView(answerField);
        return v;
    }

    @Override
    protected void showAttempt(Attempt attempt) {
        //do nothing, because this attempt doesn't have any specific.
        answerField.getText().clear();
    }

    @Override
    protected Reply generateReply() {
        String answer = answerField.getText().toString();
        if (attempt != null && attempt.getDataset() != null && attempt.getDataset().getIs_html_enabled() != null && attempt.getDataset().getIs_html_enabled()) {
            answer = textResolver.replaceWhitespaceToBr(answer);
        }


        return new Reply.Builder()
                .setText(answer)
                .setAttachments(new ArrayList<Attachment>())
                .build();
    }

    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {
        answerField.setEnabled(!needBlock);
    }

    @Override
    protected void onRestoreSubmission() {
        Reply reply = submission.getReply();
        if (reply == null) return;

        String text = reply.getText();
        if (attempt != null && attempt.getDataset() != null && attempt.getDataset().getIs_html_enabled() != null && attempt.getDataset().getIs_html_enabled()) {
            //todo show as html in enhanced latexview
            answerField.setText(textResolver.fromHtml(text));
        } else {
            answerField.setText(text);
        }
    }

    @Override
    protected String getCorrectString() {
        return correctString;
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

    @Override
    public void onPause() {
        super.onPause();
        answerField.clearFocus();
    }
}
