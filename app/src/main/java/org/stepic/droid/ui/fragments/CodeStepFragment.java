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
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Reply;

import butterknife.BindString;
import butterknife.ButterKnife;

public class CodeStepFragment extends StepAttemptFragment {


    @BindString(R.string.correct)
    String correctString;

    EditText answerField;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup viewGroup = (ViewGroup) ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.code_attempt, attemptContainer, false);
        answerField = ButterKnife.findById(viewGroup, R.id.answer_edit_text);
        attemptContainer.addView(viewGroup);
    }

    @Override
    protected void showAttempt(Attempt attempt) {
        //do nothing, because this attempt doesn't have any specific.
        // TODO: 29.03.16 we need render code for showing
        answerField.getText().clear();
        answerField.setText(textResolver.fromHtml("#include <iostream> int main() { // put your code here return 0; }")); // TODO: 29.03.16  choose and after that get from step.block.options.code_templates
    }

    @Override
    protected Reply generateReply() {
        return new Reply.Builder()
                .setLanguage("c++11") // TODO: 29.03.16 choose and after that get from step.block.options.limits 
                .setCode(answerField.getText().toString())
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

        String text = reply.getCode();
        answerField.setText(textResolver.fromHtml(text)); // TODO: 29.03.16 render code
    }

    @Override
    protected String getCorrectString() {
        return correctString;
    }

    @Subscribe
    public void onNewCommentWasAdded(NewCommentWasAddedOrUpdateEvent event) {
        super.onNewCommentWasAdded(event);
    }

}
