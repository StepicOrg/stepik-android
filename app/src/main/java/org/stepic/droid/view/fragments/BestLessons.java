package org.stepic.droid.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BestLessons extends FragmentBase {

    @Bind(R.id.root_view)
    FrameLayout root;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_best_lessons, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onDestroyView() {
        if (root != null) {
            root.setBackgroundColor(transparent);
        }
        super.onDestroyView();
    }
}
