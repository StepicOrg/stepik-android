package org.stepic.droid.view.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

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
import org.stepic.droid.model.Dataset;
import org.stepic.droid.model.Reply;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Submission;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.DpPixelsHelper;
import org.stepic.droid.util.HtmlHelper;
import org.stepic.droid.web.AttemptResponse;
import org.stepic.droid.web.SubmissionResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class ChoiceStepFragment extends StepBaseFragment {

    private final int FIRST_DELAY = 1000;
    private final String TAG = "ChoiceStepFragment";

    @Bind(R.id.choice_container)
    RadioGroup mChoiceContainer;

    @Bind(R.id.submit_button)
    Button mSubmitButton;

    @Bind(R.id.result_line)
    View mResultLine;

    @Bind(R.id.answer_status_icon)
    ImageView mStatusIcon;

    @Bind(R.id.answer_status_text)
    TextView mStatusTextView;

    @BindDrawable(R.drawable.ic_correct)
    Drawable mCorrectIcon;

    @BindDrawable(R.drawable.ic_error)
    Drawable mWrongIcon;

    @BindString(R.string.correct)
    String mCorrectString;

    @BindString(R.string.wrong)
    String mWrongString;


    Handler mHandler;

    private Attempt mAttempt = null; // TODO: 13.01.16 save when orientation is changed, not load from web
    private long mAttemptId; //Todo: config changes

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_choice_step, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler = new Handler();
        getExistingAttempts();
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeSubmission();
            }
        });
    }

    private void getExistingAttempts() {
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

    private void createNewAttempt() {
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

    @Subscribe
    public void onSuccessLoadAttempt(SuccessAttemptEvent e) {
        if (mStep == null || e.getStepId() != mStep.getId() || e.getAttempt() == null) return;

        Dataset dataset = e.getAttempt().getDataset();
        if (dataset == null) return;

        List<String> options = dataset.getOptions();
        if (options == null || options.isEmpty()) return;

        for (String option : options) {
            CompoundButton optionViewItem;
            if (dataset.is_multiple_choice()) {
                optionViewItem = new AppCompatCheckBox(getActivity());
            } else {
                optionViewItem = new AppCompatRadioButton(getActivity());
            }
            buildChoiceItem(optionViewItem, option);
        }
        mAttemptId = e.getAttempt().getId();
    }

    @Subscribe
    public void onFailLoadAttempt(FailAttemptEvent e) {
        if (mStep == null || e.getStepId() != mStep.getId()) return;
        // TODO: 13.01.16 cancel progress bars

    }

    private void buildChoiceItem(CompoundButton item, String rawText) {
        int dp4 = (int) DpPixelsHelper.convertDpToPixel(4);
        item.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        item.setPadding(0, dp4, 0, dp4);
        String text = HtmlHelper.fromHtml(rawText).toString();
        item.setText(text);
        mChoiceContainer.addView(item);
    }

    private void makeSubmission() {
        if (mAttemptId <= 0) return;
        Reply reply = generateReplyFromSelected();
        mShell.getApi().createNewSubmission(reply, mAttemptId).enqueue(new Callback<SubmissionResponse>() {
            @Override
            public void onResponse(Response<SubmissionResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    bus.post(new SubmissionCreatedEvent(mAttemptId, response.body()));
                } else {
                    bus.post(new FailSubmissionCreatedEvent(mAttemptId));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                bus.post(new FailSubmissionCreatedEvent(mAttemptId));
            }
        });

    }

    private Reply generateReplyFromSelected() {
        List<Boolean> options = new ArrayList<>();
        for (int i = 0; i < mChoiceContainer.getChildCount(); i++) {
            CompoundButton view = (CompoundButton) mChoiceContainer.getChildAt(i);
            options.add(view.isChecked());
        }
        return new Reply(options);
    }

    @Subscribe
    public void onSuccessCreateSubmission(SubmissionCreatedEvent e) {
        if (e.getAttemptId() != mAttemptId) return;
        getStatusOfSubmission(mAttemptId);

        // TODO: 14.01.16 view progress bar
    }

    private void getStatusOfSubmission(final long attemptId, final int numberOfTry) {
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
                                bus.post(new FailGettingLastSubmissionEvent(localAttemptId, numberOfTry));
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

    private void getStatusOfSubmission(long attemptId) {
        getStatusOfSubmission(attemptId, 0);
    }

    @Subscribe
    public void onFailGettingSubmission(FailGettingLastSubmissionEvent e) {
        if (e.getAttemptId() != mAttemptId) return;

        int nextTry = e.getTryNumber() + 1;

        getStatusOfSubmission(e.getAttemptId(), nextTry);
    }

    @Subscribe
    public void onSuccessGEttingSubmissionResilt(SuccessGettingLastSubmissionEvent e) {
        if (e.getAttemptId() != mAttemptId) return;
        if (e.getSubmission() == null || e.getSubmission().getStatus() == null) return;

        switch (e.getSubmission().getStatus()) {
            case CORRECT:
                onCorrectSubmission();
                break;
            case WRONG:
                onWrongSubmission();
                break;
        }
    }


    // todo make 3 methods: add 1 generic
    private void onWrongSubmission() {
        mChoiceContainer.setBackgroundResource(R.color.wrong_answer_background);
        mStatusIcon.setImageDrawable(mWrongIcon);
        mStatusTextView.setText(mWrongString);
        mResultLine.setBackgroundResource(R.color.wrong_answer_background);
        mResultLine.setVisibility(View.VISIBLE);

        //// TODO: 14.01.16 add/chahnge to 'try again' button
    }

    private void onCorrectSubmission() {
        mChoiceContainer.setBackgroundResource(R.color.correct_answer_background);
        mStatusIcon.setImageDrawable(mCorrectIcon);
        mStatusTextView.setText(mCorrectString);
        mResultLine.setBackgroundResource(R.color.correct_answer_background);
        mResultLine.setVisibility(View.VISIBLE);
        mSubmitButton.setVisibility(View.GONE);
    }
}
