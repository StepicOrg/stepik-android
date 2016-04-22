package org.stepic.droid.preferences;

import android.content.Context;
import android.os.Environment;

import com.yandex.metrica.YandexMetrica;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.EmailAddress;
import org.stepic.droid.model.Profile;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserPreferences {

    Context mContext;
    SharedPreferenceHelper mSharedPreferenceHelper;


    @Inject
    public UserPreferences(Context context, SharedPreferenceHelper helper) {
        mContext = context;
        mSharedPreferenceHelper = helper;
    }

    /**
     * Returns user storage directory under /Android/data/ folder for the currently logged in user.
     * This is the folder where all video downloads should be kept.
     *
     * @return folder for current user, where videos are saved.
     */
    public File getUserDownloadFolder() {

        File android = new File(Environment.getExternalStorageDirectory(), "Android");
        File downloadsDir = new File(android, "data");
        File packDir = new File(downloadsDir, mContext.getPackageName());
        File userStepicIdDir = new File(packDir, getUserId() + "");
        userStepicIdDir.mkdirs();
        try {
            //hide from gallery our videos.
            File noMediaFile = new File(userStepicIdDir, ".nomedia");
            noMediaFile.createNewFile();
        } catch (IOException ioException) {
            // FIXME: 20.10.15 handle exception
            YandexMetrica.reportError("can't create .nomedia", ioException);
        }

        return userStepicIdDir;
    }

    private long getUserId() {
        Profile userProfile = mSharedPreferenceHelper.getProfile();
        long userId = -1; // default anonymous user id
        if (userProfile != null) {
            userId = userProfile.getId();
        }
        return userId;
    }

    @Nullable
    public List<EmailAddress> getUserEmails() {
        return mSharedPreferenceHelper.getStoredEmails();
    }

    @Nullable
    public EmailAddress getPrimaryEmail() {
        List<EmailAddress> emails = getUserEmails();
        if (emails == null || emails.isEmpty()) return null;
        if (emails.size() == 1) return emails.get(0);

        //emails >1
        EmailAddress primary = null;
        for (EmailAddress item : emails) {
            if (item != null && item.is_primary()) {
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
        return mSharedPreferenceHelper.isMobileInternetAlsoAllowed();
    }

    public String getQualityVideo() {
        return mSharedPreferenceHelper.getVideoQuality();
    }

    public void storeQualityVideo(String videoQuality) {
        mSharedPreferenceHelper.storeVideoQuality(videoQuality);
    }

    public VideoPlaybackRate getVideoPlaybackRate() {
        return mSharedPreferenceHelper.getVideoPlaybackRate();
    }

    public void setVideoPlaybackRate(VideoPlaybackRate rate) {
        mSharedPreferenceHelper.storeVideoPlaybackRate(rate);
    }

    public boolean isOpenInExternal() {
        return mSharedPreferenceHelper.isOpenInExternal();
    }

    public void setOpenInExternal(boolean isOpenInExternal) {
        mSharedPreferenceHelper.setOpenInExternal(isOpenInExternal);
    }

    public boolean isNotificationEnabled() {
        return !mSharedPreferenceHelper.isNotificationDisabled();
    }

    public void setNotificationEnabled(boolean isEnabled) {
        mSharedPreferenceHelper.setNotificationDisabled(!isEnabled);
    }


    public boolean isVibrateNotificationEnabled() {
        return !mSharedPreferenceHelper.isNotificationVibrationDisabled();
    }

    public void setVibrateNotificationEnabled(boolean isEnabled) {
        mSharedPreferenceHelper.setNotificationVibrationDisabled(!isEnabled);
    }

    public boolean isSoundNotificationEnabled() {
        return !mSharedPreferenceHelper.isNotificationSoundDisabled();
    }

    public void setNotificationSoundEnabled(boolean isEnabled) {
        mSharedPreferenceHelper.setNotificationSoundDisabled(!isEnabled);
    }

}
