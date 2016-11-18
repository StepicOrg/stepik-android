package org.stepic.droid.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.core.DefaultFilter;
import org.stepic.droid.model.EmailAddress;
import org.stepic.droid.model.Profile;
import org.stepic.droid.model.StepikFilter;
import org.stepic.droid.model.comments.DiscussionOrder;
import org.stepic.droid.notifications.model.NotificationType;
import org.stepic.droid.store.operations.Table;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.RWLocks;
import org.stepic.droid.web.AuthenticationStepicResponse;

import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SharedPreferenceHelper {
    private static final String NOTIFICATION_SOUND_DISABLED = "notification_sound";
    private static final String TEMP_UPDATE_LINK = "temp_update_link";
    private static final java.lang.String NEED_DROP_116 = "need_drop_116";
    private static final java.lang.String DISCOUNTING_POLICY_DIALOG = "discounting_pol_dialog";
    private static final java.lang.String KEEP_SCREEN_ON_STEPS = "keep_screen_on_steps";


    private final String ACCESS_TOKEN_TIMESTAMP = "access_token_timestamp";
    private final String UPDATING_TIMESTAMP = "updating_timestamp";
    private final String AUTH_RESPONSE_JSON = "auth_response_json";
    private final String PROFILE_JSON = "profile_json";
    private final String EMAIL_LIST = "email_list";
    private final String WIFI_KEY = "wifi_key";
    private final String IS_SOCIAL = "is_social_key";
    private final String VIDEO_QUALITY_KEY = "video_quality_key";
    private final String TEMP_POSITION_KEY = "temp_position_key";
    private final String VIDEO_RATE_PREF_KEY = "video_rate_pref_key";
    private final String VIDEO_EXTERNAL_PREF_KEY = "video_external_pref_key";
    private final String GCM_TOKEN_ACTUAL = "gcm_token_actual";
    private final String NOTIFICATION_LEARN_DISABLED = "notification_disabled_by_user";
    private final String NOTIFICATION_COMMENT_DISABLED = "notification_comment_disabled";
    private final String NOTIFICATION_TEACH_DISABLED = "notification_teach_disabled";
    private final String NOTIFICATION_REVIEW_DISABLED = "notification_review_disabled";
    private final String NOTIFICATION_OTHER_DISABLED = "notification_other_disabled";
    private final String NOTIFICATION_VIBRATION_DISABLED = "not_vibrat_disabled";
    private final String SD_CHOSEN = "sd_chosen";
    private final String FIRST_TIME_LAUNCH = "first_time_launch";
    private final String SCHEDULED_LINK_CACHED = "scheduled_cached";
    private final String DISCUSSION_ORDER = "discussion_order";
    private final String CALENDAR_WIDGET = "calenda_widget";
    private final String VIDEO_QUALITY_EXPLANATION = "video_quality_explanation";
    private final String NEED_DROP_114 = "need_drop_114";
    private static final String ONE_DAY_NOTIFICATION = "one_day_notification";
    private static final String SEVEN_DAY_NOTIFICATION = "seven_day_notification";
    private static final String ANY_STEP_SOLVED = "any_step_solved";
    private static final String NEW_USER_ALARM_TIMESTAMP = "new_user_alarm_timestamp";

    private final String FILTER_PERSISTENT = "filter_persistent";
    private final String FILTER_RUSSIAN_LANGUAGE = "russian_lang";
    private final String FILTER_ENGLISH_LANGUAGE = "english_lang";
    private final String FILTER_UPCOMING = "filter_upcoming";
    private final String FILTER_ACTIVE = "filter_active";
    private final String FILTER_PAST = "filter_past";

    public final String USER_START_KEY = "user_start_app";

    private Context context;
    private Analytic analytic;
    private DefaultFilter defaultFilter;

    public boolean anyStepIsSolved() {
        return getBoolean(PreferenceType.LOGIN, ANY_STEP_SOLVED, false);
    }

    public void trackWhenUserSolved() {
        put(PreferenceType.LOGIN, ANY_STEP_SOLVED, true);
    }

    public void saveNewUserTimestamp(long scheduleMillis) {
        put(PreferenceType.DEVICE_SPECIFIC, NEW_USER_ALARM_TIMESTAMP, scheduleMillis);
    }

    public long getNewUserTimestamp (){
        return getLong(PreferenceType.DEVICE_SPECIFIC, NEW_USER_ALARM_TIMESTAMP);
    }

    public enum NotificationDay {
        ONE(ONE_DAY_NOTIFICATION),
        SEVEN(SEVEN_DAY_NOTIFICATION);

        private String internalNotificationKey;

        NotificationDay(String notificationKey) {
            this.internalNotificationKey = notificationKey;
        }

        public String getInternalNotificationKey() {
            return internalNotificationKey;
        }
    }


    public boolean isNotificationWasShown(NotificationDay day) {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, day.getInternalNotificationKey(), false);
    }

    public void setNotificationShown(NotificationDay day) {
        put(PreferenceType.DEVICE_SPECIFIC, day.getInternalNotificationKey(), true);
    }

    @Inject
    public SharedPreferenceHelper(Analytic analytic, DefaultFilter defaultFilter, Context context) {
        this.analytic = analytic;
        this.defaultFilter = defaultFilter;
        this.context = context;

        resetFilters(Table.enrolled); //reset on app recreating and on destroy course's Fragments
        resetFilters(Table.featured);
    }

    public void onTryDiscardFilters(Table type) {
        resetFilters(type);
    }

    public int incrementNumberOfLaunches() {
        int numberOfLaunches = getInt(PreferenceType.DEVICE_SPECIFIC, USER_START_KEY);
        int newValue = numberOfLaunches + 1;
        put(PreferenceType.DEVICE_SPECIFIC, USER_START_KEY, newValue);
        return newValue;
    }

    public int getNumberOfLaunches() {
        return getInt(PreferenceType.DEVICE_SPECIFIC, USER_START_KEY);
    }

    public boolean isSDChosen() {
        //default is not. false -> sd is not chosen
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, SD_CHOSEN);
    }

    public void setSDChosen(boolean isSdChosen) {
        put(PreferenceType.DEVICE_SPECIFIC, SD_CHOSEN, isSdChosen);
    }

    public boolean isNotificationVibrationDisabled() {
        //default is enabled
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, NOTIFICATION_VIBRATION_DISABLED);
    }

    public void setNotificationVibrationDisabled(boolean isNotificationVibrationDisabled) {
        put(PreferenceType.DEVICE_SPECIFIC, NOTIFICATION_VIBRATION_DISABLED, isNotificationVibrationDisabled);
    }

    public boolean isNotificationDisabled(NotificationType type) {
        String resultKey = keyByNotificationType(type);
        if (resultKey == null) return true;
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, resultKey);
    }

    @Nullable
    private String keyByNotificationType(NotificationType type) {
        switch (type) {
            case learn:
                return NOTIFICATION_LEARN_DISABLED;
            case teach:
                return NOTIFICATION_TEACH_DISABLED;
            case comments:
                return NOTIFICATION_COMMENT_DISABLED;
            case other:
                return NOTIFICATION_OTHER_DISABLED;
            case review:
                return NOTIFICATION_REVIEW_DISABLED;
        }
        return null;
    }

    public void setNotificationDisabled(NotificationType type, boolean isNotificationDisabled) {
        String key = keyByNotificationType(type);
        if (key != null) {
            analytic.reportEventWithIdName(Analytic.Notification.PERSISTENT_KEY_NULL, "0", type.name());
            put(PreferenceType.DEVICE_SPECIFIC, key, isNotificationDisabled);
        }
    }

    public boolean isNotificationSoundDisabled() {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, NOTIFICATION_SOUND_DISABLED);
    }

    public void setNotificationSoundDisabled(boolean isDisabled) {
        put(PreferenceType.DEVICE_SPECIFIC, NOTIFICATION_SOUND_DISABLED, isDisabled);
    }

    public boolean isFirstTime() {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, FIRST_TIME_LAUNCH, true);
    }

    public boolean isScheduleAdded() {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, SCHEDULED_LINK_CACHED, false);
    }

    public void afterScheduleAdded() {
        put(PreferenceType.DEVICE_SPECIFIC, SCHEDULED_LINK_CACHED, true);
    }

    public void afterFirstTime() {
        put(PreferenceType.DEVICE_SPECIFIC, FIRST_TIME_LAUNCH, false);
    }

    public boolean isNeedToShowVideoQualityExplanation() {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, VIDEO_QUALITY_EXPLANATION, true);
    }

    public boolean isKeepScreenOnSteps() {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, KEEP_SCREEN_ON_STEPS, true);
    }

    public void setKeepScreenOnSteps(boolean isChecked) {
        put(PreferenceType.DEVICE_SPECIFIC, KEEP_SCREEN_ON_STEPS, isChecked);
    }

    public void setNeedToShowVideoQualityExplanation(boolean needToShowCalendarWidget) {
        put(PreferenceType.DEVICE_SPECIFIC, VIDEO_QUALITY_EXPLANATION, needToShowCalendarWidget);
    }

    public boolean isNeedToShowCalendarWidget() {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, CALENDAR_WIDGET, true);
    }

    public boolean isShowDiscountingPolicyWarning() {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, DISCOUNTING_POLICY_DIALOG, true);
    }

    public void setNeedToShowCalendarWidget(boolean needToShowCalendarWidget) {
        put(PreferenceType.DEVICE_SPECIFIC, CALENDAR_WIDGET, needToShowCalendarWidget);
    }

    public void setShowDiscountingPolicyWarning(boolean needToShowCalendarWidget) {
        put(PreferenceType.DEVICE_SPECIFIC, DISCOUNTING_POLICY_DIALOG, needToShowCalendarWidget);
    }

    public boolean isNeedDropCoursesIn114() {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, NEED_DROP_114, true) || getBoolean(PreferenceType.DEVICE_SPECIFIC, NEED_DROP_116, true);
    }

    public void afterNeedDropCoursesIn114() {
        put(PreferenceType.DEVICE_SPECIFIC, NEED_DROP_114, false);
        put(PreferenceType.DEVICE_SPECIFIC, NEED_DROP_116, false);
    }

    private void resetFilters(Table type) {
        if (type == Table.enrolled && !getBoolean(PreferenceType.ENROLLED_FILTER, FILTER_PERSISTENT, defaultFilter.getDefaultEnrolled(StepikFilter.PERSISTENT))) {
            clear(PreferenceType.ENROLLED_FILTER);
        }

        if (type == Table.featured && !getBoolean(PreferenceType.FEATURED_FILTER, FILTER_PERSISTENT, defaultFilter.getDefaultFeatured(StepikFilter.PERSISTENT))) {
            clear(PreferenceType.FEATURED_FILTER);
        }
    }

    public EnumSet<StepikFilter> getFilter(Table type) {
        EnumSet<StepikFilter> filter = EnumSet.noneOf(StepikFilter.class);
        // TODO: 04.09.16 refactor
        if (type == Table.enrolled) {
            appendValueForFilter(type, filter, FILTER_RUSSIAN_LANGUAGE, StepikFilter.RUSSIAN, defaultFilter.getDefaultEnrolled(StepikFilter.RUSSIAN));
            appendValueForFilter(type, filter, FILTER_ENGLISH_LANGUAGE, StepikFilter.ENGLISH, defaultFilter.getDefaultEnrolled(StepikFilter.ENGLISH));
            appendValueForFilter(type, filter, FILTER_UPCOMING, StepikFilter.UPCOMING, defaultFilter.getDefaultEnrolled(StepikFilter.UPCOMING));
            appendValueForFilter(type, filter, FILTER_ACTIVE, StepikFilter.ACTIVE, defaultFilter.getDefaultEnrolled(StepikFilter.ACTIVE));
            appendValueForFilter(type, filter, FILTER_PAST, StepikFilter.PAST, defaultFilter.getDefaultEnrolled(StepikFilter.PAST));
            appendValueForFilter(type, filter, FILTER_PERSISTENT, StepikFilter.PERSISTENT, defaultFilter.getDefaultEnrolled(StepikFilter.PERSISTENT));
        } else {
            appendValueForFilter(type, filter, FILTER_RUSSIAN_LANGUAGE, StepikFilter.RUSSIAN, defaultFilter.getDefaultFeatured(StepikFilter.RUSSIAN));
            appendValueForFilter(type, filter, FILTER_ENGLISH_LANGUAGE, StepikFilter.ENGLISH, defaultFilter.getDefaultFeatured(StepikFilter.ENGLISH));
            appendValueForFilter(type, filter, FILTER_UPCOMING, StepikFilter.UPCOMING, defaultFilter.getDefaultFeatured(StepikFilter.UPCOMING));
            appendValueForFilter(type, filter, FILTER_ACTIVE, StepikFilter.ACTIVE, defaultFilter.getDefaultFeatured(StepikFilter.ACTIVE));
            appendValueForFilter(type, filter, FILTER_PAST, StepikFilter.PAST, defaultFilter.getDefaultFeatured(StepikFilter.PAST));
            appendValueForFilter(type, filter, FILTER_PERSISTENT, StepikFilter.PERSISTENT, defaultFilter.getDefaultFeatured(StepikFilter.PERSISTENT));

        }
        return filter;
    }

    private void appendValueForFilter(Table type, EnumSet<StepikFilter> filter, String key, StepikFilter value, boolean defaultValue) {
        PreferenceType preferenceType = resolvePreferenceType(type);
        if (getBoolean(preferenceType, key, defaultValue)) {
            filter.add(value);
        }
    }

    @NonNull
    private PreferenceType resolvePreferenceType(Table type) {
        PreferenceType preferenceType;
        if (type == Table.enrolled) {
            preferenceType = PreferenceType.ENROLLED_FILTER;
        } else {
            preferenceType = PreferenceType.FEATURED_FILTER;
        }
        return preferenceType;
    }

    public void saveFilter(Table type, EnumSet<StepikFilter> filter) {
        saveValueFromFilterIfExist(type, filter, FILTER_RUSSIAN_LANGUAGE, StepikFilter.RUSSIAN);
        saveValueFromFilterIfExist(type, filter, FILTER_ENGLISH_LANGUAGE, StepikFilter.ENGLISH);
        saveValueFromFilterIfExist(type, filter, FILTER_UPCOMING, StepikFilter.UPCOMING);
        saveValueFromFilterIfExist(type, filter, FILTER_ACTIVE, StepikFilter.ACTIVE);
        saveValueFromFilterIfExist(type, filter, FILTER_PAST, StepikFilter.PAST);
        saveValueFromFilterIfExist(type, filter, FILTER_PERSISTENT, StepikFilter.PERSISTENT);
    }

    private void saveValueFromFilterIfExist(Table type, EnumSet<StepikFilter> filter, String key, StepikFilter value) {
        PreferenceType preferenceType = resolvePreferenceType(type);
        put(preferenceType, key, filter.contains(value));
    }

    public enum PreferenceType {
        LOGIN("login preference"),
        WIFI("wifi_preference"),
        VIDEO_QUALITY("video_quality_preference"),
        TEMP("temporary"),
        VIDEO_SETTINGS("video_settings"),
        DEVICE_SPECIFIC("device_specific"),
        ENROLLED_FILTER("filter_prefs"),
        FEATURED_FILTER("featured_filter_prefs");

        private String description;

        PreferenceType(String description) {
            this.description = description;
        }

        public String getStoreName() {
            return description;
        }
    }

    public DiscussionOrder getDiscussionOrder() {
        int orderId = getInt(PreferenceType.LOGIN, DISCUSSION_ORDER);
        DiscussionOrder order = DiscussionOrder.Companion.getById(orderId);
        analytic.reportEvent(Analytic.Comments.ORDER_TREND, order.toString());
        return order;
    }

    public void setDiscussionOrder(DiscussionOrder disscussionOrder) {
        put(PreferenceType.LOGIN, DISCUSSION_ORDER, disscussionOrder.getId());
    }

    public void setIsGcmTokenOk(boolean isGcmTokenOk) {
        put(PreferenceType.LOGIN, GCM_TOKEN_ACTUAL, isGcmTokenOk);
    }

    public boolean isGcmTokenOk() {
        return getBoolean(PreferenceType.LOGIN, GCM_TOKEN_ACTUAL);
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
        if (profile != null) {
            analytic.setUserId(profile.getId() + "");
        }
        Gson gson = new Gson();
        String json = gson.toJson(profile);
        put(PreferenceType.LOGIN, PROFILE_JSON, json);
    }

    @Nullable
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

    public void storeTempLink(String link) {
        put(PreferenceType.TEMP, TEMP_UPDATE_LINK, link);
    }

    public String getTempLink() {
        return getString(PreferenceType.TEMP, TEMP_UPDATE_LINK);
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

    public void storeLastShownUpdatingMessage() {
        DateTime now = DateTime.now(DateTimeZone.UTC);
        long millisNow = now.getMillis();
        put(PreferenceType.DEVICE_SPECIFIC, UPDATING_TIMESTAMP, millisNow);
    }

    public long getLastShownUpdatingMessageTimestamp() {
        long timestamp = getLong(PreferenceType.DEVICE_SPECIFIC, UPDATING_TIMESTAMP);
        return timestamp;
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
            Profile profile = getProfile();
            String userId = "anon_prev";
            if (profile != null) {
                userId += profile.getId();
            }
            analytic.setUserId(userId);
            clear(PreferenceType.LOGIN);
            clear(PreferenceType.FEATURED_FILTER);
            clear(PreferenceType.ENROLLED_FILTER);
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
        SharedPreferences.Editor editor = context.getSharedPreferences(type.getStoreName(), Context.MODE_PRIVATE).edit();
        editor.putString(key, value).apply();
    }

    private void put(PreferenceType type, String key, int value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(type.getStoreName(), Context.MODE_PRIVATE).edit();
        editor.putInt(key, value).apply();
    }

    private void put(PreferenceType type, String key, long value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(type.getStoreName(), Context.MODE_PRIVATE).edit();
        editor.putLong(key, value).apply();
    }

    private void put(PreferenceType type, String key, Boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(type.getStoreName(), Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, value).apply();
    }

    private void clear(PreferenceType type) {
        SharedPreferences.Editor editor = context.getSharedPreferences(type.getStoreName(), Context.MODE_PRIVATE).edit();
        editor.clear().apply();
    }

    private int getInt(PreferenceType preferenceType, String key) {
        return context.getSharedPreferences(preferenceType.getStoreName(), Context.MODE_PRIVATE)
                .getInt(key, -1);
    }

    private long getLong(PreferenceType preferenceType, String key) {
        return context.getSharedPreferences(preferenceType.getStoreName(), Context.MODE_PRIVATE)
                .getLong(key, -1);
    }

    private String getString(PreferenceType preferenceType, String key) {
        return context.getSharedPreferences(preferenceType.getStoreName(), Context.MODE_PRIVATE)
                .getString(key, null);
    }

    private boolean getBoolean(PreferenceType preferenceType, String key) {
        return getBoolean(preferenceType, key, false);
    }

    private boolean getBoolean(PreferenceType preferenceType, String key, boolean defaultValue) {
        return context.getSharedPreferences(preferenceType.getStoreName(), Context.MODE_PRIVATE)
                .getBoolean(key, defaultValue);
    }
}
