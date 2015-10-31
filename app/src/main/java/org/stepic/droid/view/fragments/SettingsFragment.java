package org.stepic.droid.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.events.wifi_settings.WifiLoadIsChangedEvent;
import org.stepic.droid.view.dialogs.AllowMobileDataDialogFragment;
import org.stepic.droid.view.dialogs.ClearCacheDialogFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SettingsFragment extends FragmentBase {
    @Bind(R.id.clear_cache_button)
    Button mClearCacheButton;

    @Bind(R.id.fragment_settings_wifi_enable_switch)
    SwitchCompat mWifiLoadSwitch;

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

        mWifiLoadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean newCheckedState) {
                if (newCheckedState) {
                    //wifi only
                    bus.post(new WifiLoadIsChangedEvent(false));
                } else {
                    //wifi and mobile internet
                    AllowMobileDataDialogFragment dialogFragment = new AllowMobileDataDialogFragment();
                    dialogFragment.show(getFragmentManager(), null);
                }

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        bus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        bus.unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Subscribe
    public void onWifiChanged(WifiLoadIsChangedEvent e) {
        mWifiLoadSwitch.setChecked(!e.isNewStateMobileAllowed());
        storeMobileState(e.isNewStateMobileAllowed());
    }

    private void storeMobileState(boolean isMobileAllowed) {
        mSharedPreferenceHelper.setMobileInternetAndWifiAllowed(isMobileAllowed);
    }
}
