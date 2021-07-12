package org.stepik.android.presentation.course_benefits

import org.stepik.android.domain.course_benefits.model.CourseBenefitListItem

interface CourseBenefitsPurchasesAndRefundsFeature {
    sealed class State {
        object Loading : State()
        object Empty : State()
        object Error : State()
        data class Content(val courseBenefitListItems: List<CourseBenefitListItem.Data>) : State()
    }

    sealed class Message {
        data class FetchCourseBenefitsSuccess(val courseBenefitListItems: List<CourseBenefitListItem.Data>) : Message()
        object FetchCourseBenefitsFailure : Message()
    }

    sealed class Action {
        data class FetchCourseBenefits(val courseId: Long) : Action()
        sealed class ViewAction : Action()
    }
}