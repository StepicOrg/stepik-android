package org.stepic.droid.analytic;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Analytic {

    interface FastContinue {
        String EMPTY_COURSES_SHOWN = "fast_continue_empty_courses";
        String EMPTY_COURSES_CLICK = "fast_continue_empty_courses_click";

        String NO_INTERNET_SHOWN = "fast_continue_no_internet";
        String NO_INTERNET_CLICK = "fast_continue_no_internet_click";
    }

    interface Rating {
        String SHOWN = "app_rate_shown"; //on shown dialog
        String APP_RATE = "app_rate"; //number of stars, that user chosen, may multiple by session

        String POSITIVE_LATER = "app_rate_positive_later";
        String POSITIVE_APPSTORE = "app_rate_positive_appstore";

        String NEGATIVE_LATER = "app_rate_negative_later";
        String NEGATIVE_EMAIL = "app_rate_negative_email";
    }

    interface Registration {
        String ERROR = "registration_error";
        String TAP_ON_FIELDS = "tap_on_fields_registration";
        String TYPING_TEXT_FIELDS = "typing_text_fields_registration";
        String CLICK_WITH_INTERACTION_TYPE = "click_registration_with_interaction_type";
        String CLICK_SEND_IME = "click_registration_send_ime";

    }

    interface Login {
        String FAIL_LOGIN = "fail_login";

        String REQUEST_LOGIN_WITH_INTERACTION_TYPE = "click_sign_in_with_interaction_type";

        String TAP_ON_FIELDS = "tap_on_fields_login";
        String TYPING_TEXT_FIELDS = "typing_text_fields_login";
    }

    interface System {
        String BOOT_COMPLETED = "boot_completed";
        java.lang.String FIRST_LAUNCH_AFTER_INSTALL = "first_launch_after_install";
    }

    interface Preferences {
        String VIDEO_QUALITY = "video quality was chosen";
    }

    interface Interaction {
        String CLICK_SIGN_IN = "click sign in on launch screen";
        String CLICK_SIGN_UP = "click sign up";
        String CLICK_SIGN_IN_ON_SIGN_IN_SCREEN = "click sign in on sign in on sign-in screen";
        String CLICK_SIGN_IN_NEXT_ON_SIGN_IN_SCREEN = "click_sign_in_next_sign_in_screen";
        String CLICK_DELETE_SECTION = "Click delete section from cache";
        String CLICK_CACHE_SECTION = "Click cache section";
        String CLICK_CACHE_LESSON = "Click cache unit";
        String CLICK_DELETE_LESSON = "Click delete unit from cache";
        String CLICK_LOGOUT = "Click logout";
        String CLICK_CLEAR_CACHE = "Click clear cache button";
        String CLICK_YES_LOGOUT = "Click accept logout";
        String CANCEL_VIDEO_QUALITY = "Cancel video quality dialog";
        String CANCEL_VIDEO_QUALITY_DETAILED = "cancel_detailed_video";
        String YES_CLEAR_VIDEOS = "clear videos from downloads";
        String DELETE_COMMENT_TRIAL = "comment: delete comment trial";
        String UPDATING_MESSAGE_IS_APPROVED = "updating approved";
        String PULL_TO_REFRESH_COURSE = "Pull from top to refreshWhenOnConnectionProblem course";
        String COURSE_USER_TRY_FAIL = "course: user open failed for him course";
        String JOIN_COURSE_NULL = "course is null when join, detail";
        String CANCEL_CHOOSE_STORE_CLICK = "storage: cancel choice";
        String AUTH_FROM_DIALOG_FOR_UNAUTHORIZED_USER = "Auth: yes from auth dialog";
        String TRANSFER_DATA_YES = "storage: transfer data";
        String CLICK_CANCEL_SECTION = "click cancel section";
        String CLICK_CANCEL_LESSON = "click cancel unit";
        String UPDATING_MESSAGE_IS_SHOWN = "updating shown";
        String REFRESH_UNIT = "Pull from top to refreshWhenOnConnectionProblem section unit";
        String REFRESH_SECTION = "Pull from top to refreshWhenOnConnectionProblem section";
        String SUCCESS_LOGIN = "success login";
        String SHOW_DETAILED_INFO_CLICK = "Show detailed info click from context menu of course";
        String LONG_TAP_COURSE = "Long tap on course";
        String CLICK_REGISTER_BUTTON = "click_register_register_screen";
        String CLICK_SEND_SUBMISSION = "click_send_submission";
        String SHARE_COURSE = "share_course_detail";
        String SHARE_COURSE_SECTION = "share_course_from_section";
        String CLICK_FIND_COURSE_EMPTY_SCREEN = "click_find_courses_empty_screen";
        String CLICK_NEXT_LESSON_IN_STEPS = "click_next_lesson_in_steps";
        String CLICK_PREVIOUS_LESSON_IN_STEPS = "click_previous_lesson_in_steps";
        String CLICK_SIGN_IN_SOCIAL = "social_login";
        String CLICK_ACCEPT_FILTER_BUTTON = "click_accept_filter_btn";
        String CLICK_AUTH_FROM_STEPS = "click_auth_from_steps";
        String SHARE_STEP_CLICK = "share_step_click";
        String CLICK_TRY_STEP_AGAIN = "step_try_again";
        String CLICK_TRY_STEP_AGAIN_AFTER_CORRECT = "step_try_again_after_correct";
        String NO_DISCOUNTING_DIALOG = "discounting_dialog_no";
        String YES_DISCOUNTING_DIALOG = "discounting_dialog_yes";
        String CLICK_SETTINGS_FROM_NOTIFICATION = "click_settings_from_notification";
        String START_SPLASH = "user_start_splash_new";
        String START_SPLASH_EXPERT = "user_start_splash_expert";
        String CLICK_CHOOSE_NOTIFICATION_INTERVAL = "click_choose_notification_interval";
        String CLICK_PRIVACY_POLICY = "click_privacy_policy";
        String CLICK_TERMS_OF_SERVICE = "click_terms_of_service";
        String POSITIVE_MATERIAL_DIALOG_INVITATION = "material_dialog_invite_positive";
        String NEGATIVE_MATERIAL_DIALOG_INVITATION = "material_dialog_invite_negative";
        String SHOW_MATERIAL_DIALOG_INVITATION = "materdial_dialog_invite_shown";
        String INVITATION_PREVENTED = "invite_prevented";
        String CLICK_CONTINUE_COURSE = "click_continue_course";
        String CLICK_COURSE = "click_course";
        String CLICK_PROFILE_BEFORE_LOADING = "click_profile_before_loading";
        String JOIN_COURSE = "click_join_course";

        String CLICK_FIND_COURSE_LAUNCH = "click_find_courses_launch";
        String USER_OPEN_IMAGE = "user_open_image";
        String SCREENSHOT = "screenshot";
        String GOOGLE_SOCIAL_IS_NOT_ENABLED = "google_social_is_not_enabled";
        String ACCEPT_DELETING_UNIT = "click_delete_unit_dialog";
        String ACCEPT_DELETING_SECTION = "click_delete_section_dialog";
        String CLICK_STREAK_DRAWER = "click_streak_drawer";
        String SHOW_LAUNCH_SCREEN_AFTER_LOGOUT = "show_launch_screen_after_logout";
    }

    interface Screens {
        String SHOW_LAUNCH = "Screen manager: show launch screen";
        String SHOW_REGISTRATION = "Screen manager: show registration";
        String SHOW_LOGIN = "Screen manager: show login";
        String SHOW_MAIN_FEED = "Screen manager: show main feed";
        String SHOW_COURSE_DESCRIPTION = "Screen manager: show course description";
        String SHOW_TEXT_FEEDBACK = "show text feedback";
        String OPEN_STORE = "Open google play, estimation";
        String TRY_OPEN_VIDEO = "video is tried to show";
        String SHOW_SETTINGS = "show settings";
        String SHOW_NOTIFICATION_SETTINGS = "show_notification_settings";
        String SHOW_STORAGE_MANAGEMENT = "show storage management";
        String OPEN_COMMENT_NOT_AVAILABLE = "comment: not available";
        String OPEN_COMMENT = "comments: open oldList";
        String OPEN_WRITE_COMMENT = "comments: open write form";
        String SHOW_SECTIONS = "Screen manager: show section";
        String SHOW_UNITS = "Screen manager: show units-lessons screen";
        String SHOW_STEP = "Screen manager: show steps of lesson";
        String OPEN_STEP_IN_WEB = "Screen manager: open Step in Web";
        String REMIND_PASSWORD = "Screen manager: remind password";
        String OPEN_LINK_IN_WEB = "open_link";

        String USER_OPEN_MY_COURSES = "main_choice_my_courses";
        String USER_OPEN_FIND_COURSES = "main_choice_find_courses";
        String USER_OPEN_DOWNLOADS = "main_choice_downloads";
        String USER_OPEN_CERTIFICATES = "main_choice_certificates";
        String USER_OPEN_FEEDBACK = "main_choice_feedback";
        String USER_OPEN_NOTIFICATIONS = "main_choice_notifications";
        String USER_OPEN_SETTINGS = "main_choice_settings";
        String USER_LOGOUT = "main_choice_logout";
        String USER_OPEN_ABOUT_APP = "main_choice_about";
        String SHOW_SECTIONS_JOINED = "show_sections_joined";
        String USER_OPEN_PROFILE = "main_choice_profile";
    }

    interface Video {
        String OPEN_EXTERNAL = "video_open_external";
        String OPEN_NATIVE = "video_open_native";
        String NOT_COMPATIBLE = "video_is_not_compatible";
        String SHOW_CHOOSE_RATE = "video_player_show_choose_rate_menu";
        String JUMP_FORWARD = "video_player_jump_forward";
        String JUMP_BACKWARD = "video_player_jump_backward";
        String QUALITY_NOT_DETERMINATED = "video_quality_failed";
        String SHOW_MORE_ITEMS = "video_player_show_more";
        String ROTATE_CLICKED = "video_player_rotate_clicked";
        String PLAYER_CREATED = "video_player_created";
        String PLAY = "video_player_play";
        String PAUSE = "video_player_pause";
        String ERROR = "video_player_error";
        String QUALITY_MENU = "video_player_quality";
        String CANCEL_VIDEO_QUALITY = "video_player_quality_cancel";
        String NOW_PLAYING_WAS_NULL = "video_player_now_playing_null";
        String OLD_STYLE = "video_old_style";
    }

    interface AppIndexing {
        String COURSE_DETAIL = "appindexing_course_detail";
        String COURSE_SYLLABUS = "appindexing_course_syllabus";
        String STEP = "appindexing_step";
    }

    interface Error {
        String CALLBACK_SOCIAL = "callback_from_social_login";
        String NOT_PLAYER = "NotPlayer";
        String VIDEO_RESOLVER_FAILED = "video resolver is failed";
        String CANT_UPDATE_TOKEN = "cant update token";
        String AUTH_ERROR = "retrofitAuth";
        String ERROR_CREATING_PLAYER = "video player: Error creating player";
        String INIT_PHONE_STATE = "initPhoneStateListener";
        String REMOVE_PHONE_STATE = "removePhoneStateCallbacks";
        String NOTIFICATION_ERROR_PARSE = "notification error parse";
        String DELETE_SERVICE_ERROR = "DeleteService nullptr";
        String ERROR_UPDATE_CHECK_APP = "update check failed";
        String UPDATE_FROM_APK_FAILED = "update apk is failed";
        String CANT_RESOLVE_VIDEO = "can't Resolve video";
        String FAIL_TO_MOVE = "storage: fail to move";
        String REGISTRATION_IMPORTANT_ERROR = "registration important error";
        String NOTIFICATION_NOT_POSTED_ON_CLICK = "notification is not posted";
        String NULL_SECTION = "Null section is not expected";
        String LESSON_IN_STORE_STATE_NULL = "lesson was null in store state manager";
        String UNIT_IN_STORE_STATE_NULL = "unit was null in store state manager";
        String LOAD_SERVICE = "Load Service";
        String PUSH_STATE_EXCEPTION = "Push state exception";
        String CANT_CREATE_NOMEDIA = "can't create .nomedia";
        String ILLEGAL_STATE_NEXT_LESSON = "cant_show_next_lesson";
        String ILLEGAL_STATE_PREVIOUS_LESSON = "cant_show_previous_lesson";
        String FAIL_PUSH_STEP_VIEW = "fail_push_step_view";
        String NO_INTERNET_EXISTING_ATTEMPTS = "no_internet_existing_attempts";
        String DOWNLOAD_ID_NEGATIVE = "download_id_negative";
        String STREAK_ON_STEP_SOLVED = "streak_on_step_solved";
        String GOOGLE_SERVICES_TOO_OLD = "google_services_too_old";
        String VIDEO_PATH_WAS_NULL_WITH_INTERNET = "video_path_was_null_internet_enabled";
        String FAIL_REFRESH_TOKEN_ONLINE = "fail_refresh_token_online";
        String FAIL_REFRESH_TOKEN_ONLINE_EXTENDED = "fail_refresh_token_online_extended";
        String COOKIE_MANAGER_ERROR = "cookie_manager_error";
        String FAIL_REFRESH_TOKEN_INLINE_GETTING = "fail_refresh_token_online_get";
        String COOKIE_WAS_EMPTY = "cookie_was_empty";
        String FAIL_LOGOUT_WHEN_REFRESH = "refresh_fail_logout_social";
        String UNITS_LOADING_FAIL = "units_loading_fail";
        String UNPREDICTABLE_LOGIN_RESULT = "login_successful_was_not_correct";
        String LESSON_ACCESS_DENIED = "lesson_access_denied";
        String SEARCH_COURSE_NO_INTERNET = "search_course_no_internet";
        String SEARCH_COURSE_UNSUCCESSFUL = "search_course_unsuccessful";
        String CHOICES_ARE_SMALLER = "choices_are_smaller";
        String PREVIOUS_VIEW_NOT_DETACHED = "previous_view_not_detached";
        String UNEXPECTED_VIEW = "unexpected_view";
        String ILLEGAL_STATE_VIDEO_STEP = "illegal_state_video_step";
        String ILLEGAL_STATE_VIDEO_STEP_PLAY = "illegal_state_video_step_play";
        String NO_VIDEO_ON_STEP_SHOWING = "illegal_state_video_no_video";
        String CANT_PARSE_QUALITY = "cant_parse_quality";
        String JOIN_FAILED = "error_join_failed"; //#Apps-580
        String UNIT_CACHED_LESSON_NO = "error_unit_cached_lesson_not";
    }

    interface Web {
        String UPDATE_TOKEN_FAILED = "update is failed";
        String AUTH_LOGIN_PASSWORD = "Api:auth with login password";
        String AUTH_SOCIAL = "Api:auth with social account";
        String TRY_REGISTER = "Api: try register";
        String TRY_JOIN_COURSE = "Api:try join to course";
        String DROP_COURSE = "Api: drop course";
        String DROP_COURSE_SUCCESSFUL = "drop course successful";
        String DROP_COURSE_FAIL = "drop course fail";
    }

    interface Notification {
        String DISABLED_BY_USER = "Notification is disabled by user in app";
        String ACTION_NOT_SUPPORT = "notification action is not support";
        String HTML_WAS_NULL = "notification_html_was_null";
        String WAS_MUTED = "notification_was_muted";
        String NOT_SUPPORT_TYPE = "notification_type_is_not_support";//After checking action

        String CANT_PARSE_COURSE_ID = "notification, cant parse courseId";
        String TOKEN_UPDATED = "notification gcm token is updated";
        String TOKEN_UPDATE_FAILED = "notification gcm token is not updated";
        String OPEN_NOTIFICATION = "notification_opened";
        String OPEN_NOTIFICATION_SYLLABUS = "notification_opened_syllabus";
        String ID_WAS_NULL = "notification_id_was_null";
        String NOTIFICATION_NULL_POINTER = "notification_unpredicatable_null";
        String NOTIFICATION_CLICKED_IN_CENTER = "notification_clicked_in_center";
        String NOTIFICATION_CENTER_OPENED = "notification_center_opened";
        String OPEN_COMMENT_NOTIFICATION_LINK = "notification_open_comment_link";
        String OPEN_LESSON_NOTIFICATION_LINK = "notification_open_lesson_link";
        String NOTIFICATION_NOT_OPENABLE = "notification_not_openable";
        String GCM_TOKEN_NOT_OK = "notification_gsm_token_not_ok";
        String NOTIFICATION_SHOWN = "notification_shown";
        String DISCARD = "notification_discarded";
        String CANT_PARSE_NOTIFICATION = "notification_parse_fail";
        String OPEN_TEACH_CENTER = "notification_open_teach_link";
        String PERSISTENT_KEY_NULL = "notification_key_null";
        String MARK_ALL_AS_READ = "notification_mark_all";
        String REMIND_HIDDEN = "remind_hidden";
        String REMIND_SHOWN = "remind_shown";
        String REMIND_SCHEDULED = "remind_scheduled";
        String REMIND_OPEN = "remind_opened";
        String REMIND_ENROLL = "remind_success_user_enroll";
        String REMINDER_SWIPE_TO_CANCEL = "remind_swipe_to_cancel";
        String STREAK_SWIPE_TO_CANCEL = "streak_swipe_to_cancel";
        String NIGHT_WITHOUT_SOUND_AND_VIBRATE = "notification_night_without_sound_and_vibrate";
    }

    interface Feedback {
        String FAILED_ON_SERVER = "Feedback is failed due to server";
        String INTERNET_FAIL = "Feedback internet fail";
        String FEEDBACK_SENT = "feedback_sent";
    }

    interface Comments {
        String CLICK_SEND_COMMENTS = "comments: click send comment";
        String COMMENTS_SENT_SUCCESSFULLY = "comments: comment was sent successfully";
        String DELETE_COMMENT_CONFIRMATION = "comment: delete comment confirmed";
        String ORDER_TREND = "order_trend";
        String SHOW_CONFIRM_DISCARD_TEXT_DIALOG = "comment_discard_dialog_show";
        String SHOW_CONFIRM_DISCARD_TEXT_DIALOG_SUCCESS = "comment_discard_ok";
        String OPEN_FROM_OPTION_MENU = "comment_open_from_option_menu";
        String OPEN_FROM_STEP_UI = "comment_open_from_step_ui";
    }

    interface Steps {
        String CORRECT_SUBMISSION_FILL = "submission_correct_fill"; // it can be existing submission, use in chain.
        String WRONG_SUBMISSION_FILL = "submission_wrong_fill";
        String SHARE_OPEN_IN_BROWSER = "step_share_open_in_browser";
        String COPY_LINK = "step_share_copy";
        String SHARE_ALL = "steps_share_all";
        String SHOW_KEEP_ON_SCREEN = "steps_show_keep_on_screen";
        String SHOW_KEEP_OFF_SCREEN = "steps_show_keep_off_screen";
        String STEP_OPENED = "step_opened";
        String CLICK_SEND_SUBMISSION_STEP_TYPE = "step_click_send";
    }

    interface Calendar {
        String USER_CLICK_ADD_WIDGET = "calendar_click_add_widget";
        String USER_CLICK_ADD_MENU = "calendar_click_add_menu";
        String CALENDAR_ADDED_SUCCESSFULLY = "calendar_added_successfully";
        String CALENDAR_ADDED_FAIL = "calendar_added_fail";
        String SHOW_CALENDAR_AS_WIDGET = "calendar_shown_as_widget";
        String SHOW_CALENDAR = "calendar_shown"; // course with deadlines in future //// FIXME: 13.01.17 this metric has doubled number of events
        String HIDE_WIDGET_FROM_PREFS = "widget_hidden_from_prefs"; //// FIXME: 13.01.17 this metric has doubled number of events
        String USER_CLICK_NOT_NOW = "calendar_click_not_now";
    }

    interface DeepLink {
        String USER_OPEN_LINK_GENERAL = "open_deep_link";
        String USER_OPEN_SYLLABUS_LINK = "open_syllabus_by_link";
        String USER_OPEN_COURSE_DETAIL_LINK = "open_detail_course_link";
        String USER_OPEN_STEPS_LINK = "open_step_link";
        String ANONYMOUS_OPEN_STEPS_LINK = "open_step_link_anonymous";
    }

    interface Certificate {
        String COPY_LINK_CERTIFICATE = "certificate_copy_link";
        String SHARE_LINK_CERTIFICATE = "certificate_share";
        String ADD_LINKEDIN = "certificate_add_linkeding";
        String OPEN_IN_BROWSER = "certificate_open_browser";
        String CLICK_SHARE_MAIN = "certificate_click_share_main";
        String OPEN_CERTIFICATE_FROM_NOTIFICATION_CENTER = "certificate_notification_center";
    }

    interface Filters {
        String FILTERS_CANCELED = "filters_canceled";
        String FILTERS_NEED_UPDATE = "filters_need_update";
        String FILTER_APPLIED_IN_INTERFACE_WITH_PARAMS = "filters_params";
    }

    interface Exam {
        String START_EXAM = "exam_start";
        String SHOW_EXAM = "exam_shown_on_bind_view";
    }

    interface Profile {
        String CLICK_INSTRUCTOR = "profile_click_instructor";
        String CLICK_USER_IN_COMMENT = "profile_click_in_comment";
        String SHOW_LOCAL = "profile_show_my";
        String OPEN_NO_INTERNET = "profile_no_internet";
        String STREAK_NO_INTERNET = "profile_no_internet_streak";
        String OPEN_BY_LINK = "profile_open_by_link";
        String CLICK_IMAGE = "profile_click_avatar";
        String CLICK_STREAK_VALUE = "profile_click_streak";
        String CLICK_FULL_NAME = "profile_click_full_name";
        String OPEN_SCREEN_OVERALL = "profile_open_screen_overall";
        String CLICK_FULL_NAME_DRAWER = "profile_click_fill_name_drawer";
    }

    interface Streak {
        String SWITCH_NOTIFICATION_IN_MENU = "streak_switch_notification_state";
        String CHOOSE_INTERVAL_PROFILE = "streak_choose_interval_profile";
        String CHOOSE_INTERVAL_CANCELED_PROFILE = "streak_choose_interval_canceled_profile";
        String CHOOSE_INTERVAL_CANCELED = "streak_choose_interval_canceled";
        String CHOOSE_INTERVAL = "streak_choose_interval";
        String CAN_SHOW_DIALOG = "streak_can_show_dialog";
        String SHOW_DIALOG_UNDEFINED_STREAKS = "streak_show_dialog_undefined";
        String SHOW_DIALOG_POSITIVE_STREAKS = "streak_show_dialog_positive";
        String STREAK_NOTIFICATION_OPENED = "streak_notification_opened";
        String NEGATIVE_MATERIAL_DIALOG = "streak_material_dialog_negative";
        String POSITIVE_MATERIAL_DIALOG = "streak_material_dialog_positive";
        String SHOWN_MATERIAL_DIALOG = "streak_material_dialog_shown";
        String GET_NON_ZERO_STREAK_NOTIFICATION = "streak_get_non_zero_notification";
        String GET_ZERO_STREAK_NOTIFICATION = "streak_get_zero_notification";
        String GET_NO_INTERNET_NOTIFICATION = " streak_get_no_internet_notification";

        String NOTIFICATION_TYPE_PARAM = "type";

        enum NotificationType {
            zero, noInternet, solvedToday, notSolvedToday
        }
    }

    interface Shortcut {
        String OPEN_PROFILE = "shortcut_open_profile";
        String FIND_COURSES = "shortcut_find_courses";
    }

    interface Anonymous {
        String JOIN_COURSE = "click_join_course_anonymous";
        String BROWSE_COURSES_CENTER = "click_anonymous_browse_courses_center";
        String AUTH_CENTER = "click_anonymous_auth_center";
        String BROWSE_COURSES_DRAWER = "click_anonymous_auth_center";
        String AUTH_DRAWER = "click_anonymous_auth_drawer";
        String SUCCESS_LOGIN_AND_ENROLL = "success_login_insta_enroll";
    }

    interface Downloading {
        String DOWNLOAD_MANAGER_IS_NOT_ENABLED = "download_manager_is_not_enabled";

        String SHOW_SNACK_PREFS_SECTIONS = "downloading_show_snack_prefs_sections";
        String SHOW_SNACK_INTERNET_SECTIONS = "downloading_show_snack_internet_sections";
        String CLICK_SETTINGS_SECTIONS = "downloading_click_settings_sections";
        String CLICK_RETRY_SECTIONS = "downloading_click_retry_sections";

        String SHOW_SNACK_PREFS_UNITS = "downloading_show_snack_prefs_units";
        String SHOW_SNACK_INTERNET_UNITS = "downloading_show_snack_internet_units";
        String CLICK_SETTINGS_UNITS = "downloading_click_settings_units";
        String CLICK_RETRY_UNITS = "downloading_click_retry_units";

    }

    interface SmartLock {
        String READ_CREDENTIAL_WITHOUT_INTERACTION = "smartlock_read_without_interaction";
        String DISABLED_LOGIN = "smartlock_disabled_login";
        String DISABLED_REGISTRATION = "smartlock_disabled_registration";

        String SHOW_SAVE_LOGIN = "smartlock_show_save_login";
        String LOGIN_SAVED = "smartlock_login_saved";
        String LOGIN_NOT_SAVED = "smartlock_login_not_saved";

        String SHOW_SAVE_REGISTRATION = "smartlock_show_save_registration";
        String REGISTRATION_SAVED = "smartlock_registration_saved";
        String REGISTRATION_NOT_SAVED = "smartlock_registration_not_saved";

        String PROMPT_TO_CHOOSE_CREDENTIALS = "smartlock_prompt_to_choose_credentials";
        String LAUNCH_CREDENTIAL_RETRIEVED_PROMPT = "smartlock_launch_credential_retrieved_prompt";
        String LAUNCH_CREDENTIAL_CANCELED_PROMPT = "smartlock_launch_credential_canceled_prompt";
        String CREDENTIAL_DELETED_FAIL = "smartlock_credential_deleted_fail";
        String CREDENTIAL_DELETED_SUCCESSFUL = "smartlock_credential_deleted_successful";
    }

    interface RemoteConfig {
        String FETCHED_SUCCESSFUL = "remote_fetched_successful";
        String FETCHED_UNSUCCESSFUL = "remote_fetched_unsuccessful";
    }

    interface ContinueExperiment {
        String CONTINUE_OLD = "experiment_continue_old";
        String CONTINUE_NEW = "experiment_continue_new";
        String COURSE_OLD = "experiment_continue_course_old";// click whole course (only for enrolled)
        String COURSE_NEW = "experiment_continue_course_new";// click whole course (only for enrolled)
    }

    interface CourseDetailScreen {
        String ANONYMOUS = "course_detail_anonymous";
        String ENROLLED = "course_detail_enrolled";
        String NOT_ENROLLED = "course_detail_not_enrolled";
    }

    void reportEvent(String eventName, Bundle bundle);

    void reportEvent(String eventName, String id);

    void reportEventWithIdName(String eventName, String id, @Nullable String name);

    void reportEventWithName(String eventName, @Nullable String name);

    void reportEvent(String eventName);

    void reportError(String message, @NotNull Throwable throwable);

    void setUserId(@NotNull String userId);

    void reportEventValue(String eventName, long value);

}
