package org.stepic.droid.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.core.DefaultFilter;
import org.stepic.droid.di.AppSingleton;
import org.stepic.droid.model.StepikFilter;
import org.stepic.droid.model.comments.DiscussionOrder;
import org.stepic.droid.notifications.model.NotificationType;
import org.stepic.droid.persistence.model.StorageLocation;
import org.stepic.droid.ui.util.TimeIntervalUtil;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.DateTimeHelper;
import org.stepic.droid.util.RWLocks;
import org.stepic.droid.web.AuthenticationStepikResponse;
import org.stepik.android.model.user.EmailAddress;
import org.stepik.android.model.user.Profile;

import java.io.File;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

@AppSingleton
public class SharedPreferenceHelper {
    private static final String NOTIFICATION_SOUND_DISABLED = "notification_sound";
    private static final String NEED_DROP_116 = "need_drop_116";
    private static final String DISCOUNTING_POLICY_DIALOG = "discounting_pol_dialog";
    private static final String KEEP_SCREEN_ON_STEPS = "keep_screen_on_steps";
    private static final String IS_ADAPTIVE_MODE_ENABLED = "is_adaptive_mode_enabled";
    private static final String IS_FIRST_ADAPTIVE_COURSE = "is_first_adaptive_course";
    private static final String IS_ADAPTIVE_EXP_TOOLTIP_WAS_SHOWN = "is_adaptive_exp_tooltip_was_shown";
    private static final String ROTATE_PREF = "rotate_pref";
    private static final String NOTIFICATION_LEARN_DISABLED = "notification_disabled_by_user";
    private static final String NOTIFICATION_COMMENT_DISABLED = "notification_comment_disabled";
    private static final String NOTIFICATION_TEACH_DISABLED = "notification_teach_disabled";
    private static final String NOTIFICATION_REVIEW_DISABLED = "notification_review_disabled";
    private static final String NOTIFICATION_OTHER_DISABLED = "notification_other_disabled";
    private static final String NOTIFICATION_VIBRATION_DISABLED = "not_vibrat_disabled";
    private final static String ONE_DAY_NOTIFICATION = "one_day_notification";
    private final static String SEVEN_DAY_NOTIFICATION = "seven_day_notification";
    private static final String FILTER_RUSSIAN_LANGUAGE = "russian_lang";
    private static final String FILTER_ENGLISH_LANGUAGE = "english_lang";
    private static final String NOTIFICATIONS_COUNT = "notifications_count";
    private static final String IS_EVER_LOGGED = "is_ever_logged";

    private static final String IS_LANG_WIDGET_WAS_SHOWN_AFTER_LOGIN = "is_lang_widget_was_shown_after_login";
    private static final String NEED_SHOW_LANG_WIDGET = "need_show_lang_widget";

    private static final String SUBMISSIONS_COUNT = "submissions_count";

    private static final String STORAGE_LOCATION_PATH = "storage_location_path";
    private static final String STORAGE_LOCATION_TYPE = "storage_location_type";

