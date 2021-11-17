package org.stepic.droid.configuration

object RemoteConfig {
    const val PREFIX = "remote_config_"

    const val MIN_DELAY_RATE_DIALOG_SEC = "min_delay_rate_dialog_sec"
    const val SHOW_STREAK_DIALOG_AFTER_LOGIN = "show_streak_dialog_after_login"
    const val ADAPTIVE_COURSES = "adaptive_courses_android"
    const val ADAPTIVE_BACKEND_URL = "adaptive_backend_url"
    const val IS_LOCAL_SUBMISSIONS_ENABLED = "is_local_submissions_enabled"
    const val SEARCH_QUERY_PARAMS_ANDROID = "search_query_params_android"
    const val IS_NEW_HOME_SCREEN_ENABLED = "is_new_home_screen_enabled"
    const val PERSONALIZED_ONBOARDING_COURSE_LISTS = "personalized_onboarding_course_lists"
    const val IS_COURSE_REVENUE_AVAILABLE_ANDROID = "is_course_revenue_available_android"
    const val PURCHASE_FLOW_ANDROID = "purchase_flow_android"

    // TODO APPS-3443: Remove after finishing feature
    const val PURCHASE_FLOW_ANDROID_TESTING_FLAG = true
}
