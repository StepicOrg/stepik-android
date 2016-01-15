package org.stepic.droid.util;


public class AppConstants {

    public static final String USER_LOG_IN = "user_login_clicked";
    public static final String KEY_COURSE_BUNDLE = "course";
    public static final String KEY_SECTION_BUNDLE = "section";
    public static final String KEY_UNIT_BUNDLE = "unit";
    public static final String KEY_LESSON_BUNDLE = "lesson";
    public static final String KEY_STEP_BUNDLE = "step";
    public static final String DEFAULT_QUALITY = "270";
    public static final String PRE_BODY = "<html>\n" +
            "<head>\n" +
            "<title>Step. Stepic.org</title>\n" +
            "<script type=\"text/x-mathjax-config\">\n" +
            "  MathJax.Hub.Config({tex2jax: {inlineMath: [['$','$'], ['\\\\(','\\\\)']]}});\n" +
            "</script>\n" +
            "<script type=\"text/javascript\"\n" +
            " src=\"http://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML\">\n" +
            "</script>\n" +

            "<style>\n" +
            "\nimg { max-width: 100%; }" +
            "</style>\n" +
            "</head>\n" +
            "<body>";
    public static final String POST_BODY = "</body>\n" +
            "</html>";
    public static final String KEY_LOAD_TYPE = "KEY_LOAD_TYPE";
    public static final String KEY_TABLE_TYPE = "table_type";
    public static final int UI_UPDATING_TIME = 1000;


    //Types of steps:
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_MATCHING = "matching";
    public static final String TYPE_SORTING = "sorting";
    public static final String TYPE_MATCH = "match";
    public static final String TYPE_FREE_ANSWER = "free-answer";
    public static final String TYPE_TABLE = "table";
    public static final String TYPE_STRING = "string";
    public static final String TYPE_CHOICE = "choice";
    public static final String TYPE_NUMBER = "number";
    public static final String TYPE_DATASET = "dataset";
    public static final String TYPE_ANIMATION = "animation";
    public static final String TYPE_CHEMICAL = "chemical";
    public static final String TYPE_FILL_BLANKS = "fill-blanks";
    public static final String TYPE_PUZZLE = "puzzle";
    public static final String TYPE_PYCHARM = "pycharm";
    public static final String TYPE_CODE = "code";

    //App Metrica:
    public static final String METRICA_CLICK_SIGN_IN = "click sign in on launch screen";
    public static final String METRICA_CLICK_SIGN_UP = "click sign up";
    public static final String METRICA_CLICK_SIGN_IN_ON_SIGN_IN_SCREEN = "click sign in on sign in on sign-in screen";
    public static final String METRICA_FAIL_LOGIN = "fail login";
    public static final String METRICA_SUCCESS_LOGIN = "success login";
    public static final String METRICA_DROP_COURSE = "drop course";
    public static final String METRICA_LOAD_SERVICE = "Load Service";
    public static final String METRICA_REFRESH_COURSE = "Pull from top to refresh course";
    public static final String METRICA_REFRESH_SECTION = "Pull from top to refresh section";
    public static final String METRICA_REFRESH_UNIT = "Pull from top to refresh section unit";
    public static final String METRICA_LONG_TAP_COURSE = "Long tap on course";
    public static final java.lang.String SHOW_DETAILED_INFO_CLICK = "Show detailed info click from context menu of course";
    public static final java.lang.String METRICA_CLICK_CACHE_COURSE = "Click cache course";

    public static final java.lang.String METRICA_CLICK_DELETE_COURSE = "Click delete course from cache";
    public static final java.lang.String METRICA_CLICK_DELETE_SECTION = "Click delete section from cache";
    public static final java.lang.String METRICA_CLICK_CACHE_SECTION = "Click cache section";
    public static final java.lang.String METRICA_CLICK_CACHE_UNIT = "Click cache unit";
    public static final java.lang.String METRICA_CLICK_DELETE_UNIT = "Click delete unit from cache";
    public static final java.lang.String METRICA_CLICK_LOGOUT = "Click logout";
    public static final java.lang.String METRICA_CLICK_CLEAR_CACHE = "Click clear cache button";
    public static final java.lang.String METRICA_CLICK_YES_CLEAR_CACHE = "Click Accept clear cache";
    public static final java.lang.String METRICA_CLICK_YES_LOGOUT = "Click accept logout";
    public static final int REQUEST_WIFI = 1;
    public static final java.lang.String METRICA_CANCEL_VIDEO_QUALITY = "Cancel video quality dialog";
    public static final String NULL_SHOW_PROFILE = "Null profile is tried to show";
    public static final String IMAGE_ON_DISK = "Image on disk";
    public static final String METRICA_GET_PROGRESSES = "Get progresses";
    public static final String METRICA_GET_ASSIGNMENTS = "Get assignments";
    public static final String KEY_ASSIGNMENT_BUNDLE = "key_assignment";
    public static final String COURSE_ID_KEY = "course_id";
    public static final String ENROLLMENT_KEY = "is_enrolled";

    public static final int REQUEST_CODE_DETAIL = 1;
    public static final java.lang.String METRICA_YES_CLEAR_VIDEOS = "clear videos from downloads";
    public static final String NULL_SECTION = "Null section is not expected";
    public static final String NULL_COURSE = "Null course is not expected";
    public static final String NOT_VALID_ACCESS_AND_REFRESH = "Not valid access, why?";
    public static final String NOT_FOUND_VERSION = "Not found version of app";
    public static final String NOT_SIGNIFICANT_ERROR = "Not significant error, app will continue to work";
    public static final java.lang.String GET_OLD_ATTEMPT = "get attempt from server db, new is not creating";
    public static final java.lang.String SAVE_SESSION_FAIL = "save session was failed because of null attempt or submission";
}
