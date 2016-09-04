package org.stepic.droid.analytic;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

public interface Analytic {

    interface Preferences {
        String VIDEO_QUALITY = "video quality was chosen";
    }

    interface Interaction {
        String CLICK_SIGN_IN = "click sign in on launch screen";
        String CLICK_SIGN_UP = "click sign up";
        String CLICK_SIGN_IN_ON_SIGN_IN_SCREEN = "click sign in on sign in on sign-in screen";
        String CLICK_DELETE_SECTION = "Click delete section from cache";
        String CLICK_CACHE_SECTION = "Click cache section";
        String CLICK_CACHE_UNIT = "Click cache unit";
        String CLICK_DELETE_UNIT = "Click delete unit from cache";
        String CLICK_LOGOUT = "Click logout";
        String CLICK_CLEAR_CACHE = "Click clear cache button";
        String CLICK_YES_LOGOUT = "Click accept logout";
        String CANCEL_VIDEO_QUALITY = "Cancel video quality dialog";
        String YES_CLEAR_VIDEOS = "clear videos from downloads";
        String DELETE_COMMENT_TRIAL = "comment: delete comment trial";
        String UPDATING_MESSAGE_IS_APPROVED = "updating approved";
        String PULL_TO_REFRESH_COURSE = "Pull from top to refresh course";
        String COURSE_USER_TRY_FAIL = "course: user open failed for him course";
        String JOIN_COURSE_NULL = "course is null when join, detail";
        String CANCEL_CHOOSE_STORE_CLICK = "storage: cancel choice";
        String AUTH_FROM_DIALOG_FOR_UNAUTHORIZED_USER = "Auth: yes from auth dialog";
        String TRANSFER_DATA_YES = "storage: transfer data";
        String CLICK_CANCEL_SECTION = "click cancel section";
        String CLICK_CANCEL_UNIT = "click cancel unit";
        String UPDATING_MESSAGE_IS_SHOWN = "updating shown";
        String REFRESH_UNIT = "Pull from top to refresh section unit";
        String REFRESH_SECTION = "Pull from top to refresh section";
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
        String SHOW_STORAGE_MANAGEMENT = "show storage management";
        String OPEN_COMMENT_NOT_AVAILABLE = "comment: not available";
        String OPEN_COMMENT = "comments: open list";
        String OPEN_WRITE_COMMENT = "comments: open write form";
        String SHOW_SECTIONS = "Screen manager: show section";
        String SHOW_UNITS = "Screen manager: show units-lessons screen";
        String SHOW_STEP = "Screen manager: show steps of lesson";
        String OPEN_STEP_IN_WEB = "Screen manager: open Step in Web";
        String REMIND_PASSWORD = "Screen manager: remind password";
        String OPEN_LINK_IN_WEB = "open_link";
    }

    interface Video {
        String OPEN_EXTERNAL = "video open external";
        String OPEN_NATIVE = "video open native";
        String NOT_COMPATIBLE = "video is not compatible";
        String VLC_HARDWARE_ERROR = "video player: vlc error hardware";
        String INVALID_SURFACE_SIZE = "video player: Invalid surface size";
        String SHOW_CHOOSE_RATE = "video player: showChooseRateMenu";
        String JUMP_FORWARD = "video player: onJumpForward";
        String JUMP_BACKWARD = "video player: onJumpBackward";
        String START_LOADING = "video player: startLoading";
        String STOP_LOADING = "video player: stopLoading";
    }

