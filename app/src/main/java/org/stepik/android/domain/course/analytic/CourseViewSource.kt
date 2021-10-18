package org.stepik.android.domain.course.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.search_result.model.SearchResultQuery
import java.io.Serializable

sealed class CourseViewSource : AnalyticEvent, Serializable {
    object MyCourses : CourseViewSource() {
        override val name: String =
            "my_courses"
    }

    object Downloads : CourseViewSource() {
        override val name: String =
            "downloads"
    }

    object FastContinue : CourseViewSource() {
        override val name: String =
            "fast_continue"
    }

    class Search(query: SearchResultQuery) : CourseViewSource() {
        companion object {
            private const val PARAM_QUERY = "query"
        }

        override val name: String = "search"

        override val params: Map<String, Any> =
            mapOf(PARAM_QUERY to query.toMap())
    }

    class Collection(collectionId: Long) : CourseViewSource() {
        companion object {
            private const val PARAM_COLLECTION = "collection"
        }

        override val name: String = "collection"

        override val params: Map<String, Any> =
            mapOf(PARAM_COLLECTION to collectionId)
    }

    object Recommendation : CourseViewSource() {
        override val name: String = "recommendation"
    }

    class Query(query: CourseListQuery) : CourseViewSource() {
        companion object {
            private const val PARAM_QUERY = "query"
        }

        override val name: String = "query"

        override val params: Map<String, Any> =
            mapOf(PARAM_QUERY to query.toMap())
    }

    class Story(storyId: Long) : CourseViewSource() {
        companion object {
            private const val PARAM_STORY = "story"
        }

        override val name: String = "story"

        override val params: Map<String, Any> =
            mapOf(PARAM_STORY to storyId)
    }

    class DeepLink(url: String) : CourseViewSource() {
        companion object {
            private const val PARAM_URL = "url"
        }

        override val name: String = "deeplink"

        override val params: Map<String, Any> =
            mapOf(PARAM_URL to url)
    }

    object Notification : CourseViewSource() {
        override val name: String =
            "notification"
    }

    object Auth : CourseViewSource() {
        override val name: String =
            "auth"
    }

    object PurchaseReminderNotification : CourseViewSource() {
        override val name: String =
            "purchase_reminder_notification"
    }

    object Visited : CourseViewSource() {
        override val name: String =
            "visited"
    }

    object LessonDemoDialog : CourseViewSource() {
        override val name: String =
            "lesson_demo_dialog"
    }

    object SectionUnavailableDialog : CourseViewSource() {
        override val name: String =
            "section_unavailable_dialog"
    }

    object CourseCompleteDialog : CourseViewSource() {
        override val name: String =
            "course_complete_dialog"
    }

    object Wishlist : CourseViewSource() {
        override val name: String =
            "wishlist"
    }

    object UserReviews : CourseViewSource() {
        override val name: String =
            "user_reviews"
    }

    object CoursePurchase : CourseViewSource() {
        override val name: String =
            "course_purchase"
    }

    object Unknown : CourseViewSource() {
        override val name: String =
            "unknown"
    }
}