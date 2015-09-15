package org.stepic.droid.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.stepic.droid.R;
import org.stepic.droid.base.StepicBaseFragment;

import butterknife.ButterKnife;

public class AvailableCourses extends StepicBaseFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_available_lessons, container, false);
        ButterKnife.bind(this, v);
        return v;
    }
}
