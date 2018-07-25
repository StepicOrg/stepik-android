package org.stepic.droid.preferences;

import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.di.AppSingleton;
import org.stepic.droid.model.StorageOption;
import org.stepic.droid.notifications.model.NotificationType;
import org.stepic.droid.util.StorageUtil;
import org.stepik.android.model.user.EmailAddress;
import org.stepik.android.model.user.Profile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

@AppSingleton
public class UserPreferences {

    private final Context context;
    private final SharedPreferenceHelper sharedPreferenceHelper;
    private final Analytic analytic;

    private String kb;
    private String mb;
    private String gb;
    private String defaultStorage;
    private String secondary;
    private String free_title;


    @Inject
    public UserPreferences(Context context, SharedPreferenceHelper helper, Analytic analytic) {
        this.context = context;
        this.sharedPreferenceHelper = helper;
        this.analytic = analytic;
        kb = context.getString(R.string.kb);
        mb = context.getString(R.string.mb);
        gb = context.getString(R.string.gb);
        defaultStorage = context.getString(R.string.default_storage);
        secondary = context.getString(R.string.secondary_storage);
        free_title = context.getString(R.string.free_title);
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
        File packDir = new File(downloadsDir, context.getPackageName());
        if (packDir == null) return null;
        File userStepicIdDir = new File(packDir, getUserId() + "");
        userStepicIdDir.mkdirs();
        try {
            //hide from gallery our videos.
            File noMediaFile = new File(userStepicIdDir, ".nomedia");
            noMediaFile.createNewFile();
        } catch (IOException ioException) {
            // FIXME: 20.10.15 handle exception
            analytic.reportError(Analytic.Error.CANT_CREATE_NOMEDIA, ioException);
        }

        return userStepicIdDir;
    }

    @Nullable
    public File getSdCardDownloadFolder() {
        try {
            File androidDataPackage = ContextCompat.getExternalFilesDirs(context, null)[1];
            if (androidDataPackage == null) return null;
            File userStepicIdDir = new File(androidDataPackage, getUserId() + "");
            userStepicIdDir.mkdirs();
            try {
                //hide from gallery our videos.
                File noMediaFile = new File(userStepicIdDir, ".nomedia");
                noMediaFile.createNewFile();
            } catch (IOException ioException) {
                // FIXME: 20.10.15 handle exception
                analytic.reportError(Analytic.Error.CANT_CREATE_NOMEDIA, ioException);
            }

            return userStepicIdDir;
        } catch (IndexOutOfBoundsException ex) {
            return null;
        } catch (NullPointerException ex) {
            return null;
        }
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

    /**
     * @return oldList of storage option: oldList.size()<=2, can be empty
     */
    @NotNull
    public List<StorageOption> getStorageOptionList() {
        List<StorageOption> list = new ArrayList<>();
        File[] files = StorageUtil.getRawAppDirs();
        if (files == null || files.length == 0) {
            return list;
        }

        int i = 0;
        while (i < files.length && i < 2) {
            if (files[i] != null) {
                long free = StorageUtil.getAvailableMemorySize(files[i]);
                long total = StorageUtil.getTotalMemorySize(files[i]);
                if (total <= 0) {
                    // not show fake storage
                    continue;
                }

                boolean isChosen = false;
                final boolean isSd = isSdChosen();
                if (isSd && i != 0) {
                    isChosen = true;
                } else if (!isSd && i == 0) {
                    isChosen = true;
                }
                String info = formatOptionList(i, total, free, files[i]);

                StorageOption option = new StorageOption(info, isChosen, total, free, files[i]);
                list.add(option);
            }
            i++;
        }


        return list;
    }

    //move to another class. total&free in bytes
    private String formatOptionList(int index, long total, long free, File file) {
        total /= 1024;
        free /= 1024; //now in kb
        StringBuilder sb = new StringBuilder();
        if (index == 0) {
            sb.append(defaultStorage);
        } else {
            sb.append(secondary);
        }

        sb.append(" (");

        addToBuilderSizeSpaceMeasure(sb, total);
        sb.append(")");
        sb.append(". ");
        addToBuilderSizeSpaceMeasure(sb, free);
        sb.append(" ");
        sb.append(free_title);
        return sb.toString();
    }

    private void addToBuilderSizeSpaceMeasure(StringBuilder sb, long sizeInKb) {
        if (sizeInKb < 1024) {
            sb.append(sizeInKb);
            sb.append(" ");
            sb.append(kb);
        } else {
            sizeInKb /= 1024;

            if (sizeInKb >= 1024) {
                sizeInKb /= 1024;
                sb.append(sizeInKb);
                sb.append(" ");
                sb.append(gb);
            } else {
                sb.append(sizeInKb);
                sb.append(" ");
                sb.append(mb);
            }
        }
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

    public boolean isRotateVideo() {
        return sharedPreferenceHelper.needRotate();
    }

    public void setRotateVideo(boolean rotateVideo) {
        sharedPreferenceHelper.setRotateAlways(rotateVideo);
    }
}
