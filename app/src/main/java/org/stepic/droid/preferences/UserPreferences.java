package org.stepic.droid.preferences;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.di.AppSingleton;
import org.stepic.droid.notifications.model.NotificationType;
import org.stepic.droid.persistence.model.StorageLocation;
import org.stepik.android.model.user.EmailAddress;
import org.stepik.android.model.user.Profile;

import java.util.List;

import javax.inject.Inject;

@AppSingleton
public class UserPreferences {

    private final SharedPreferenceHelper sharedPreferenceHelper;
    private final Analytic analytic;


    @Inject
    public UserPreferences(SharedPreferenceHelper helper, Analytic analytic) {
        this.sharedPreferenceHelper = helper;
        this.analytic = analytic;
    }

    public long getUserId() {
        Profile userProfile = sharedPreferenceHelper.getProfile();
        long userId = -1; // default anonymous user id
        if (userProfile != null) {
            userId = userProfile.getId();
        }
        return userId;
    }

    @Nullable
    public List<EmailAddress> getUserEmails() {
        return sharedPreferenceHelper.getStoredEmails();
    }

    @Nullable
    public EmailAddress getPrimaryEmail() {
        List<EmailAddress> emails = getUserEmails();
        if (emails == null || emails.isEmpty()) return null;
        if (emails.size() == 1) return emails.get(0);

        //emails >1
        EmailAddress primary = null;
        for (EmailAddress item : emails) {
            if (item != null && item.isPrimary()) {
                primary = item;
                break;
            }
        }
        if (primary == null) {
            primary = emails.get(0);
        }
        return primary;
    }

    public boolean isNetworkMobileAllowed() {
        return sharedPreferenceHelper.isMobileInternetAlsoAllowed();
    }


    public void saveVideoQualityForPlaying(String videoQuality) {
        sharedPreferenceHelper.saveVideoQualityForPlaying(videoQuality);
    }

    public String getQualityVideoForPlaying () {
        return sharedPreferenceHelper.getVideoQualityForPlaying();
    }

    public String getQualityVideo() {
        return sharedPreferenceHelper.getVideoQuality();
    }

    public void storeQualityVideo(String videoQuality) {
        sharedPreferenceHelper.storeVideoQuality(videoQuality);
    }

    public VideoPlaybackRate getVideoPlaybackRate() {
        return sharedPreferenceHelper.getVideoPlaybackRate();
    }

    public void setVideoPlaybackRate(VideoPlaybackRate rate) {
        sharedPreferenceHelper.storeVideoPlaybackRate(rate);
    }

    public boolean isOpenInExternal() {
        return sharedPreferenceHelper.isOpenInExternal();
    }

    public void setOpenInExternal(boolean isOpenInExternal) {
        sharedPreferenceHelper.setOpenInExternal(isOpenInExternal);
    }

    public boolean isVibrateNotificationEnabled() {
        return !sharedPreferenceHelper.isNotificationVibrationDisabled();
    }

    public void setVibrateNotificationEnabled(boolean isEnabled) {
        sharedPreferenceHelper.setNotificationVibrationDisabled(!isEnabled);
    }

    public boolean isSoundNotificationEnabled() {
        return !sharedPreferenceHelper.isNotificationSoundDisabled();
    }

    public void setNotificationSoundEnabled(boolean isEnabled) {
        sharedPreferenceHelper.setNotificationSoundDisabled(!isEnabled);
    }

    public void setSdChosen(boolean isSdChosen) {
        sharedPreferenceHelper.setSDChosen(isSdChosen);
    }

    public boolean isSdChosen() {
        return sharedPreferenceHelper.isSDChosen();
    }

    public boolean isNeedToShowCalendarWidget() {
        return sharedPreferenceHelper.isNeedToShowCalendarWidget();
    }

    public void setNeedToShowCalendarWidget(boolean needToShowCalendarWidget) {
        sharedPreferenceHelper.setNeedToShowCalendarWidget(needToShowCalendarWidget);
    }

    public boolean isShowDiscountingPolicyWarning() {
        return sharedPreferenceHelper.isShowDiscountingPolicyWarning();
    }

    public void setShowDiscountingPolicyWarning(boolean isChecked) {
        sharedPreferenceHelper.setShowDiscountingPolicyWarning(isChecked);
    }

    public boolean isKeepScreenOnSteps() {
        return sharedPreferenceHelper.isKeepScreenOnSteps();
    }

    public void setKeepScreenOnSteps(boolean isChecked) {
        sharedPreferenceHelper.setKeepScreenOnSteps(isChecked);
    }

    public boolean isAdaptiveModeEnabled() {
        return sharedPreferenceHelper.isAdaptiveModeEnabled();
    }

    public void setAdaptiveModeEnabled(boolean isEnabled) {
        sharedPreferenceHelper.setAdaptiveModeEnabled(isEnabled);
        if (!isEnabled) {
            analytic.reportEvent(Analytic.Adaptive.ADAPTIVE_MODE_DISABLED);
        }
    }

    public boolean isNotificationEnabled(NotificationType type) {
        return !sharedPreferenceHelper.isNotificationDisabled(type);
    }

    public void setNotificationEnabled(NotificationType type, boolean isEnabled) {
        sharedPreferenceHelper.setNotificationDisabled(type, !isEnabled);
    }

    public void setStorageLocation(StorageLocation storageLocation) {
        sharedPreferenceHelper.setStorageLocation(storageLocation);
    }

    @Nullable
    public StorageLocation getStorageLocation() {
        return sharedPreferenceHelper.getStorageLocation();
    }
}
