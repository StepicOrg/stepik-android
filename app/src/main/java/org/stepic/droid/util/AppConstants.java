package org.stepic.droid.util;


import org.jetbrains.annotations.Nullable;

public class AppConstants {

    public static final String FILE_PROVIDER_AUTHORITY = ".provider";
    public static final String SVG_EXTENSION = ".svg";

    public static final String ERROR_SOCIAL_AUTH_WITH_EXISTING_EMAIL = "social_signup_with_existing_email";
    public static final String ERROR_SOCIAL_AUTH_WITHOUT_EMAIL = "social_signup_without_email";

    public static final String KEY_EMAIL_BUNDLE = "email";
    public static final String KEY_COURSE_BUNDLE = "course";
    public static final String KEY_COURSE_LONG_ID = "course_id_key";
    public static final String DEFAULT_QUALITY = "360";
    public static final String HIGH_QUALITY = "720";
    public static final String MAX_QUALITY = "1080";
    public static final int MAX_QUALITY_INT = 1080;
    public static final String COMMENT_DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm";
    public static final String NOTIFICATIONS_GROUP_DATE = "dd MMMM";
    public static final String NOTIFICATIONS_GROUP_DAY = "EEEE";

    public static final String WEB_URI_SEPARATOR = "/";

    //Types of steps:
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_MATCHING = "matching";
    public static final String TYPE_SORTING = "sorting";
    public static final String TYPE_MATH = "math";
    public static final String TYPE_FREE_ANSWER = "free-answer";
    public static final String TYPE_STRING = "string";
    public static final String TYPE_CHOICE = "choice";
    public static final String TYPE_NUMBER = "number";
    public static final String TYPE_DATASET = "dataset";
    public static final String TYPE_ANIMATION = "animation";
    public static final String TYPE_CHEMICAL = "chemical";
    public static final String TYPE_PUZZLE = "puzzle";
    public static final String TYPE_PYCHARM = "pycharm";
    public static final String TYPE_CODE = "code";
    public static final String TYPE_ADMIN = "admin";
    public static final String TYPE_SQL = "sql";
    public static final String TYPE_LINUX_CODE = "linux-code";

    public static final String TYPE_NULL = "null_type";

    public static final int REQUEST_EXTERNAL_STORAGE = 13;
    public static final String COURSE_ID_KEY = "course_id";

    public static final String NOTIFICATION_CANCELED = "notification_canceled";


    public static final String KEY_NOTIFICATION_ID = "key_notification_id";
    public static final String OPEN_NOTIFICATION_FOR_CHECK_COURSE = "Open_notification_check_course";
    public static final String OPEN_NOTIFICATION_FOR_ENROLL_REMINDER = "open_notificatoin_for_enroll_reminder";
    public static final String OPEN_NOTIFICATION_FROM_STREAK = "open_notification_from_streak";
    public static final String OPEN_NOTIFICATION = "Open_notification";
    public static final long MILLIS_IN_24HOURS = 86400000L;
    public static final long MILLIS_IN_1HOUR = 3600000L;
    public static final long MILLIS_IN_1MINUTE = 60000L;


    public static final String APP_INDEXING_COURSE_DETAIL_MANIFEST_HACK = "course_app";
    public static final String APP_INDEXING_SYLLABUS_MANIFEST = "syllabus";
    public static final String COMMA = ",";

    public static final String LINKEDIN_ADD_URL = "https://www.linkedin.com/profile/add?";
    public static final String LINKEDIN_ED_ID = "0_uInsUtRlLF5qiDUg80Aftvf5K-uMiiQPc0IVksZ_0oh1hhPRasb5cWi8eD5WXfgDaSgvthvZk7wTBMS3S-m0L6A6mLjErM6PJiwMkk6nYZylU7__75hCVwJdOTZCAkdv";//// TODO: 02.08.16 add to configs?

    public static final int REQUEST_CODE_GOOGLE_SIGN_IN = 7007;

    public static final int LAUNCHES_FOR_EXPERT_USER = 20;
    public static final long MILLIS_IN_SEVEN_DAYS = 604800000L;
    public static final String NOTIFICATION_CANCELED_REMINDER = "notification_canceled_reminder";
    public static final int MAX_NUMBER_OF_SHOWING_STREAK_DIALOG = 3;
    public static final int NUMBER_OF_DAYS_BETWEEN_STREAK_SHOWING = 2;
    public static final int MAX_NUMBER_OF_NOTIFICATION_STREAK = 5;
    @Nullable
    public static final String NOTIFICATION_CANCELED_STREAK = "notification_canceled_streaks";
    public static final String CATALOG_SHORTCUT_ID = "find_courses";
    public static final String OPEN_SHORTCUT_CATALOG = "open_shortcut_find_courses";
    public static final String PROFILE_SHORTCUT_ID = "profile";
    public static final String OPEN_SHORTCUT_PROFILE = "open_shortcut_profile";
    public static final String INTERNAL_STEPIK_ACTION = "internal_stepik_action";
    public static final String setCookieHeaderName = "Set-Cookie";
    public static final String authorizationHeaderName = "Authorization";
    public static final String cookieHeaderName = "Cookie";
    public static final String refererHeaderName = "Referer";
    public static final String csrfTokenHeaderName = "X-CSRFToken";

    public final static String FROM_MAIN_FEED_FLAG = "from_main_feed";
}
