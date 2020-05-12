package org.stepic.droid.analytic;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface Analytic {
    interface Adaptive {
        String REACTION_EASY = "reaction_easy";
        String REACTION_HARD = "reaction_hard";

        String REACTION_EASY_AFTER_CORRECT = "reaction_easy_after_correct_answer";
        String REACTION_HARD_AFTER_CORRECT = "reaction_hard_after_correct_answer";

        String ADAPTIVE_MODE_DISABLED = "adaptive_mode_disabled";
    }

    interface Onboarding {
        String CLOSED = "onboarding_closed";
        String ACTION = "onboarding_action";
        String COMPLETE = "onboarding_complete";
        String SCREEN_OPENED = "onboarding_screen_opened";

        String SCREEN_PARAM = "screen";
    }

    interface Code {
        String TOOLBAR_SELECTED = "code_toolbar_selected";
        String TOOLBAR_SELECTED_LANGUAGE = "language";
        String TOOLBAR_SELECTED_SYMBOL = "symbol";
        String TOOLBAR_SELECTED_LANGUAGE_SYMBOL = "language_symbol";

        String CODE_EDITOR_ERROR = "code_editor_error";
    }

    interface Search {
        String SEARCH_NULL = "search_null";
        String SEARCH_QUERY = "search_with_query";
        String SEARCH_OPENED = "search_opened";
        String SEARCH_SUBMITTED = "search_submitted";
        String SEARCH_SUGGESTION_CLICKED = "search_suggestion_clicked";
    }

    interface FastContinue {
        String EMPTY_COURSES_SHOWN = "fast_continue_empty_courses";
        String EMPTY_COURSES_CLICK = "fast_continue_empty_courses_click";

        String AUTH_SHOWN = "fast_continue_auth";
        String AUTH_CLICK = "fast_continue_auth_click";

        String CONTINUE_SHOWN = "fast_continue_shown";
        String CONTINUE_CLICK = "fast_continue_click";
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
        String REQUEST_LOGIN_WITH_INTERACTION_TYPE = "click_sign_in_with_interaction_type";

        String TAP_ON_FIELDS = "tap_on_fields_login";
        String TYPING_TEXT_FIELDS = "typing_text_fields_login";

        String FACEBOOK_ERROR = "facebook_error";
        String GOOGLE_FAILED_STATUS = "google_sign_in_failed";
        String GOOGLE_AUTH_CODE_NULL = "google_auth_code_null";
    }

    interface Preferences {
        String VIDEO_QUALITY = "video quality was chosen";
    }

    interface Interaction {
        String CLICK_SIGN_IN = "click sign in on launch screen";
        String CLICK_SIGN_UP = "click sign up";
        String CLICK_SIGN_IN_ON_SIGN_IN_SCREEN = "click sign in on sign in on sign-in screen";
        String CLICK_SIGN_IN_NEXT_ON_SIGN_IN_SCREEN = "click_sign_in_next_sign_in_screen";
        String CLICK_CLEAR_CACHE = "Click clear cache button";
        String CLICK_YES_LOGOUT = "Click accept logout";
        String CANCEL_VIDEO_QUALITY = "Cancel video quality dialog";
        String CANCEL_VIDEO_QUALITY_DETAILED = "cancel_detailed_video";
        String DELETE_COMMENT_TRIAL = "comment: delete comment trial";
        String CANCEL_CHOOSE_STORE_CLICK = "storage: cancel choice";
        String AUTH_FROM_DIALOG_FOR_UNAUTHORIZED_USER = "Auth: yes from auth dialog";
        String TRANSFER_DATA_YES = "storage: transfer data";
        String SUCCESS_LOGIN = "success login";
        String CLICK_REGISTER_BUTTON = "click_register_register_screen";
        String CLICK_SIGN_IN_SOCIAL = "social_login";
        String CLICK_SETTINGS_FROM_NOTIFICATION = "click_settings_from_notification";
        String START_SPLASH = "user_start_splash_new";
        String START_SPLASH_EXPERT = "user_start_splash_expert";
        String CLICK_CHOOSE_NOTIFICATION_INTERVAL = "click_choose_notification_interval";
        String CLICK_PRIVACY_POLICY = "click_privacy_policy";
        String CLICK_TERMS_OF_SERVICE = "click_terms_of_service";
        String CLICK_SOCIAL_NETWORK = "settings_click_social_network";

        String CLICK_CONTINUE_COURSE = "click_continue_course";
        String CLICK_COURSE = "click_course";
        String JOIN_COURSE = "click_join_course";

        String USER_OPEN_IMAGE = "user_open_image";
        String SCREENSHOT = "screenshot";
        String GOOGLE_SOCIAL_IS_NOT_ENABLED = "google_social_is_not_enabled";
        String ACCEPT_DELETING_UNIT = "click_delete_unit_dialog";
        String ACCEPT_DELETING_SECTION = "click_delete_section_dialog";
        String SHOW_LAUNCH_SCREEN_AFTER_LOGOUT = "show_launch_screen_after_logout";
    }

    interface Course {
        String DROP_COURSE_SUCCESSFUL = "drop course successful";
        String DROP_COURSE_FAIL = "drop course fail";
    }

    interface Screens {
        String SHOW_LAUNCH = "Screen manager: show launch screen";
        String SHOW_REGISTRATION = "Screen manager: show registration";
        String SHOW_LOGIN = "Screen manager: show login";
        String SHOW_MAIN_FEED = "Screen manager: show main feed";
        String SHOW_COURSE_DESCRIPTION = "Screen manager: show course description";
        String OPEN_STORE = "Open google play, estimation";
        String TRY_OPEN_VIDEO = "video is tried to show";
        String SHOW_SETTINGS = "show settings";
        String SHOW_NOTIFICATION_SETTINGS = "show_notification_settings";
        String SHOW_STORAGE_MANAGEMENT = "show storage management";
        String OPEN_COMMENT_NOT_AVAILABLE = "comment: not available";
        String OPEN_COMMENT = "comments: open oldList";
        String OPEN_WRITE_COMMENT = "comments: open write form";
        String SHOW_STEP = "Screen manager: show steps of lesson";
        String OPEN_STEP_IN_WEB = "Screen manager: open Step in Web";
        String REMIND_PASSWORD = "Screen manager: remind password";
        String OPEN_LINK_IN_WEB = "open_link";

        String USER_OPEN_MY_COURSES = "main_choice_my_courses";
        String USER_OPEN_CATALOG = "main_choice_find_courses";
        String USER_OPEN_DOWNLOADS = "main_choice_downloads";
        String USER_OPEN_CERTIFICATES = "main_choice_certificates";
        String USER_OPEN_FEEDBACK = "main_choice_feedback";
        String USER_OPEN_NOTIFICATIONS = "main_choice_notifications";
        String USER_OPEN_SETTINGS = "main_choice_settings";
        String USER_LOGOUT = "main_choice_logout";
        String USER_OPEN_ABOUT_APP = "main_choice_about";
        String USER_OPEN_PROFILE = "main_choice_profile";
    }

    interface Video {
        String OPEN_EXTERNAL = "video_open_external";
        String OPEN_NATIVE = "video_open_native";
        String NOT_COMPATIBLE = "video_is_not_compatible";
        String SHOW_CHOOSE_RATE = "video_player_show_choose_rate_menu";
        String JUMP_FORWARD = "video_player_jump_forward";
        String JUMP_BACKWARD = "video_player_jump_backward";
        String SHOW_MORE_ITEMS = "video_player_show_more";
        String ROTATE_CLICKED = "video_player_rotate_clicked";
        String PLAYER_CREATED = "video_player_created";
        String PLAY = "video_player_play";
        String PAUSE = "video_player_pause";
        String CONNECTION_ERROR = "video_player_connection_error";
        String ERROR = "video_player_error";
        String QUALITY_MENU = "video_player_quality";
        String CANCEL_VIDEO_QUALITY = "video_player_quality_cancel";
        String NOW_PLAYING_WAS_NULL = "video_player_now_playing_null";

        String VIDEO_FILE_RESTORED = "video_file_restored";

        String VIDEO_AUTOPLAY_CHANGED = "video_autoplay_changed";
    }

    interface AppIndexing {
        String COURSE_DETAIL = "appindexing_course_detail";
        String COURSE_SYLLABUS = "appindexing_course_syllabus";
        String STEP = "appindexing_step";
    }

    interface Error {
        String CALLBACK_SOCIAL = "callback_from_social_login";
        String NOT_PLAYER = "NotPlayer";
        String CANT_UPDATE_TOKEN = "cant update token";
        String NOTIFICATION_ERROR_PARSE = "notification error parse";
        String GOOGLE_SERVICES_TOO_OLD = "google_services_too_old";
        String FAIL_REFRESH_TOKEN_ONLINE = "fail_refresh_token_online";
        String FAIL_REFRESH_TOKEN_ONLINE_EXTENDED = "fail_refresh_token_online_extended";
        String FAIL_REFRESH_TOKEN_INLINE_GETTING = "fail_refresh_token_online_get";
        String CHOICES_ARE_SMALLER = "choices_are_smaller";
        String PREVIOUS_VIEW_NOT_DETACHED = "previous_view_not_detached";
        String UNEXPECTED_VIEW = "unexpected_view";
        String ILLEGAL_STATE_VIDEO_STEP_PLAY = "illegal_state_video_step_play";
        String CANT_PARSE_QUALITY = "cant_parse_quality";
        String FEATURED_EMPTY = "error_featured_empty";
        String COURSE_COLLECTION_EMPTY = "course_collection_empty";

        String REGISTRATION_FAILED = "registration_failed";
        String SOCIAL_AUTH_FAILED = "social_auth_failed";
        String CREDENTIAL_AUTH_FAILED = "credential_auth_failed";
    }

    interface Web {
        String UPDATE_TOKEN_FAILED = "update is failed";
    }

    interface Notification {
        String DISABLED_BY_USER = "Notification is disabled by user in app";
        String ACTION_NOT_SUPPORT = "notification action is not support";
        String HTML_WAS_NULL = "notification_html_was_null";
        String WAS_MUTED = "notification_was_muted";
        String NOT_SUPPORT_TYPE = "notification_type_is_not_support"; // After checking action

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

        String NOTIFICATION_SCREEN_OPENED = "notification_screen_opened";
    }

    interface Comments {
        String CLICK_SEND_COMMENTS = "comments: click send comment";
        String COMMENTS_SENT_SUCCESSFULLY = "comments: comment was sent successfully";
        String DELETE_COMMENT_CONFIRMATION = "comment: delete comment confirmed";
        String ORDER_TREND = "order_trend";
    }

    interface Steps {
        String STEP_TYPE_KEY = "type";
        String CORRECT_SUBMISSION_FILL = "submission_correct_fill"; // it can be existing submission, use in chain.
        String WRONG_SUBMISSION_FILL = "submission_wrong_fill";
        String SHARE_OPEN_IN_BROWSER = "step_share_open_in_browser";
        String COPY_LINK = "step_share_copy";
        String SHARE_ALL = "steps_share_all";
        String SHOW_KEEP_ON_SCREEN = "steps_show_keep_on_screen";
        String SHOW_KEEP_OFF_SCREEN = "steps_show_keep_off_screen";
        String STEP_OPENED = "step_opened";
        String STEP_EDIT_OPENED = "step_edit_opened";
        String STEP_EDIT_COMPLETED = "step_edit_completed";
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
        String USER_OPEN_COURSE_DETAIL_LINK = "open_detail_course_link";
    }

    interface Certificate {
        String COPY_LINK_CERTIFICATE = "certificate_copy_link";
        String SHARE_LINK_CERTIFICATE = "certificate_share";
        String ADD_LINKEDIN = "certificate_add_linkeding";
        String OPEN_IN_BROWSER = "certificate_open_browser";
        String CLICK_SHARE_MAIN = "certificate_click_share_main";
        String OPEN_CERTIFICATE_FROM_NOTIFICATION_CENTER = "certificate_notification_center";
    }

    interface Profile {
        String OPEN_BY_LINK = "profile_open_by_link";
        String OPEN_SCREEN_OVERALL = "profile_open_screen_overall";
    }

    interface Streak {
        String SWITCH_NOTIFICATION_IN_MENU = "streak_switch_notification_state";
        String CHOOSE_INTERVAL_PROFILE = "streak_choose_interval_profile";
        String CHOOSE_INTERVAL_CANCELED_PROFILE = "streak_choose_interval_canceled_profile";
        String CHOOSE_INTERVAL_CANCELED = "streak_choose_interval_canceled";

        String CAN_SHOW_DIALOG = "streak_can_show_dialog";
        String SHOW_DIALOG_UNDEFINED_STREAKS = "streak_show_dialog_undefined";
        String SHOW_DIALOG_POSITIVE_STREAKS = "streak_show_dialog_positive";
        String STREAK_NOTIFICATION_OPENED = "streak_notification_opened";
        String NEGATIVE_MATERIAL_DIALOG = "streak_material_dialog_negative";
        String GET_NON_ZERO_STREAK_NOTIFICATION = "streak_get_non_zero_notification";
        String GET_ZERO_STREAK_NOTIFICATION = "streak_get_zero_notification";
        String GET_NO_INTERNET_NOTIFICATION = " streak_get_no_internet_notification";

        String SHOWN_MATERIAL_DIALOG = "streak_material_dialog_shown";
        String POSITIVE_MATERIAL_DIALOG = "streak_material_dialog_positive";
        String CHOOSE_INTERVAL = "streak_choose_interval"; //complete

        String NOTIFICATION_TYPE_PARAM = "type";

        enum NotificationType {
            zero, noInternet, solvedToday, notSolvedToday
        }

        String EARLY_DIALOG_SHOWN = "streak_early_shown";
        String EARLY_DIALOG_POSITIVE = "streak_early_positive";
        String EARLY_NOTIFICATION_COMPLETE = "streak_early_complete";
    }

    interface Shortcut {
        String OPEN_PROFILE = "shortcut_open_profile";
        String OPEN_CATALOG = "shortcut_find_courses";
    }

    interface Anonymous {
        String JOIN_COURSE = "click_join_course_anonymous";
        String BROWSE_COURSES_CENTER = "click_anonymous_browse_courses_center";
        String AUTH_CENTER = "click_anonymous_auth_center";
        String BROWSE_COURSES_DRAWER = "click_anonymous_auth_center";
        String SUCCESS_LOGIN_AND_ENROLL = "success_login_insta_enroll";
    }

    interface SmartLock {
        String READ_CREDENTIAL_WITHOUT_INTERACTION = "smartlock_read_without_interaction";
        String DISABLED_LOGIN = "smartlock_disabled_login";

        String SHOW_SAVE_LOGIN = "smartlock_show_save_login";
        String LOGIN_SAVED = "smartlock_login_saved";

        String PROMPT_TO_CHOOSE_CREDENTIALS = "smartlock_prompt_to_choose_credentials";
        String PROMPT_CREDENTIAL_RETRIEVED = "smartlock_prompt_credential_retrieved";

        String CREDENTIAL_DELETED_FAIL = "smartlock_credential_deleted_fail";
        String CREDENTIAL_DELETED_SUCCESSFUL = "smartlock_credential_deleted_successful";
    }

    interface RemoteConfig {
        String FETCHED_SUCCESSFUL = "remote_fetched_successful";
        String FETCHED_UNSUCCESSFUL = "remote_fetched_unsuccessful";
    }

    interface Deadlines {
        interface Params {
            String COURSE = "course";
            String HOURS = "hours";
            String BEFORE_DEADLINE = "before_deadline";
        }

        String PERSONAL_DEADLINE_MODE_OPENED = "personal_deadline_mode_opened";
        String PERSONAL_DEADLINE_MODE_CHOSEN = "personal_deadline_mode_chosen";
        String PERSONAL_DEADLINE_MODE_CLOSED = "personal_deadline_mode_closed";

        String PERSONAL_DEADLINE_CHANGE_PRESSED = "personal_deadline_change_pressed";

        String PERSONAL_DEADLINE_TIME_OPENED = "personal_deadline_time_opened";
        String PERSONAL_DEADLINE_TIME_CLOSED = "personal_deadline_time_closed";

        String PERSONAL_DEADLINE_DELETED = "personal_deadline_deleted";
        String PERSONAL_DEADLINE_TIME_SAVED = "personal_deadline_time_saved";

        String PERSONAL_DEADLINE_NOTIFICATION_OPENED = "personal_deadline_notification_opened";
    }

    interface DownloaderV2 {
        String RECEIVE_BAD_DOWNLOAD_STATUS = "downloader_v2_bad_download_status";
        String FILE_NOT_FOND = "downloader_v2_file_not_fond";

        String MOVE_DOWNLOADED_FILE_ERROR = "downloader_v2_move_downloaded_file";

        String SYSTEM_DOWNLOAD_ERROR = "downloader_v2_system_download_error";

        String ADD_TASK_ERROR = "downloader_v2_add_task_error";
        String REMOVE_TASK_ERROR = "downloader_v2_remove_task_error";

        String FILE_TRANSFER_ERROR = "downloader_v2_file_transfer_error";

        String CLICK_SETTINGS_SECTIONS = "downloading_click_settings_sections";

        interface Params {
            String DOWNLOAD_STATUS = "status";
        }

    }

    interface FontSize {
        String FONT_SIZE_SELECTED = "font_size_selected";

        interface Params {
            String SIZE = "size";
        }
    }

    interface Traces {
        String COURSE_CONTENT_LOADING = "course_content_loading";
    }

    void reportEvent(String eventName, Bundle bundle);

    void reportEvent(String eventName, String id);

    void reportEventWithIdName(String eventName, String id, @Nullable String name);

    void reportEventWithName(String eventName, @Nullable String name);

    void reportEvent(String eventName);

    void reportError(String message, @NotNull Throwable throwable);

    void setUserId(@NotNull String userId);

    void setCoursesCount(int coursesCount);
    void setSubmissionsCount(long submissionsCount, long delta);
    void setScreenOrientation(int orientation);
    void setStreaksNotificationsEnabled(boolean isEnabled);
    void setTeachingCoursesCount(int coursesCount);
    void reportAmplitudeEvent(@NotNull String eventName, @Nullable Map<String, Object> params);
    void reportAmplitudeEvent(@NotNull String eventName);

    void setUserProperty(@NotNull String name, @NotNull String value);

    void reportEventValue(String eventName, long value);

}
