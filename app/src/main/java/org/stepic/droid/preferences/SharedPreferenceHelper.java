package org.stepic.droid.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.EmailAddress;
import org.stepic.droid.model.Profile;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.RWLocks;
import org.stepic.droid.web.AuthenticationStepicResponse;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SharedPreferenceHelper {
    private Context mContext;

    @Inject
    public SharedPreferenceHelper() {
        mContext = MainApplication.getAppContext();
    }


    public enum PreferenceType {
        LOGIN("login preference"),
        WIFI("wifi_preference"),
        VIDEO_QUALITY("video_quality_preference"),
        TEMP("temporary"),
        VIDEO_SETTINGS("video_settings");

        private String description;

        PreferenceType(String description) {
            this.description = description;
        }

        private String getStoreName() {
            return description;
        }
    }

    public void storeVideoPlaybackRate(@NotNull VideoPlaybackRate videoPlaybackRate) {
        int videoIndex = videoPlaybackRate.getIndex();
        put(PreferenceType.VIDEO_SETTINGS, VIDEO_RATE_PREF_KEY, videoIndex);
    }

    @NotNull
    public VideoPlaybackRate getVideoPlaybackRate() {
        int index = getInt(PreferenceType.VIDEO_SETTINGS, VIDEO_RATE_PREF_KEY);

        for (VideoPlaybackRate item : VideoPlaybackRate.values()) {
            if (index == item.getIndex()) return item;
        }

        return VideoPlaybackRate.x1_0;//default
    }

    public boolean isOpenInExternal() {
        return getBoolean(PreferenceType.VIDEO_SETTINGS, VIDEO_EXTERNAL_PREF_KEY);
    }

    public void setOpenInExternal(boolean isOpenInExternal) {
        put(PreferenceType.VIDEO_SETTINGS, VIDEO_EXTERNAL_PREF_KEY, isOpenInExternal);
    }

    public void storeProfile(Profile profile) {
        //todo save picture of user profile
        //todo validate profile from the server with cached profile and make restore to cache. make
        //todo query when nav drawer is occurred?
        Gson gson = new Gson();
        String json = gson.toJson(profile);
        put(PreferenceType.LOGIN, PROFILE_JSON, json);
    }

    public Profile getProfile() {
        String json = getString(PreferenceType.LOGIN, PROFILE_JSON);
        if (json == null) {
            return null;
        }
        Gson gson = new GsonBuilder().create();
        Profile result = gson.fromJson(json, Profile.class);
        return result;
    }

    public void storeEmailAddresses(List<EmailAddress> emailAddresses) {
        if (emailAddresses == null) return;
        Gson gson = new Gson();
        String json = gson.toJson(emailAddresses);
        put(PreferenceType.LOGIN, EMAIL_LIST, json);
    }

    @Nullable
    public List<EmailAddress> getStoredEmails() {
        String json = getString(PreferenceType.LOGIN, EMAIL_LIST);
        if (json == null) {
            return null;
        }
        Gson gson = new GsonBuilder().create();
        List<EmailAddress> result = null;
        try {

            result = gson.fromJson(json, new TypeToken<List<EmailAddress>>() {
            }.getType());
        } catch (Exception e) {
            return null;
        }
        return result;

    }

    public void storeVideoQuality(String videoQuality) {
        put(PreferenceType.VIDEO_QUALITY, VIDEO_QUALITY_KEY, videoQuality);
    }

    public void storeTempPosition(int position) {
        put(PreferenceType.TEMP, TEMP_POSITION_KEY, position);
    }

    public int getTempPosition() {
        return getInt(PreferenceType.TEMP, TEMP_POSITION_KEY);
    }

    @NotNull
    public String getVideoQuality() {
        String str = getString(PreferenceType.VIDEO_QUALITY, VIDEO_QUALITY_KEY);
        if (str == null) {
            return AppConstants.DEFAULT_QUALITY;
        } else {
            return str;
        }
    }

    public void storeAuthInfo(AuthenticationStepicResponse response) {
        Gson gson = new Gson();
        String json = gson.toJson(response);
        put(PreferenceType.LOGIN, AUTH_RESPONSE_JSON, json);

        DateTime now = DateTime.now(DateTimeZone.UTC);
        long millisNow = now.getMillis();
        put(PreferenceType.LOGIN, ACCESS_TOKEN_TIMESTAMP, millisNow);
    }

    public void storeLastTokenType(boolean isSocial) {
        put(PreferenceType.LOGIN, IS_SOCIAL, isSocial);
    }

    public boolean isLastTokenSocial() {
        return getBoolean(PreferenceType.LOGIN, IS_SOCIAL);
    }

    public void deleteAuthInfo() {
        RWLocks.AuthLock.writeLock().lock();
        try {
            clear(PreferenceType.LOGIN);
        } finally {
            RWLocks.AuthLock.writeLock().unlock();
        }
    }

    @Nullable
    public AuthenticationStepicResponse getAuthResponseFromStore() {
        String json = getString(PreferenceType.LOGIN, AUTH_RESPONSE_JSON);
        if (json == null) {
            return null;
        }

        Gson gson = new GsonBuilder().create();
        AuthenticationStepicResponse result = gson.fromJson(json, AuthenticationStepicResponse.class);
        return result;
    }

    public long getAccessTokenTimestamp() {
        long timestamp = getLong(PreferenceType.LOGIN, ACCESS_TOKEN_TIMESTAMP);
        return timestamp;
    }


    public boolean isMobileInternetAlsoAllowed() {
        return getBoolean(PreferenceType.WIFI, WIFI_KEY);
    }

    public void setMobileInternetAndWifiAllowed(boolean isOnlyWifi) {
        put(PreferenceType.WIFI, WIFI_KEY, isOnlyWifi);
    }

    private void put(PreferenceType type, String key, String value) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(type.getStoreName(), Context.MODE_PRIVATE).edit();
        editor.putString(key, value).apply();
    }

    private void put(PreferenceType type, String key, int value) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(type.getStoreName(), Context.MODE_PRIVATE).edit();
        editor.putInt(key, value).apply();
    }

    private void put(PreferenceType type, String key, long value) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(type.getStoreName(), Context.MODE_PRIVATE).edit();
        editor.putLong(key, value).apply();
    }

    private void put(PreferenceType type, String key, Boolean value) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(type.getStoreName(), Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, value).apply();
    }

    private void clear(PreferenceType type) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(type.getStoreName(), Context.MODE_PRIVATE).edit();
        editor.clear().apply();
    }

    private int getInt(PreferenceType preferenceType, String key) {
        return mContext.getSharedPreferences(preferenceType.getStoreName(), Context.MODE_PRIVATE)
                .getInt(key, -1);
    }

    private long getLong(PreferenceType preferenceType, String key) {
        return mContext.getSharedPreferences(preferenceType.getStoreName(), Context.MODE_PRIVATE)
                .getLong(key, -1);
    }

    private String getString(PreferenceType preferenceType, String key) {
        return mContext.getSharedPreferences(preferenceType.getStoreName(), Context.MODE_PRIVATE)
                .getString(key, null);
    }

    private boolean getBoolean(PreferenceType preferenceType, String key) {
        return mContext.getSharedPreferences(preferenceType.getStoreName(), Context.MODE_PRIVATE)
                .getBoolean(key, false);
    }

    private final String ACCESS_TOKEN_TIMESTAMP = "access_token_timestamp";
    private final String AUTH_RESPONSE_JSON = "auth_response_json";
    private final String PROFILE_JSON = "profile_json";
    private final String EMAIL_LIST = "email_list";
    private final String WIFI_KEY = "wifi_key";
    private final String IS_SOCIAL = "is_social_key";
    private final String VIDEO_QUALITY_KEY = "video_quality_key";
    private final String TEMP_POSITION_KEY = "temp_position_key";
    private final String VIDEO_RATE_PREF_KEY = "video_rate_pref_key";
    private final String VIDEO_EXTERNAL_PREF_KEY = "video_external_pref_key";

}
