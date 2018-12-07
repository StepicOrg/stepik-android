package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.stepic.droid.R;
import org.stepic.droid.base.StepBaseFragment;

import butterknife.BindView;

public class NotSupportedYetStepFragment extends StepBaseFragment {

    @BindView(R.id.open_web)
    Button openWebBtn;

    @BindView(R.id.stepContainer)
    ViewGroup stepContainer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_unsupported, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        openWebBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getScreenManager().openStepInWeb(getContext(), step);
            }
        });
    }

    @Override
    protected void attachStepTextWrapper() {
        stepTextWrapper.attach(stepContainer, true);
    }

    @Override
    protected void detachStepTextWrapper() {
        stepTextWrapper.detach(stepContainer);
    }
}