    interface AppIndexing {
        String COURSE_DETAIL = "appindexing_course_detail";
        String COURSE_SYLLABUS = "appindexing_course_syllabus";
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
        String NULL_SHOW_PROFILE = "Null profile is tried to show";
        String REGISTRATION_IMPORTANT_ERROR = "registration important error";
        String NOTIFICATION_NOT_POSTED_ON_CLICK = "notification is not posted";
        String NULL_COURSE = "Null course is not expected";
        String NULL_SECTION = "Null section is not expected";
        String LESSON_IN_STORE_STATE_NULL = "lesson was null in store state manager";
        String UNIT_IN_STORE_STATE_NULL = "unit was null in store state manager";
        String LOAD_SERVICE = "Load Service";
        String PUSH_STATE_EXCEPTION = "Push state exception";
        String CANT_CREATE_NOMEDIA = "can't create .nomedia";
        String FAIL_LOGIN = "fail login";
        String CONFIG_NOT_PARSED = "configRelease, config.json problem";
        String ILLEGAL_STATE_NEXT_LESSON = "cant_show_next_lesson";
        String ILLEGAL_STATE_PREVIOUS_LESSON = "cant_show_previous_lesson";
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
        String HTML_WAS_NULL = "notification action is not support";
        String WAS_MUTED = "notification html text was muted";
        String NOT_SUPPORT = "notification is not support";
        String LEARN_SHOWN = "notification learn is shown";
        String CANT_PARSE_COURSE_ID = "notification, cant parse courseId";
        String TOKEN_UPDATED = "notification gcm token is updated";
        String TOKEN_UPDATE_FAILED = "notification gcm token is not updated";
    }

    interface Feedback {
        String FAILED_ON_SERVER = "Feedback is failed due to server";
        String INTERNET_FAIL = "Feedback internet fail";
    }

    interface Comments {
        String CLICK_SEND_COMMENTS = "comments: click send comment";
        String COMMENTS_SENT_SUCCESSFULLY = "comments: comment was sent successfully";
        String DELETE_COMMENT_CONFIRMATION = "comment: delete comment confirmed";
        String ORDER_TREND = "order_trend";
        String SHOW_CONFIRM_DISCARD_TEXT_DIALOG = "comment_discard_dialog_show";
        String SHOW_CONFIRM_DISCARD_TEXT_DIALOG_SUCCESS = "comment_discard_ok";
    }

    interface Steps {
        String CORRECT_SUBMISSION_FILL = "submission_correct_fill"; // it can be existing submission, use in chain.
        String WRONG_SUBMISSION_FILL = "submission_wrong_fill";
    }

    interface Calendar {
        String USER_CLICK_ADD_WIDGET = "calendar_click_add_widget";
        String USER_CLICK_ADD_MENU = "calendar_click_add_menu";
        String CALENDAR_ADDED_SUCCESSFULLY = "calendar_added_successfully";
        String CALENDAR_ADDED_FAIL = "calendar_added_fail";
        String SHOW_CALENDAR_AS_WIDGET = "calendar_shown_as_widget";
        String SHOW_CALENDAR = "calendar_shown"; // course with deadlines in future
        String HIDE_WIDGET_FROM_PREFS = "widget_hidden_from_prefs";
    }

    interface DeepLink {
        String USER_OPEN_SYLLABUS_LINK = "open_syllabus_by_link";
        String USER_OPEN_COURSE_DETAIL_LINK = "open_detail_course_link";
    }

    interface Certificate {
        String COPY_LINK_CERTIFICATE = "certificate_copy_link";
        String SHARE_LINK_CERTIFICATE = "certificate_share";
        String ADD_LINKEDIN = "certificate_add_linkeding";
    }

    interface Filters {
        String FILTERS_NOT_CHANGED = "filters_not_changed";
        String FILTERS_NEED_UPDATE = "filters_need_update";
        String FILTER_APPLIED_IN_INTERFACE_WITH_PARAMS = "filters_params";
    }

    void reportEvent(String eventName, Bundle bundle);

    void reportEvent(String eventName, String id);

    void reportEventWithIdName(String eventName, String id, String name);

    void reportEvent(String eventName);

    void reportError(String message, @NotNull Throwable throwable);

    void setUserId(@NotNull String userId);

    void reportEventValue(String eventName, long value);
}
