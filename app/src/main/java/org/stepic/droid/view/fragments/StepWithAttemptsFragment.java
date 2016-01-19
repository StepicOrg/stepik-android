package org.stepic.droid.view.fragments;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.StepBaseFragment;
import org.stepic.droid.events.InternetIsEnabledEvent;
import org.stepic.droid.events.attempts.FailAttemptEvent;
import org.stepic.droid.events.attempts.SuccessAttemptEvent;
import org.stepic.droid.events.steps.UpdateStepEvent;
import org.stepic.droid.events.submissions.FailGettingLastSubmissionEvent;
import org.stepic.droid.events.submissions.FailSubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SuccessGettingLastSubmissionEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Reply;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Submission;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.web.AttemptResponse;
import org.stepic.droid.web.SubmissionResponse;

import java.util.List;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public abstract class StepWithAttemptsFragment extends StepBaseFragment {
    protected final int FIRST_DELAY = 1000;

    @Bind(R.id.root_view)
    ViewGroup mRootView;

    @Bind(R.id.result_line)
    View mResultLine;

    @Bind(R.id.answer_status_icon)
    ImageView mStatusIcon;

    @Bind(R.id.answer_status_text)
    TextView mStatusTextView;

    @Bind(R.id.progress_bar)
    ProgressBar mProgressBar;

    @Bind(R.id.report_problem)
    View connectionProblem;

    @Bind(R.id.attempt_container)
    ViewGroup mAttemptContainer;

    @Bind(R.id.submit_button)
    Button mActionButton;

    @BindString(R.string.correct)
    String mCorrectString;

    @BindString(R.string.wrong)
    protected String mWrongString;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_with_solution_base, container, false);
        ButterKnife.bind(this, v);
        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler = new Handler();
        init();
    }

    private void init() {
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
                        //it is dirty swap of events:
                        bus.post(new FailSubmissionCreatedEvent(localAttemptId));
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

    protected final void showOnlyInternetProblem(boolean isNeedShow) {
        showLoadState(!isNeedShow);
        if (isNeedShow) {
            //// FIXME: 17.01.16 it is bad way, because this class don't know about the button
            mActionButton.setVisibility(View.GONE);
        } else {
            mActionButton.setVisibility(View.VISIBLE);
        }
        showAnswerField(!isNeedShow);
        enableInternetMessage(isNeedShow);
    }

    private void setTextToActionButton(String text) {
        mActionButton.setText(text);
    }

    protected final void onWrongSubmission() {
        mAttemptContainer.setBackgroundResource(R.color.wrong_answer_background);
        mStatusIcon.setImageDrawable(mWrongIcon);
        mStatusTextView.setText(mWrongString);
        mResultLine.setBackgroundResource(R.color.wrong_answer_background);
        mResultLine.setVisibility(View.VISIBLE);
    }

    protected final void onCorrectSubmission() {
        markLocalProgressAsViewed();
        mAttemptContainer.setBackgroundResource(R.color.correct_answer_background);
        mStatusIcon.setImageDrawable(mCorrectIcon);
        mStatusTextView.setText(mCorrectString);
        mResultLine.setBackgroundResource(R.color.correct_answer_background);
        mResultLine.setVisibility(View.VISIBLE);
    }


    protected final void tryAgain() {
        blockUIBeforeSubmit(false);

        // FIXME: 17.01.16 refactor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mAttemptContainer.setBackground(mRootView.getBackground());
        } else {
            mAttemptContainer.setBackgroundDrawable(mRootView.getBackground());
        }

        createNewAttempt();
        mSubmission = null;

        mResultLine.setVisibility(View.GONE);
        mActionButton.setText(mSubmitText);
    }


    private void setListenerToActionButton(View.OnClickListener l) {
        mActionButton.setOnClickListener(l);
    }


    protected final void markLocalProgressAsViewed() {
        bus.post(new UpdateStepEvent(mStep.getId()));
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            long stepId = mStep.getId();

            protected Void doInBackground(Void... params) {
                long assignmentId = mDatabaseManager.getAssignmentIdByStepId(stepId);
                mDatabaseManager.markProgressAsPassed(assignmentId);
                return null;
            }
        };
        task.execute();
    }

    protected final void enableInternetMessage(boolean needShow) {
        if (needShow) {
            connectionProblem.setVisibility(View.VISIBLE);
        } else {
            connectionProblem.setVisibility(View.GONE);
        }
    }


    protected final void showAnswerField(boolean needShow) {
        if (needShow) {
            mAttemptContainer.setVisibility(View.VISIBLE);
        } else {
            mAttemptContainer.setVisibility(View.GONE);
        }
    }


    protected void showLoadState(boolean isLoading) {
        if (isLoading) {
            mActionButton.setVisibility(View.GONE);
            ProgressHelper.activate(mProgressBar);
        } else {
            ProgressHelper.dismiss(mProgressBar);
            mActionButton.setVisibility(View.VISIBLE);
            showAnswerField(true);
        }

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

    @Subscribe
    public void onInternetEnabled(InternetIsEnabledEvent enabledEvent) {
        enableInternetMessage(false);
        init();
    }

    @Subscribe
    public void onSuccessLoadAttempt(SuccessAttemptEvent e) {
        if (mStep == null || e.getStepId() != mStep.getId() || e.getAttempt() == null) return;

        showAttempt(e.getAttempt());
        mAttempt = e.getAttempt();
    }

    @Subscribe
    public void onFailCreateAttemptEvent(FailAttemptEvent event) {
        if (mStep == null || event.getStepId() != mStep.getId()) return;
        showOnlyInternetProblem(true);
    }

    @Subscribe
    public void onFailCreateSubmission(FailSubmissionCreatedEvent event) {
        if (mAttempt == null || event.getAttemptId() != mAttempt.getId()) return;
        showOnlyInternetProblem(true);
    }

    protected abstract void showAttempt(Attempt attempt);

    protected abstract Reply generateReply();

    protected abstract void blockUIBeforeSubmit(boolean needBlock);

    protected abstract void onRestoreSubmission();

}