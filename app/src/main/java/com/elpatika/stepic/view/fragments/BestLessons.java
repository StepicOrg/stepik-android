package com.elpatika.stepic.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elpatika.stepic.R;
import com.elpatika.stepic.base.StepicBaseFragment;

public class BestLessons extends StepicBaseFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_best_lessons,container,false);
        return v;
    }
}
