package org.stepic.droid.analytic

interface AmplitudeAnalytic {
    object Properties {
            const val STEPIK_ID = "stepik_id"
            const val SUBMISSIONS_COUNT = "submissions_count"
            const val COURSES_COUNT = "courses_count"
            const val SCREEN_ORIENTATION = "screen_orientation"
            const val APPLICATION_ID = "application_id"
            const val PUSH_PERMISSION = "push_permission"
            const val STREAKS_NOTIFICATIONS_ENABLED = "streaks_notifications_enabled"
    }

    object Launch {
        const val FIRST_TIME = "First time"
        const val SESSION_START = "Session start"
    }

    object Onboarding {
        const val SCREEN_OPENED = "Onboarding screen opened"
        const val CLOSED = "Onboarding closed"
        const val COMPLETED = "Onboarding completed"

        const val PARAM_SCREEN = "screen"
    }

    object Auth {
        const val LOGGED_ID = "Logged in"
        const val REGISTERED = "Registered"

        const val PARAM_SOURCE = "source"
    }

    object Course {
        const val JOINED = "Course joined"
        const val UNSUBSCRIBED = "Course unsubscribed"
        const val CONTINUE_PRESSED = "Continue course pressed"

        object Params {
            const val COURSE = "course"
            const val SOURCE = "source"
        }

        object Values {
            const val WIDGET = "widget"
            const val PREVIEW = "preview"

            const val COURSE_WIDGET = "course_widget"
            const val HOME_WIDGET = "home_widget"
        }
    }

    object Steps {
        const val SUBMISSION_MADE = "Submission made"
        const val STEP_OPENED = "Step opened"

        object Params {
            const val TYPE = "type"
            const val LANGUAGE = "language"
            const val NUMBER = "number"
            const val STEP = "step"
        }
    }

    object Downloads {
        const val STARTED = "Download started"
        const val CANCELLED = "Download cancelled"
        const val DELETED = "Download deleted"

        const val PARAM_CONTENT = "content"

        object Values {
            const val SECTION = "section"
            const val LESSON = "lesson"
        }
    }

    object Search {
        const val SEARCHED = "Course searched"

        const val PARAM_SUGGESTION = "suggestion"
    }
}