    private final String ACCESS_TOKEN_TIMESTAMP = "access_token_timestamp";
    private final String AUTH_RESPONSE_JSON = "auth_response_json";
    private final String PROFILE_JSON = "profile_json";
    private final String EMAIL_LIST = "email_list";
    private final String WIFI_KEY = "wifi_key";
    private final String IS_SOCIAL = "is_social_key";
    private final String VIDEO_QUALITY_KEY = "video_quality_key";
    private final String VIDEO_QUALITY_KEY_FOR_PLAYING = "video_quality_key_for_playing";
    private final String TEMP_POSITION_KEY = "temp_position_key";
    private final String VIDEO_RATE_PREF_KEY = "video_rate_pref_key";
    private final String VIDEO_EXTERNAL_PREF_KEY = "video_external_pref_key";
    private final String GCM_TOKEN_ACTUAL = "gcm_token_actual_with_badges"; // '_with_badges' suffix was added to force update of gcm token to enable silent push with badge count, see #188
    private final String SD_CHOSEN = "sd_chosen";
    private final String FIRST_TIME_LAUNCH = "first_time_launch";
    private final String SCHEDULED_LINK_CACHED = "scheduled_cached";
    private final String DISCUSSION_ORDER = "discussion_order";
    private final String CALENDAR_WIDGET = "calenda_widget";
    private final String AB_DEADLINES_TOOLTIP_SHOWN = "ab_deadlines_tooltip_shown";
    private final String FIRST_TIME_VIDEO = "first_time_video";
    private final String VIDEO_QUALITY_EXPLANATION = "video_quality_explanation";
    private final String NEED_DROP_114 = "need_drop_114";
    private final String REMIND_CLICK = "remind_click";
    private final String ANY_STEP_SOLVED = "any_step_solved";
    private final String NUMBER_OF_STEPS_SOLVED = "number_of_steps_solved";
    private final String NEW_USER_ALARM_TIMESTAMP = "new_user_alarm_timestamp";
    private final String REGISTRATION_ALARM_TIMESTAMP = "registration_alarm_timestamp";
    private final String NUMBER_OF_SHOWN_STREAK_DIALOG = "number_of_shown_streak_dialog";
    private final String STREAK_DIALOG_SHOWN_TIMESTAMP = "streak_dialog_shown_timestamp";
    private final String STREAK_NUMBER_OF_IGNORED = "streak_number_of_ignored";
    private final String TIME_NOTIFICATION_CODE = "time_notification_code";
    private final String STREAK_NOTIFICATION = "streak_notification";
    private final String USER_START_KEY = "user_start_app";
    private final String RATE_LAST_TIMESTAMP = "rate_last_timestamp";
    private final String RATE_TIMES_SHOWN = "rate_times_shown";
    private final String RATE_WAS_HANDLED = "rate_was_handled";

    private final static String LAST_SESSION_TIMESTAMP = "last_session_timestamp";
    private final static String RETENTION_NOTITICATION_TIMESTAMP = "retention_notification_timestamp";

    private AuthenticationStepikResponse cachedAuthStepikResponse = null;


    private final Context context;
    private final Analytic analytic;
    private final DefaultFilter defaultFilter;

    public enum NotificationDay {
        DAY_ONE(ONE_DAY_NOTIFICATION),
        DAY_SEVEN(SEVEN_DAY_NOTIFICATION);

        private String internalNotificationKey;

        NotificationDay(String notificationKey) {
            this.internalNotificationKey = notificationKey;
        }

        public String getInternalNotificationKey() {
            return internalNotificationKey;
        }
    }

    @Inject
    public SharedPreferenceHelper(Analytic analytic, DefaultFilter defaultFilter, Context context) {
        this.analytic = analytic;
        this.defaultFilter = defaultFilter;
        this.context = context;
    }

    /**
     * call when user click Google Play or Support at rate Dialog
     */
    public void afterRateWasHandled() {
        put(PreferenceType.DEVICE_SPECIFIC, RATE_WAS_HANDLED, true);
    }

