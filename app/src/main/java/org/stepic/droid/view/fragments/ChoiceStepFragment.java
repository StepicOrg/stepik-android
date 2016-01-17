package org.stepic.droid.view.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.events.attempts.FailAttemptEvent;
import org.stepic.droid.events.attempts.SuccessAttemptEvent;
import org.stepic.droid.events.submissions.FailGettingLastSubmissionEvent;
import org.stepic.droid.events.submissions.FailSubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SuccessGettingLastSubmissionEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Dataset;
import org.stepic.droid.model.Reply;
import org.stepic.droid.util.DpPixelsHelper;
import org.stepic.droid.util.HtmlHelper;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.RadioGroupHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChoiceStepFragment extends StepWithAttemptsFragment {

    @Bind(R.id.root_view)
    ViewGroup mRootView;

    @Bind(R.id.choice_container)
    RadioGroup mChoiceContainer;

    @Bind(R.id.submit_button)
    Button mActionButton;

    @Bind(R.id.result_line)
    View mResultLine;

    @Bind(R.id.answer_status_icon)
    ImageView mStatusIcon;

    @Bind(R.id.answer_status_text)
    TextView mStatusTextView;

    @Bind(R.id.progress_bar)
    ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_choice_step, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    protected void setTextToActionButton(String text) {
        mActionButton.setText(text);
    }

    @Override
    protected void setListenerToActionButton(View.OnClickListener l) {
        mActionButton.setOnClickListener(l);
    }

    @Override
    @Subscribe
    public void onSuccessLoadAttempt(SuccessAttemptEvent e) {
        if (mStep == null || e.getStepId() != mStep.getId() || e.getAttempt() == null) return;

        showAttempt(e.getAttempt());
        mAttempt = e.getAttempt();
    }


    /**
     * it is unique for each attempt type
     *
     * @param attempt data of task
     */
    @Override
    protected void showAttempt(Attempt attempt) {
        Dataset dataset = attempt.getDataset();
        if (dataset == null) return;

        List<String> options = dataset.getOptions();
        if (options == null || options.isEmpty()) return;

        mChoiceContainer.removeAllViews();

        for (String option : options) {
            CompoundButton optionViewItem;
            if (dataset.is_multiple_choice()) {
                optionViewItem = new AppCompatCheckBox(getActivity());
            } else {
                optionViewItem = new AppCompatRadioButton(getActivity());
            }
            buildChoiceItem(optionViewItem, option);
        }

        if (mLessonManager.restoreSubmissionForStep(mStep.getId()) == null) {
            getStatusOfSubmission(attempt.getId());//fill last server submission if exist
        }
    }

    @Subscribe
    @Override
    public void onFailCreateAttemptEvent(FailAttemptEvent event) {
        if (mStep == null || event.getStepId() != mStep.getId()) return;
        // TODO: 13.01.16 cancel progress bars
    }

    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {
        RadioGroupHelper.setEnabled(mChoiceContainer, !needBlock);
    }


    @Override
    protected void tryAgain() {
        blockUIBeforeSubmit(false);

        // FIXME: 17.01.16 refactor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mChoiceContainer.setBackground(mRootView.getBackground());
        } else {
            mChoiceContainer.setBackgroundDrawable(mRootView.getBackground());
        }

        createNewAttempt();
        mSubmission = null;

        mResultLine.setVisibility(View.GONE);
        showLoadState(false);
        mActionButton.setText(mSubmitText);
    }

    @Override
    protected Reply generateReply() {
        List<Boolean> options = new ArrayList<>();
        for (int i = 0; i < mChoiceContainer.getChildCount(); i++) {
            CompoundButton view = (CompoundButton) mChoiceContainer.getChildAt(i);
            options.add(view.isChecked());
        }
        return new Reply(options);
    }

    @Override
    @Subscribe
    public void onSuccessCreateSubmission(SubmissionCreatedEvent e) {
        super.onSuccessCreateSubmission(e);
    }

    @Override
    public void onFailCreateSubmission(FailSubmissionCreatedEvent event) {
    }

    @Subscribe
    public void onFailGettingSubmission(FailGettingLastSubmissionEvent e) {
        super.onFailGettingSubmission(e);
    }

    @Override
    @Subscribe
    public void onGettingSubmission(SuccessGettingLastSubmissionEvent e) {
        super.onGettingSubmission(e);
    }

    @Override
    protected void onRestoreSubmission() {
        Reply reply = mSubmission.getReply();
        if (reply == null) return;

        List<Boolean> choices = reply.getChoices();
        if (choices == null) return;

        for (int i = 0; i < mChoiceContainer.getChildCount(); i++) {
            CompoundButton view = (CompoundButton) mChoiceContainer.getChildAt(i);
            view.setChecked(choices.get(i));
        }
    }

    @Override
    protected void onWrongSubmission() {
        mChoiceContainer.setBackgroundResource(R.color.wrong_answer_background);
        mStatusIcon.setImageDrawable(mWrongIcon);
        mStatusTextView.setText(mWrongString);
        mResultLine.setBackgroundResource(R.color.wrong_answer_background);
        mResultLine.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCorrectSubmission() {
        mChoiceContainer.setBackgroundResource(R.color.correct_answer_background);
        mStatusIcon.setImageDrawable(mCorrectIcon);
        mStatusTextView.setText(mCorrectString);
        mResultLine.setBackgroundResource(R.color.correct_answer_background);
        mResultLine.setVisibility(View.VISIBLE);
    }

    @Override
    protected void showLoadState(boolean isLoading) {
        if (isLoading) {
            mActionButton.setVisibility(View.GONE);
            ProgressHelper.activate(mProgressBar);
        } else {
            ProgressHelper.dismiss(mProgressBar);
            mActionButton.setVisibility(View.VISIBLE);
        }

    }

    private void buildChoiceItem(CompoundButton item, String rawText) {
        int dp4 = (int) DpPixelsHelper.convertDpToPixel(4);
        item.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        item.setPadding(0, dp4, 0, dp4);
        String text = HtmlHelper.fromHtml(rawText).toString();
        item.setText(text);
        mChoiceContainer.addView(item);
    }
}
