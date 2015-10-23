package org.stepic.droid.preferences;

import android.content.Context;
import android.os.Environment;

import org.stepic.droid.model.Profile;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserPreferences {

    Context mContext;
    SharedPreferenceHelper sharedPreferenceHelper;


    @Inject
    public UserPreferences(Context context, SharedPreferenceHelper helper) {
        mContext = context;
        sharedPreferenceHelper = helper;
    }

    /**
     * Returns user storage directory under /Android/data/ folder for the currently logged in user.
     * This is the folder where all video downloads should be kept.
     *
     * @return folder for current user, where videos are saved.
     */
    public File getDownloadFolder() {

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
        }

        return userStepicIdDir;
    }

    private long getUserId() {
        Profile userProfile = sharedPreferenceHelper.getProfile();
        long userId = -1; // default anonymous user id
        if (userProfile != null) {
            userId = userProfile.getId();
        }
        return userId;
    }


    public boolean isNetworkMobileAllowed() {
        return false;
        //// FIXME: 20.10.15 save at preferences and change
    }

}
