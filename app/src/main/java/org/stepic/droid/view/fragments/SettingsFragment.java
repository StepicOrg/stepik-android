package org.stepic.droid.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.events.wifi_settings.WifiLoadIsChangedEvent;
import org.stepic.droid.view.custom.BetterSwitch;
import org.stepic.droid.view.dialogs.AllowMobileDataDialogFragment;
import org.stepic.droid.view.dialogs.VideoQualityDialog;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class SettingsFragment extends FragmentBase {

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Bind(R.id.fragment_settings_wifi_enable_switch)
    BetterSwitch mWifiLoadSwitch;

    @Bind(R.id.fragment_settings_external_player_switch)
    BetterSwitch mExternalPlayerSwitch;

    @Bind(R.id.video_quality_view)
    View mVideoQuality;

    @Bind(R.id.fragment_settings_notification_learn_switch)
    BetterSwitch notificationLearnSwitch;

    @Bind(R.id.fragment_settings_notification_vibration_switch)
    BetterSwitch notificationVibration;

    @Bind(R.id.fragment_settings_notification_sound_switch)
    BetterSwitch notificationSound;

    @Bind(R.id.storage_management_button)
    View storageManagementButton;

    @BindString(R.string.version)
    String versionPrefix;

    @BindString(R.string.clear_cache_title)
    String mClearCacheTitle;

    @BindString(R.string.kb)
    String kb;

    @BindString(R.string.mb)
    String mb;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpNotificationVibration();

        setUpNotificationLearn();

        setUpSound();

        mWifiLoadSwitch.setChecked(!mSharedPreferenceHelper.isMobileInternetAlsoAllowed());//if first time it is true

        mExternalPlayerSwitch.setChecked(mUserPreferences.isOpenInExternal());

        mExternalPlayerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mUserPreferences.setOpenInExternal(isChecked);
            }
        });


        mWifiLoadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean newCheckedState) {
                if (mWifiLoadSwitch.isUserTriggered()) {
                    if (newCheckedState) {
                        //wifi only
                        bus.post(new WifiLoadIsChangedEvent(false));
                    } else {
                        //wifi and mobile internet
                        mWifiLoadSwitch.setChecked(true);
                        AllowMobileDataDialogFragment dialogFragment = new AllowMobileDataDialogFragment();
                        dialogFragment.show(getFragmentManager(), null);
                    }

                }
            }
        });


        final DialogFragment videoDialog = new VideoQualityDialog();
        mVideoQuality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoDialog.show(getFragmentManager(), null);
            }
        });

        storageManagementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShell.getScreenProvider().showStorageManagement(getActivity());
            }
        });

    }

    private void setUpNotificationVibration() {
        notificationVibration.setChecked(mUserPreferences.isVibrateNotificationEnabled());
        notificationVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mUserPreferences.setVibrateNotificationEnabled(isChecked);
            }
        });
    }

    private void setUpSound() {
        notificationSound.setChecked(mUserPreferences.isSoundNotificationEnabled());
        notificationSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mUserPreferences.setNotificationSoundEnabled(isChecked);
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
        mWifiLoadSwitch.setOnCheckedChangeListener(null);
        mExternalPlayerSwitch.setOnCheckedChangeListener(null);
        notificationLearnSwitch.setOnCheckedChangeListener(null);
        notificationVibration.setOnCheckedChangeListener(null);
        notificationSound.setOnCheckedChangeListener(null);
        storageManagementButton.setOnClickListener(null);
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

    private void setUpNotificationLearn() {
        notificationLearnSwitch.setChecked(mUserPreferences.isNotificationEnabled());
        notificationLearnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mUserPreferences.setNotificationEnabled(isChecked);
            }
        });
    }
}
