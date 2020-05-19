package org.stepik.android.domain.course.analytic

import com.google.gson.Gson
import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.domain.course_list.model.CourseListQuery
import java.io.Serializable

sealed class CourseViewSource : AnalyticEvent, Serializable {
    object MyCourses : CourseViewSource() {
        override val name: String =
            "my_courses"
    }

    class Search(query: String) : CourseViewSource() {
        companion object {
            private const val PARAM_QUERY = "query"
        }

        override val name: String = "search"

        override val params: Map<String, Any> =
            mapOf(PARAM_QUERY to query)
    }

    class Collection(collectionId: Long) : CourseViewSource() {
        companion object {
            private const val PARAM_COLLECTION = "collection"
        }

        override val name: String = "collection"

        override val params: Map<String, Any> =
            mapOf(PARAM_COLLECTION to collectionId)
    }

    class Query(query: CourseListQuery) : CourseViewSource() {
        companion object {
            private const val PARAM_QUERY = "query"
        }

        override val name: String = "query"

        override val params: Map<String, Any> =
            mapOf(PARAM_QUERY to Gson().toJson(query))
    }

    class Profile(profileId: Long) : CourseViewSource() {
        companion object {
            private const val PARAM_PROFILE = "profile"
        }

        override val name: String = "profile"

        override val params: Map<String, Any> =
            mapOf(PARAM_PROFILE to profileId)
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
}