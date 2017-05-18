package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.notifications.model.NotificationType;
import org.stepic.droid.ui.custom.BetterSwitch;
import org.stepic.droid.ui.dialogs.AllowMobileDataDialogFragment;
import org.stepic.droid.ui.dialogs.VideoQualityDialog;

import butterknife.BindString;
import butterknife.BindView;

public class SettingsFragment extends FragmentBase implements AllowMobileDataDialogFragment.Callback {

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @BindView(R.id.fragment_settings_wifi_enable_switch)
    BetterSwitch wifiLoadSwitch;

    @BindView(R.id.fragment_settings_external_player_switch)
    BetterSwitch externalPlayerSwitch;

    @BindView(R.id.video_quality_view)
    View videoQuality;

    @BindView(R.id.fragment_settings_notification_learn_switch)
    BetterSwitch notificationLearnSwitch;

    @BindView(R.id.fragment_settings_notification_comment_switch)
    BetterSwitch notificationCommentSwitch;

    @BindView(R.id.fragment_settings_notification_review_switch)
    BetterSwitch notificationReviewSwitch;

    @BindView(R.id.fragment_settings_notification_teaching_switch)
    BetterSwitch notificationTeachingSwitch;

    @BindView(R.id.fragment_settings_notification_other_switch)
    BetterSwitch notificationOtherSwitch;


    @BindView(R.id.fragment_settings_notification_vibration_switch)
    BetterSwitch notificationVibration;

    @BindView(R.id.fragment_settings_notification_sound_switch)
    BetterSwitch notificationSound;

    @BindView(R.id.fragment_settings_calendar_widget_switch)
    BetterSwitch calendarWidgetSwitch;

    @BindView(R.id.fragment_settings_keep_screen_on_switch)
    BetterSwitch keepScreenOnSwitch;

    @BindView(R.id.storage_management_button)
    View storageManagementButton;

    @BindView(R.id.fragment_settings_discounting_policy_switch)
    BetterSwitch discountingPolicySwitch;

    @BindString(R.string.version)
    String versionPrefix;

    @BindString(R.string.clear_cache_title)
    String clearCacheTitle;

    @BindString(R.string.kb)
    String kb;

    @BindString(R.string.mb)
    String mb;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpNotificationVibration();

        setUpNotifications();

        setUpSound();

        wifiLoadSwitch.setChecked(!sharedPreferenceHelper.isMobileInternetAlsoAllowed());//if first time it is true

        externalPlayerSwitch.setChecked(userPreferences.isOpenInExternal());

        externalPlayerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userPreferences.setOpenInExternal(isChecked);
            }
        });

        calendarWidgetSwitch.setChecked(userPreferences.isNeedToShowCalendarWidget());

        calendarWidgetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                userPreferences.setNeedToShowCalendarWidget(isChecked);
            }
        });

        keepScreenOnSwitch.setChecked(userPreferences.isKeepScreenOnSteps());
        keepScreenOnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userPreferences.setKeepScreenOnSteps(isChecked);
            }
        });

        discountingPolicySwitch.setChecked(userPreferences.isShowDiscountingPolicyWarning());

        discountingPolicySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userPreferences.setShowDiscountingPolicyWarning(isChecked);
            }
        });


        wifiLoadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean newCheckedState) {
                if (wifiLoadSwitch.isUserTriggered()) {
                    if (newCheckedState) {
                        //wifi only
                        onMobileDataStateChanged(false);
                    } else {
                        //wifi and mobile internet
                        wifiLoadSwitch.setChecked(true);
                        AllowMobileDataDialogFragment dialogFragment = AllowMobileDataDialogFragment.Companion.newInstance();
                        dialogFragment.setTargetFragment(SettingsFragment.this, 0);
                        if (!dialogFragment.isAdded()) {
                            dialogFragment.show(getFragmentManager(), null);
                        }
                    }

                }
            }
        });


        final DialogFragment videoDialog = new VideoQualityDialog();
        videoQuality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!videoDialog.isAdded()) {
                    videoDialog.show(getFragmentManager(), null);
                }
            }
        });

        storageManagementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenManager.showStorageManagement(getActivity());
            }
        });

    }

    private void setUpNotificationVibration() {
        notificationVibration.setChecked(userPreferences.isVibrateNotificationEnabled());
        notificationVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userPreferences.setVibrateNotificationEnabled(isChecked);
            }
        });
    }

    private void setUpSound() {
        notificationSound.setChecked(userPreferences.isSoundNotificationEnabled());
        notificationSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userPreferences.setNotificationSoundEnabled(isChecked);
            }
        });
    }

    @Override
    public void onDestroyView() {
        keepScreenOnSwitch.setOnCheckedChangeListener(null);
        discountingPolicySwitch.setOnCheckedChangeListener(null);
        calendarWidgetSwitch.setOnCheckedChangeListener(null);
        wifiLoadSwitch.setOnCheckedChangeListener(null);
        externalPlayerSwitch.setOnCheckedChangeListener(null);
        notificationLearnSwitch.setOnCheckedChangeListener(null);
        notificationCommentSwitch.setOnCheckedChangeListener(null);
        notificationTeachingSwitch.setOnCheckedChangeListener(null);
        notificationOtherSwitch.setOnCheckedChangeListener(null);
        notificationReviewSwitch.setOnCheckedChangeListener(null);
        notificationVibration.setOnCheckedChangeListener(null);
        notificationSound.setOnCheckedChangeListener(null);
        storageManagementButton.setOnClickListener(null);
        super.onDestroyView();
    }

    private void storeMobileState(boolean isMobileAllowed) {
        sharedPreferenceHelper.setMobileInternetAndWifiAllowed(isMobileAllowed);
    }

    private void setUpNotifications() {
        notificationLearnSwitch.setChecked(userPreferences.isNotificationEnabled(NotificationType.learn));
        notificationLearnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userPreferences.setNotificationEnabled(NotificationType.learn, isChecked);
            }
        });

        notificationCommentSwitch.setChecked(userPreferences.isNotificationEnabled(NotificationType.comments));
        notificationCommentSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userPreferences.setNotificationEnabled(NotificationType.comments, isChecked);
            }
        });

        notificationReviewSwitch.setChecked(userPreferences.isNotificationEnabled(NotificationType.review));
        notificationReviewSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userPreferences.setNotificationEnabled(NotificationType.review, isChecked);
            }
        });

        notificationTeachingSwitch.setChecked(userPreferences.isNotificationEnabled(NotificationType.teach));
        notificationTeachingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userPreferences.setNotificationEnabled(NotificationType.teach, isChecked);
            }
        });

        notificationOtherSwitch.setChecked(userPreferences.isNotificationEnabled(NotificationType.other));
        notificationOtherSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userPreferences.setNotificationEnabled(NotificationType.other, isChecked);
            }
        });

    }

    @Override
    public void onMobileDataStateChanged(boolean isMobileAllowed) {
        wifiLoadSwitch.setChecked(!isMobileAllowed);
        storeMobileState(isMobileAllowed);
    }
}
