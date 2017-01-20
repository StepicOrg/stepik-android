package org.stepic.droid.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.events.InternetIsEnabledEvent;
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.FillBlankComponent;
import org.stepic.droid.model.Reply;

import java.util.ArrayList;
import java.util.List;

public class FillBlanksFragment extends StepAttemptFragment {


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View fillBlanksView = ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_fill_blanks, attemptContainer, false);
        attemptContainer.addView(fillBlanksView);
    }

    @Override
    protected void showAttempt(Attempt attempt) {
        List<FillBlankComponent> list = attempt.getDataset().getFillBlankComponents();
        //// TODO: 20.01.17   show components from reply

    }

    @Override
    protected Reply generateReply() {
        //// TODO: 20.01.17 make it from user changing
        List<String> blanks = new ArrayList<>();
        blanks.add("First one");
        blanks.add("Second");
        blanks.add("    etc");
        return new Reply.Builder()
                .setBlanks(blanks)
                .build();
    }

    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {
        //// TODO: 20.01.17   block UI
    }

    @Override
    protected void onRestoreSubmission() {
        Reply reply = submission.getReply();
        //// TODO: 20.01.17   fill blanks from reply
        reply.getBlanks();
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
