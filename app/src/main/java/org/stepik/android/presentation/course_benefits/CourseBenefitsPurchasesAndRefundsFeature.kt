package org.stepik.android.presentation.course_benefits

import org.stepik.android.domain.course_benefits.model.CourseBenefit

interface CourseBenefitsPurchasesAndRefundsFeature {
    sealed class State {
        object Loading : State()
        object Empty : State()
        object Error : State()
        data class Content(val courseBenefits: List<CourseBenefit>) : State()
    }

    sealed class Message {
        data class FetchCourseBenefitsSuccess(val courseBenefits: List<CourseBenefit>) : Message()
        object FetchCourseBenefitsFailure : Message()
    }

    sealed class Action {
        object FetchCourseBenefits : Action()
        sealed class ViewAction : Action()
    }
}