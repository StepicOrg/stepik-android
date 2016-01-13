package org.stepic.droid.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.StepBaseFragment;
import org.stepic.droid.events.attemptions.FailAttemptEvent;
import org.stepic.droid.events.attemptions.SuccessAttemptEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Dataset;
import org.stepic.droid.model.Step;
import org.stepic.droid.util.DpPixelsHelper;
import org.stepic.droid.util.HtmlHelper;
import org.stepic.droid.web.AttemptResponse;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class ChoiceStepFragment extends StepBaseFragment {

    @Bind(R.id.choice_container)
    ViewGroup mChoiceContainer;


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
            builtChoiceItem(optionViewItem, option);
        }
    }

    @Subscribe
    public void onFailLoadAttempt(FailAttemptEvent e) {
        if (mStep == null || e.getStepId() != mStep.getId()) return;
        // TODO: 13.01.16 cancel progress bars

    }

    private void builtChoiceItem(CompoundButton item, String rawText) {
        int dp4 = (int) DpPixelsHelper.convertDpToPixel(4);
        item.setPadding(0, dp4, 0, dp4);
        String text = HtmlHelper.fromHtml(rawText).toString();
        item.setText(text);
        mChoiceContainer.addView(item);
    }


}
