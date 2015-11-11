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


}
