package org.stepic.droid.analytic;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

public interface Analytic {
    String METRICA_FAIL_LOGIN = "fail login";
    String METRICA_SUCCESS_LOGIN = "success login";
    String DROP_COURSE = "drop course";
    String METRICA_LOAD_SERVICE = "Load Service";
    String METRICA_REFRESH_COURSE = "Pull from top to refresh course";
    String METRICA_REFRESH_SECTION = "Pull from top to refresh section";
    String METRICA_REFRESH_UNIT = "Pull from top to refresh section unit";
    String METRICA_LONG_TAP_COURSE = "Long tap on course";
    String METRICA_LESSON_IN_STORE_STATE_NULL = "lesson was null in store state manager";
    String METRICA_UNIT_IN_STORE_STATE_NULL = "unit was null in store state manager";

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
    }

    interface Web {
        String UPDATE_TOKEN_FAILED = "update is failed";
        String AUTH_LOGIN_PASSWORD = "Api:auth with login password";
        String AUTH_SOCIAL = "Api:auth with social account";
        String TRY_REGISTER = "Api: try register";
        String TRY_JOIN_COURSE = "Api:try join to course";
        String DROP_COURSE = "Api: drop course";
    }

    void reportEvent(String eventName, Bundle bundle);

    void reportEventWithId(String eventName, String id);

    void reportEventWithIdName(String eventName, String id, String name);

    void reportEvent(String eventName);

    void reportError(String message, @NotNull Throwable throwable);
}
