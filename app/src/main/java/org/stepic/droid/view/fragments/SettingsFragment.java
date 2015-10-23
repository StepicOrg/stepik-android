package org.stepic.droid.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.view.dialogs.ClearCacheDialogFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SettingsFragment extends FragmentBase {
    @Bind(R.id.clear_cache_button)
    Button mClearCacheButton;

    private DialogFragment mClearCacheDialogFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mClearCacheDialogFragment = new ClearCacheDialogFragment();

        mClearCacheButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClearCacheDialogFragment.show(getFragmentManager(), null);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
