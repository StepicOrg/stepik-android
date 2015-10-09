package org.stepic.droid.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.base.StepFragmentBase;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VideoStepFragment extends StepFragmentBase {

    @Bind(R.id.test_tv)
    TextView testTv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_video_step, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        testTv.setText(getArguments().getString("test"));

    }
}
