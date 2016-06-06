package org.stepic.droid.view.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.loading.FinishDeletingLoadEvent;
import org.stepic.droid.events.loading.StartDeletingLoadEvent;
import org.stepic.droid.events.wifi_settings.WifiLoadIsChangedEvent;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.FileUtil;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.view.custom.BetterSwitch;
import org.stepic.droid.view.custom.LoadingProgressDialog;
import org.stepic.droid.view.dialogs.AllowMobileDataDialogFragment;
import org.stepic.droid.view.dialogs.ClearVideosDialog;
import org.stepic.droid.view.dialogs.VideoQualityDialog;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class SettingsFragment extends FragmentBase {

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Bind(R.id.clear_cache_button)
    Button mClearCacheButton;

    @Bind(R.id.fragment_settings_wifi_enable_switch)
    BetterSwitch mWifiLoadSwitch;

    @Bind(R.id.fragment_settings_external_player_switch)
    BetterSwitch mExternalPlayerSwitch;

    @Bind(R.id.video_quality_view)
    View mVideoQuality;

    @Bind(R.id.version_tv)
    TextView mVersionTv;

    @Bind(R.id.fragment_settings_notification_learn_switch)
    BetterSwitch notificationLearnSwitch;

    @Bind(R.id.fragment_settings_notification_vibration_switch)
    BetterSwitch notificationVibration;

    @Bind(R.id.fragment_settings_notification_sound_switch)
    BetterSwitch notificationSound;

    @Bind(R.id.move_data_button)
    Button moveDataButton;

    @BindString(R.string.version)
    String versionPrefix;

    @BindString(R.string.clear_cache_title)
    String mClearCacheTitle;

    @BindString(R.string.kb)
    String kb;

    @BindString(R.string.mb)
    String mb;

    private ProgressDialog loadingProgressDialog;
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

        setUpClearCacheButton();

        setUpNotificationVibration();

        setUpNotificationLearn();

        setUpSound();

        setUpMoveButton();

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

        loadingProgressDialog = new LoadingProgressDialog(getContext());
    }

    private void setUpMoveButton() {
        moveDataButton.setVisibility(View.VISIBLE);
        Boolean isSDPresent = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) && Environment.isExternalStorageRemovable() ;

        if (isSDPresent) {
            moveDataButton.setText("Yeah, I have card");
        } else {
            moveDataButton.setText("Sorry =(");
        }

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

    private void setUpClearCacheButton() {
        mClearCacheDialogFragment = new ClearVideosDialog();
        mClearCacheButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YandexMetrica.reportEvent(AppConstants.METRICA_CLICK_CLEAR_CACHE);
                mClearCacheDialogFragment.show(getFragmentManager(), null);
            }
        });
        StringBuilder clearCacheButtonText = new StringBuilder();
        clearCacheButtonText.append(mClearCacheTitle);
        long size = FileUtil.getFileOrFolderSizeInKb(mUserPreferences.getUserDownloadFolder());
        if (size > 0) {
            mClearCacheButton.setEnabled(true);
            clearCacheButtonText.append(" ");
            clearCacheButtonText.append("(");
            if (size > 1024) {
                size = size / 1024;
                clearCacheButtonText.append(size);
                clearCacheButtonText.append(mb);
            } else {
                clearCacheButtonText.append(size);
                clearCacheButtonText.append(kb);
            }
            clearCacheButtonText.append(")");
        } else {
            mClearCacheButton.setEnabled(false);
        }
        mClearCacheButton.setText(clearCacheButtonText.toString());
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

    private void showVersionName() {
        try {
            Context mainAppContext = MainApplication.getAppContext();
            String versionName = mainAppContext.getPackageManager().getPackageInfo(mainAppContext.getPackageName(), 0).versionName;
            String textForVersionView = versionPrefix + ": " + versionName;
            mVersionTv.setText(textForVersionView);
        } catch (PackageManager.NameNotFoundException e) {
            YandexMetrica.reportError(AppConstants.NOT_FOUND_VERSION, e);
            e.printStackTrace();
            mVersionTv.setVisibility(View.GONE);
        } catch (Exception e) {
            YandexMetrica.reportError(AppConstants.NOT_SIGNIFICANT_ERROR, e);
            mVersionTv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
    }

    @Subscribe
    public void onStartDeleting(StartDeletingLoadEvent event) {
        ProgressHelper.activate(loadingProgressDialog);
    }

    @Subscribe
    public void onFinishDeleting(FinishDeletingLoadEvent event) {
        setUpClearCacheButton();
        ProgressHelper.dismiss(loadingProgressDialog);
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
