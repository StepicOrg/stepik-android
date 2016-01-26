package org.stepic.droid.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.events.InternetIsEnabledEvent;
import org.stepic.droid.events.attempts.FailAttemptEvent;
import org.stepic.droid.events.attempts.SuccessAttemptEvent;
import org.stepic.droid.events.submissions.FailGettingLastSubmissionEvent;
import org.stepic.droid.events.submissions.FailSubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SuccessGettingLastSubmissionEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Reply;
import org.stepic.droid.util.HtmlHelper;

import butterknife.BindString;
import butterknife.ButterKnife;

public class PyCharmStepFragment extends StepWithAttemptsFragment {

    private TextView mMessageField;

    @BindString(R.string.py_message)
    String mPyMessage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mMessageField = (TextView) ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_py_step, mAttemptContainer, false);
        mMessageField.setMovementMethod(LinkMovementMethod.getInstance());
        mAttemptContainer.addView(mMessageField);
        ButterKnife.bind(this, v);
        mActionButton.setVisibility(View.GONE);
        return v;
    }

    @Override
    protected void showAttempt(Attempt attempt) {
        mMessageField.setText(HtmlHelper.fromHtml(mPyMessage));
    }

    @Override
    protected Reply generateReply() {
        return new Reply.Builder()
                .build();
    }

    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {
        mMessageField.setEnabled(true);
    }

    @Override
    protected void onRestoreSubmission() {
        mMessageField.setText(HtmlHelper.fromHtml(mPyMessage));
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
