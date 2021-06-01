package org.stepik.android.presentation.course_complete

import org.stepik.android.domain.course_complete.model.CourseCompleteInfo
import org.stepik.android.model.Course

interface CourseCompleteFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Content(val courseCompleteInfo: CourseCompleteInfo)
        object NetworkError : State()
    }

    sealed class Message {
        data class Init(val course: Course) : Message()
        data class FetchCourseCompleteInfoSuccess(val courseCompleteInfo: CourseCompleteInfo) : Message()
        object FetchCourseCompleteError : Message()
    }

    sealed class Action {
        data class FetchCourseCompleteInfo(val course: Course) : Action()
        sealed class ViewAction : Action()
    }
}