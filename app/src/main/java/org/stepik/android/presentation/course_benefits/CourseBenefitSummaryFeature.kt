package org.stepik.android.presentation.course_benefits

import org.stepik.android.domain.course_benefits.model.CourseBenefitSummary

interface CourseBenefitSummaryFeature {
    sealed class State {
        object Loading : State()
        object Empty : State()
        object Error : State()
        data class Content(val courseBenefitSummary: CourseBenefitSummary) : State()
    }

    sealed class Message {
        data class FetchCourseBenefitSummarySuccess(val courseBenefitSummary: CourseBenefitSummary) : Message()
        object FetchCourseBenefitSummaryFailure : Message()
    }

    sealed class Action {
        data class FetchCourseBenefitSummary(val courseId: Long) : Action()
        sealed class ViewAction : Action()
    }
}