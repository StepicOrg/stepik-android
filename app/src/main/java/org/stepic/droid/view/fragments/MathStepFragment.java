package org.stepic.droid.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.stepic.droid.events.InternetIsEnabledEvent;
import org.stepic.droid.events.attempts.FailAttemptEvent;
import org.stepic.droid.events.attempts.SuccessAttemptEvent;
import org.stepic.droid.events.submissions.FailGettingLastSubmissionEvent;
import org.stepic.droid.events.submissions.FailSubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SuccessGettingLastSubmissionEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Reply;

public class MathStepFragment extends StepWithAttemptsFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void showAttempt(Attempt attempt) {

    }

    @Override
    protected Reply generateReply() {
        return null;
    }

    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {

    }

    @Override
    protected void onRestoreSubmission() {

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
}