    public boolean wasRateHandled() {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, RATE_WAS_HANDLED);
    }

    public void rateShown(long timeMillis) {
        put(PreferenceType.DEVICE_SPECIFIC, RATE_LAST_TIMESTAMP, timeMillis);
        long times = getLong(PreferenceType.DEVICE_SPECIFIC, RATE_TIMES_SHOWN, 0);
        put(PreferenceType.DEVICE_SPECIFIC, RATE_TIMES_SHOWN, times + 1);
    }

    /**
     * @return last timestamp, when it was shown, -1 when it has never been shown
     */
    public long whenRateWasShown() {
        return getLong(PreferenceType.DEVICE_SPECIFIC, RATE_LAST_TIMESTAMP, -1);
    }

    public long howManyRateWasShownBefore() {
        return getLong(PreferenceType.DEVICE_SPECIFIC, RATE_TIMES_SHOWN, 0);
    }

    public void incrementNumberOfNotifications() {
        int numberOfIgnored = getInt(PreferenceType.LOGIN, STREAK_NUMBER_OF_IGNORED, 0);
        numberOfIgnored++;
        put(PreferenceType.LOGIN, STREAK_NUMBER_OF_IGNORED, numberOfIgnored);
    }

    public void resetNumberOfStreakNotifications() {
        put(PreferenceType.LOGIN, STREAK_NUMBER_OF_IGNORED, 0);
    }

    public int getNumberOfStreakNotifications() {
        return getInt(PreferenceType.LOGIN, STREAK_NUMBER_OF_IGNORED, 0);
    }

    public boolean anyStepIsSolved() {
        return getBoolean(PreferenceType.LOGIN, ANY_STEP_SOLVED, false);
    }

    public void trackWhenUserSolved() {
        put(PreferenceType.LOGIN, ANY_STEP_SOLVED, true);
    }

    public void incrementUserSolved() {
        long userSolved = getLong(PreferenceType.LOGIN, NUMBER_OF_STEPS_SOLVED, 0);
        put(PreferenceType.LOGIN, NUMBER_OF_STEPS_SOLVED, userSolved + 1);
    }

    public long numberOfSolved() {
        return getLong(PreferenceType.LOGIN, NUMBER_OF_STEPS_SOLVED, 0);
    }

    public void incrementSubmissionsCount() {
        long submissionsCount = getLong(PreferenceType.DEVICE_SPECIFIC, SUBMISSIONS_COUNT, 0);
        put(PreferenceType.DEVICE_SPECIFIC, SUBMISSIONS_COUNT, submissionsCount + 1);
        analytic.setSubmissionsCount(submissionsCount + 1);
    }

    public void saveNewUserRemindTimestamp(long scheduleMillis) {
        put(PreferenceType.DEVICE_SPECIFIC, NEW_USER_ALARM_TIMESTAMP, scheduleMillis);
    }

    public long getNewUserRemindTimestamp() {
        return getLong(PreferenceType.DEVICE_SPECIFIC, NEW_USER_ALARM_TIMESTAMP);
    }

    public void saveRegistrationRemindTimestamp(long scheduleMillis) {
        put(PreferenceType.DEVICE_SPECIFIC, REGISTRATION_ALARM_TIMESTAMP, scheduleMillis);
    }

    public long getRegistrationRemindTimestamp() {
        return getLong(PreferenceType.DEVICE_SPECIFIC, REGISTRATION_ALARM_TIMESTAMP);
    }

    public void saveLastSessionTimestamp(long scheduleMillis) {
        put(PreferenceType.DEVICE_SPECIFIC, LAST_SESSION_TIMESTAMP, scheduleMillis);
    }

    public long getLastSessionTimestamp() {
        return getLong(PreferenceType.DEVICE_SPECIFIC, LAST_SESSION_TIMESTAMP);
    }

    public void saveRetentionNotificationTimestamp(long scheduleMillis) {
        put(PreferenceType.DEVICE_SPECIFIC, RETENTION_NOTITICATION_TIMESTAMP, scheduleMillis);
    }

    public long getRetentionNotificationTimestamp() {
        return getLong(PreferenceType.DEVICE_SPECIFIC, RETENTION_NOTITICATION_TIMESTAMP);
    }

    /**
     * Lang widget should be kept the whole session after first time catalog was opened after new login
     */
    public boolean isNeedShowLangWidget() {
        boolean isLangWidgetWasShown = getBoolean(PreferenceType.LOGIN, IS_LANG_WIDGET_WAS_SHOWN_AFTER_LOGIN, false);
        if (!isLangWidgetWasShown) {
            put(PreferenceType.LOGIN, IS_LANG_WIDGET_WAS_SHOWN_AFTER_LOGIN, true);
            put(PreferenceType.LOGIN, NEED_SHOW_LANG_WIDGET, true);
        }

        return getBoolean(PreferenceType.LOGIN, NEED_SHOW_LANG_WIDGET, true);
    }

    public void onSessionAfterLogin() {
        put(PreferenceType.LOGIN, IS_LANG_WIDGET_WAS_SHOWN_AFTER_LOGIN, false);
    }

    public void onNewSession() {
        put(PreferenceType.LOGIN, NEED_SHOW_LANG_WIDGET, false);
        saveLastSessionTimestamp(DateTimeHelper.INSTANCE.nowUtc());
    }

    public void clickEnrollNotification(long timestamp) {
        put(PreferenceType.DEVICE_SPECIFIC, REMIND_CLICK, timestamp);
    }

    @Nullable
    public Long getLastClickEnrollNotification() {
        long lastClickNotificationRemind = getLong(PreferenceType.DEVICE_SPECIFIC, REMIND_CLICK);
        if (lastClickNotificationRemind <= 0) {
            return null;
        } else {
            return lastClickNotificationRemind;
        }
    }

    public int getTimeNotificationCode() {
        return getInt(PreferenceType.LOGIN, TIME_NOTIFICATION_CODE, TimeIntervalUtil.INSTANCE.getDefaultTimeCode());
    }

    public void setTimeNotificationCode(int value) {
        put(PreferenceType.LOGIN, TIME_NOTIFICATION_CODE, value);
    }

    public boolean isStreakNotificationEnabled() {
        int simpleCode = getInt(PreferenceType.LOGIN, STREAK_NOTIFICATION, -1);
        return simpleCode > 0;
    }

    /**
     * Null by default
     */
    @Nullable
    public Boolean isStreakNotificationEnabledNullable() {
        int codeInt = getInt(PreferenceType.LOGIN, STREAK_NOTIFICATION, -1);
        if (codeInt > 0) {
            return true;
        } else if (codeInt == 0) {
            return false;
        } else {
            return null;
        }
    }

    public void setStreakNotificationEnabled(boolean value) {
        put(PreferenceType.LOGIN, STREAK_NOTIFICATION, value ? 1 : 0);
        analytic.setStreaksNotificationsEnabled(value);
        resetNumberOfStreakNotifications();
    }

    public boolean canShowStreakDialog() {
        int streakDialogShownNumber = getInt(PreferenceType.LOGIN, NUMBER_OF_SHOWN_STREAK_DIALOG, 0);
        if (streakDialogShownNumber > AppConstants.MAX_NUMBER_OF_SHOWING_STREAK_DIALOG) {
            return false;
        } else {
            long millis = getLong(PreferenceType.LOGIN, STREAK_DIALOG_SHOWN_TIMESTAMP, -1L);
            if (millis < 0) {
                onShowStreakDialog(streakDialogShownNumber);
                // first time
                return true;
            } else {
                long calculatedMillis = millis + AppConstants.NUMBER_OF_DAYS_BETWEEN_STREAK_SHOWING * AppConstants.MILLIS_IN_24HOURS;
                if (DateTimeHelper.INSTANCE.isBeforeNowUtc(calculatedMillis)) {
                    //we can show
                    onShowStreakDialog(streakDialogShownNumber);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    private void onShowStreakDialog(int streakDialogShownNumber) {
        analytic.reportEvent(Analytic.Streak.CAN_SHOW_DIALOG, streakDialogShownNumber + "");
        put(PreferenceType.LOGIN, STREAK_DIALOG_SHOWN_TIMESTAMP, DateTimeHelper.INSTANCE.nowUtc());
        put(PreferenceType.LOGIN, NUMBER_OF_SHOWN_STREAK_DIALOG, streakDialogShownNumber + 1);
    }

    public boolean isNotificationWasShown(NotificationDay day) {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, day.getInternalNotificationKey(), false);
    }

    public void setNotificationShown(NotificationDay day) {
        put(PreferenceType.DEVICE_SPECIFIC, day.getInternalNotificationKey(), true);
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

    @Nullable
    public StorageLocation getStorageLocation() {
        final String path = getString(PreferenceType.DEVICE_SPECIFIC, STORAGE_LOCATION_PATH);
        if (path == null) return null;

        final File file = new File(path);
        final StorageLocation.Type type = StorageLocation.Type.valueOf(getString(PreferenceType.DEVICE_SPECIFIC, STORAGE_LOCATION_TYPE));
        return new StorageLocation(file, file.getFreeSpace(), file.getTotalSpace(), type);
    }

    public void setStorageLocation(StorageLocation storageLocation) {
        put(PreferenceType.DEVICE_SPECIFIC, STORAGE_LOCATION_PATH, storageLocation.getPath().getAbsolutePath());
        put(PreferenceType.DEVICE_SPECIFIC, STORAGE_LOCATION_TYPE, storageLocation.getType().name());
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

    public void setRotateAlways(boolean needRotate) {
        put(PreferenceType.DEVICE_SPECIFIC, ROTATE_PREF, needRotate);
    }

    public boolean needRotate() {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, ROTATE_PREF, true);
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

    public boolean isOnboardingNotPassedYet() {
        // before onboarding App was tracked first time launch
        // for avoiding to show onboarding for old users this property is reused
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, FIRST_TIME_LAUNCH, true);
    }

    public boolean isScheduleAdded() {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, SCHEDULED_LINK_CACHED, false);
    }

    public void afterScheduleAdded() {
        put(PreferenceType.DEVICE_SPECIFIC, SCHEDULED_LINK_CACHED, true);
    }

    public void afterOnboardingPassed() {
        put(PreferenceType.DEVICE_SPECIFIC, FIRST_TIME_LAUNCH, false);
    }

    public boolean isFirstAdaptiveCourse() {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, IS_FIRST_ADAPTIVE_COURSE, true);
    }

    public void afterAdaptiveOnboardingPassed() {
        put(PreferenceType.DEVICE_SPECIFIC, IS_FIRST_ADAPTIVE_COURSE, false);
    }

    public boolean isAdaptiveExpTooltipWasShown() {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, IS_ADAPTIVE_EXP_TOOLTIP_WAS_SHOWN, false);
    }

    public void afterAdaptiveExpTooltipWasShown() {
        put(PreferenceType.DEVICE_SPECIFIC, IS_ADAPTIVE_EXP_TOOLTIP_WAS_SHOWN, true);
    }

    public void setHasEverLogged() {
        put(PreferenceType.DEVICE_SPECIFIC, IS_EVER_LOGGED, true);
    }

    public boolean isEverLogged() {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, IS_EVER_LOGGED, false);
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

    public boolean isAdaptiveModeEnabled() {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, IS_ADAPTIVE_MODE_ENABLED, true);
    }

    public void setAdaptiveModeEnabled(boolean isEnabled) {
        put(PreferenceType.DEVICE_SPECIFIC, IS_ADAPTIVE_MODE_ENABLED, isEnabled);
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

    private String mapToPreferenceName(StepikFilter filter) {
        switch (filter) {
            case RUSSIAN:
                return FILTER_RUSSIAN_LANGUAGE;
            case ENGLISH:
                return FILTER_ENGLISH_LANGUAGE;
            default:
                throw new IllegalArgumentException("Unknown StepikFilter type: " + filter);
        }
    }

    @NotNull
    public EnumSet<StepikFilter> getFilterForFeatured() {
        EnumSet<StepikFilter> filters = EnumSet.noneOf(StepikFilter.class);
        for (StepikFilter filter : StepikFilter.values()) {
            appendValueForFilter(filters, filter, defaultFilter.getDefaultFilter(filter));
        }
        if (filters.size() > 1) {
            //only one of languages are allowed
            put(PreferenceType.FEATURED_FILTER, mapToPreferenceName(StepikFilter.ENGLISH), false);
            return EnumSet.of(StepikFilter.RUSSIAN);
        }

        return filters;
    }

    private void appendValueForFilter(EnumSet<StepikFilter> filter, StepikFilter value, boolean defaultValue) {
        if (getBoolean(PreferenceType.FEATURED_FILTER, mapToPreferenceName(value), defaultValue)) {
            filter.add(value);
        }
    }

    public void saveFilterForFeatured(EnumSet<StepikFilter> filters) {
        for (StepikFilter filterValue : StepikFilter.values()) {
            String key = mapToPreferenceName(filterValue);
            put(PreferenceType.FEATURED_FILTER, key, filters.contains(filterValue));
        }
    }

    private enum PreferenceType {
        LOGIN("login preference"),
        WIFI("wifi_preference"),
        VIDEO_QUALITY("video_quality_preference"),
        TEMP("temporary"),
        VIDEO_SETTINGS("video_settings"),
        DEVICE_SPECIFIC("device_specific"),
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

    public void setNotificationsCount(int count) {
        put(PreferenceType.LOGIN, NOTIFICATIONS_COUNT, count);
    }

    public int getNotificationsCount() {
        return getInt(PreferenceType.LOGIN, NOTIFICATIONS_COUNT, 0);
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

    public boolean isFirstTimeVideo() {
        return getBoolean(PreferenceType.VIDEO_SETTINGS, FIRST_TIME_VIDEO, true);
    }

    public void afterFirstTimeVideo() {
        put(PreferenceType.VIDEO_SETTINGS, FIRST_TIME_VIDEO, false);
    }

    public boolean isPersonalDeadlinesTooltipShown() {
        return getBoolean(PreferenceType.DEVICE_SPECIFIC, AB_DEADLINES_TOOLTIP_SHOWN, false);
    }

    public void afterPersonalDeadlinesTooltipShown() {
        put(PreferenceType.DEVICE_SPECIFIC, AB_DEADLINES_TOOLTIP_SHOWN, true);
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
        return gson.fromJson(json, Profile.class);
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
        List<EmailAddress> result;
        try {
            result = gson.fromJson(json, new TypeToken<List<EmailAddress>>(){}.getType());
        } catch (Exception e) {
            result = null;
        }
        return result;

    }

    public void saveVideoQualityForPlaying(String videoQuality) {
        put(PreferenceType.VIDEO_QUALITY, VIDEO_QUALITY_KEY_FOR_PLAYING, videoQuality);
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
        } else if (str.equals("1080")) {
            //it is hack for removing 1080 quality from dialogs
            return AppConstants.MAX_QUALITY;
        } else {
            return str;
        }
    }

    public String getVideoQualityForPlaying() {
        String str = getString(PreferenceType.VIDEO_QUALITY, VIDEO_QUALITY_KEY_FOR_PLAYING);
        if (str == null) {
            //by default high
            return AppConstants.MAX_QUALITY;
        } else {
            return str;
        }
    }

    public void storeAuthInfo(AuthenticationStepikResponse response) {
        Gson gson = new Gson();
        String json = gson.toJson(response);
        put(PreferenceType.LOGIN, AUTH_RESPONSE_JSON, json);
        cachedAuthStepikResponse = response;

        long millisNow = DateTimeHelper.INSTANCE.nowUtc(); // we should use +0 UTC for avoid problems with TimeZones
        put(PreferenceType.LOGIN, ACCESS_TOKEN_TIMESTAMP, millisNow);

        put(PreferenceType.DEVICE_SPECIFIC, IS_EVER_LOGGED, true);
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
            cachedAuthStepikResponse = null;
            clear(PreferenceType.LOGIN);
            clear(PreferenceType.FEATURED_FILTER);
        } finally {
            RWLocks.AuthLock.writeLock().unlock();
        }
    }

    @Nullable
    public AuthenticationStepikResponse getAuthResponseFromStore() {
        if (cachedAuthStepikResponse != null) {
            return cachedAuthStepikResponse;
        }

        String json = getString(PreferenceType.LOGIN, AUTH_RESPONSE_JSON);
        if (json == null) {
            return null;
        }

        Gson gson = new GsonBuilder().create();
        cachedAuthStepikResponse = gson.fromJson(json, AuthenticationStepikResponse.class);
        return cachedAuthStepikResponse;
    }

    public long getAccessTokenTimestamp() {
        return getLong(PreferenceType.LOGIN, ACCESS_TOKEN_TIMESTAMP);
    }

    public boolean isMobileInternetAlsoAllowed() {
        return getBoolean(PreferenceType.WIFI, WIFI_KEY);
    }

    public void setMobileInternetAndWifiAllowed(boolean isOnlyWifi) {
        put(PreferenceType.WIFI, WIFI_KEY, isOnlyWifi);
    }

    @Nullable
    public String getSplitTestGroup(@NotNull String testName) {
        return getString(PreferenceType.DEVICE_SPECIFIC, testName);
    }

    public void saveSplitTestGroup(@NotNull String testName, @NotNull String groupName) {
        put(PreferenceType.DEVICE_SPECIFIC, testName, groupName);
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

    private int getInt(PreferenceType preferenceType, String key, int defaultValue) {
        return context.getSharedPreferences(preferenceType.getStoreName(), Context.MODE_PRIVATE)
                .getInt(key, defaultValue);
    }

    private int getInt(PreferenceType preferenceType, String key) {
        return context.getSharedPreferences(preferenceType.getStoreName(), Context.MODE_PRIVATE)
                .getInt(key, -1);
    }

    private long getLong(PreferenceType preferenceType, String key, long defaultValue) {
        return context.getSharedPreferences(preferenceType.getStoreName(), Context.MODE_PRIVATE)
                .getLong(key, defaultValue);
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
