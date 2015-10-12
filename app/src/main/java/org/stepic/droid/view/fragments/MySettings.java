package org.stepic.droid.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MySettings extends FragmentBase {

    @Bind(R.id.root_view)
    View root;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
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
