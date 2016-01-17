package org.stepic.droid.view.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;

import org.stepic.droid.R;
import org.stepic.droid.base.StepBaseFragment;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Submission;

import butterknife.BindDrawable;
import butterknife.BindString;

public abstract class StepWithAttemptsFragment extends StepBaseFragment {
    protected final int FIRST_DELAY = 1000;


    @BindString(R.string.submit)
    protected String mSubmitText;

    @BindString(R.string.try_again)
    protected String mTryAgainText;

    protected Attempt mAttempt = null;
    protected Submission mSubmission = null;

    protected Handler mHandler;


    @BindDrawable(R.drawable.ic_correct)
    protected Drawable mCorrectIcon;

    @BindDrawable(R.drawable.ic_error)
    protected Drawable mWrongIcon;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler = new Handler();
        if (!tryRestoreState()) {
            getExistingAttempts();
        }

        if (mSubmission == null || mSubmission.getStatus() == Submission.Status.LOCAL) {
            setTextToActionButton(mSubmitText);
        } else {
            setTextToActionButton(mTryAgainText);
        }

        setListenerToActionButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadState(true);
                if (mSubmission == null || mSubmission.getStatus() == Submission.Status.LOCAL) {
                    makeSubmission();
                } else {
                    tryAgain();
                }
            }
        });
    }

    protected abstract void tryAgain();

    protected abstract void getExistingAttempts();

    protected abstract boolean tryRestoreState();

    protected abstract void setTextToActionButton(String text);

    protected abstract void setListenerToActionButton(View.OnClickListener l);

    protected abstract void showLoadState(boolean isLoading);

    protected abstract void makeSubmission();
}
