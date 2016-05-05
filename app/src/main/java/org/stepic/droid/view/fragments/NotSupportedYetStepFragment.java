package org.stepic.droid.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.stepic.droid.R;
import org.stepic.droid.base.StepBaseFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NotSupportedYetStepFragment extends StepBaseFragment {

    @Bind(R.id.open_web)
    Button mOpenWebBtn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_unsupported, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mOpenWebBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShell.getScreenProvider().openStepInWeb(getContext(), step);
            }
        });
    }
}
