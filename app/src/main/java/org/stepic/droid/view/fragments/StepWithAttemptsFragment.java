package org.stepic.droid.view.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.squareup.otto.Subscribe;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.StepBaseFragment;
import org.stepic.droid.events.attempts.FailAttemptEvent;
import org.stepic.droid.events.attempts.SuccessAttemptEvent;
import org.stepic.droid.events.submissions.FailGettingLastSubmissionEvent;
import org.stepic.droid.events.submissions.FailSubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SuccessGettingLastSubmissionEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Reply;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Submission;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.web.AttemptResponse;
import org.stepic.droid.web.SubmissionResponse;

import java.util.List;

import butterknife.BindDrawable;
import butterknife.BindString;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public abstract class StepWithAttemptsFragment extends StepBaseFragment {
    protected final int FIRST_DELAY = 1000;


    @BindString(R.string.correct)
    String mCorrectString;

    @BindString(R.string.wrong)
    String mWrongString;

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
        showLoadState(true);
        showAnswerField(false);
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


    /**
     * @return false if restore was failed;
     */
    protected final boolean tryRestoreState() {
        mAttempt = mLessonManager.restoreAttemptForStep(mStep.getId());
        mSubmission = mLessonManager.restoreSubmissionForStep(mStep.getId());
        if (mSubmission == null || mAttempt == null) return false;

        showAttempt(mAttempt);
        fillSubmission(mSubmission);
        return true;
    }

    protected final void getExistingAttempts() {
        mShell.getApi().getExistingAttempts(mStep.getId()).enqueue(new Callback<AttemptResponse>() {
            Step localStep = mStep;

            @Override
            public void onResponse(Response<AttemptResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {

                    AttemptResponse body = response.body();
                    if (body == null) {
                        createNewAttempt();
                        return;
                    }

                    List<Attempt> attemptList = body.getAttempts();
                    if (attemptList == null || attemptList.isEmpty() || !attemptList.get(0).getStatus().equals("active")) {
                        createNewAttempt();
                    } else {
                        Log.d(TAG, AppConstants.GET_OLD_ATTEMPT);
                        YandexMetrica.reportEvent(AppConstants.GET_OLD_ATTEMPT);
                        Attempt attempt = attemptList.get(0);
                        bus.post(new SuccessAttemptEvent(localStep.getId(), attempt));
                    }
                } else {
                    createNewAttempt();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                createNewAttempt();
            }
        });
    }


    protected final void createNewAttempt() {
        if (mStep == null) return;
        mShell.getApi().createNewAttempt(mStep.getId()).enqueue(new Callback<AttemptResponse>() {
            Step localStep = mStep;

            @Override
            public void onResponse(Response<AttemptResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<Attempt> attemptList = response.body().getAttempts();
                    if (attemptList != null && !attemptList.isEmpty()) {
                        Attempt attempt = attemptList.get(0);
                        bus.post(new SuccessAttemptEvent(localStep.getId(), attempt));
                    } else {
                        bus.post(new FailAttemptEvent(localStep.getId()));
                    }

                } else {
                    bus.post(new FailAttemptEvent(localStep.getId()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                bus.post(new FailAttemptEvent(localStep.getId()));
            }
        });
    }

    protected void makeSubmission() {
        if (mAttempt == null || mAttempt.getId() <= 0) return;
        blockUIBeforeSubmit(true);
        final long attemptId = mAttempt.getId();
        final Reply reply = generateReply();
        mShell.getApi().createNewSubmission(reply, attemptId).enqueue(new Callback<SubmissionResponse>() {
            @Override
            public void onResponse(Response<SubmissionResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    bus.post(new SubmissionCreatedEvent(attemptId, response.body()));
                } else {
                    bus.post(new FailSubmissionCreatedEvent(attemptId));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                bus.post(new FailSubmissionCreatedEvent(attemptId));
            }
        });

    }


    protected final void getStatusOfSubmission(long attemptId) {
        getStatusOfSubmission(attemptId, 0);
    }

    protected final void getStatusOfSubmission(final long attemptId, final int numberOfTry) {
        if (mHandler == null) return;
        mHandler.postDelayed(new Runnable() {
            long localAttemptId = attemptId;

            @Override
            public void run() {
                mShell.getApi().getSubmissions(localAttemptId).enqueue(new Callback<SubmissionResponse>() {
                    @Override
                    public void onResponse(Response<SubmissionResponse> response, Retrofit retrofit) {
                        if (response.isSuccess()) {
                            List<Submission> submissionList = response.body().getSubmissions();
                            if (submissionList == null || submissionList.isEmpty()) {
//                                bus.post(new FailGettingLastSubmissionEvent(localAttemptId, numberOfTry));
                                bus.post(new SuccessGettingLastSubmissionEvent(localAttemptId, null));
                                return;
                            }

                            Submission submission = submissionList.get(0);

                            if (submission.getStatus() == Submission.Status.EVALUATION) {
                                bus.post(new FailGettingLastSubmissionEvent(localAttemptId, numberOfTry));
                                return;
                            }

                            bus.post(new SuccessGettingLastSubmissionEvent(localAttemptId, submission));


                        } else {
                            bus.post(new FailGettingLastSubmissionEvent(localAttemptId, numberOfTry));
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        bus.post(new FailGettingLastSubmissionEvent(localAttemptId, numberOfTry));
                    }
                });
            }
        }, numberOfTry * FIRST_DELAY);
    }

    @Override
    public void onDestroyView() {
        saveSession();
        super.onDestroyView();
    }

    @Subscribe
    public void onSuccessCreateSubmission(SubmissionCreatedEvent e) {
        if (mAttempt == null || e.getAttemptId() != mAttempt.getId()) return;
        getStatusOfSubmission(mAttempt.getId());
    }

    @Subscribe
    public void onFailGettingSubmission(FailGettingLastSubmissionEvent e) {
        if (mAttempt == null || e.getAttemptId() != mAttempt.getId()) return;

        int nextTry = e.getTryNumber() + 1;

        getStatusOfSubmission(e.getAttemptId(), nextTry);
    }

    @Subscribe
    public void onGettingSubmission(SuccessGettingLastSubmissionEvent e) {
        if (mAttempt == null || e.getAttemptId() != mAttempt.getId()) return;
        if (e.getSubmission() == null || e.getSubmission().getStatus() == null) {
            showLoadState(false);
            return;
        }

        mSubmission = e.getSubmission();
        saveSession();
        fillSubmission(mSubmission);
    }

    protected final void fillSubmission(Submission submission) {
        if (submission == null || submission.getStatus() == null) {
            return;
        }

        switch (submission.getStatus()) {
            case CORRECT:
                onCorrectSubmission();
                setTextToActionButton(mTryAgainText);
                blockUIBeforeSubmit(true);
                break;
            case WRONG:
                onWrongSubmission();
                setTextToActionButton(mTryAgainText);
                blockUIBeforeSubmit(true);
                break;
        }

        onRestoreSubmission();
        showLoadState(false);
    }

    protected final void saveSession() {
        if (mAttempt == null) return;

        if (mSubmission == null) {
            Reply reply = generateReply();
            mSubmission = new Submission(reply, mAttempt.getId(), Submission.Status.LOCAL);
        }

        mLessonManager.saveSession(mStep.getId(), mAttempt, mSubmission);
    }

    protected abstract void onWrongSubmission();

    protected abstract void onCorrectSubmission();

    protected abstract void tryAgain();

    protected abstract void showAttempt(Attempt attempt);

    protected abstract void setTextToActionButton(String text);

    protected abstract void setListenerToActionButton(View.OnClickListener l);

    protected abstract void showLoadState(boolean isLoading);

    protected abstract Reply generateReply();

    protected abstract void blockUIBeforeSubmit(boolean needBlock);

    protected abstract void onRestoreSubmission();

    protected abstract void showAnswerField(boolean needShow);

    @Subscribe
    public abstract void onSuccessLoadAttempt(SuccessAttemptEvent e);

    @Subscribe
    public abstract void onFailCreateSubmission(FailSubmissionCreatedEvent event);

    @Subscribe
    public abstract void onFailCreateAttemptEvent(FailAttemptEvent event);


}
