package org.stepik.android.presentation.course_revenue

interface CourseRevenueFeature {
    data class State(
        val courseBenefitState: CourseBenefitState,
        val courseBenefitSummaryState: CourseBenefitSummaryFeature.State,
        val courseBenefitsState: CourseBenefitsFeature.State
    )

    sealed class CourseBenefitState {
        object Idle : CourseBenefitState()
        object Loading : CourseBenefitState()
        object Error : CourseBenefitState()
        object Content : CourseBenefitState()
    }

    sealed class Message {
        data class InitMessage(val courseId: Long, val forceUpdate: Boolean = false) : Message()
        /**
         * Message Wrappers
         */
        data class CourseBenefitSummaryMessage(val message: CourseBenefitSummaryFeature.Message) : Message()
        data class CourseBenefitsMessage(val message: CourseBenefitsFeature.Message) : Message()
    }

    sealed class Action {
        sealed class ViewAction : Action()
        /**
         * Action Wrappers
         */
        data class CourseBenefitSummaryAction(val action: CourseBenefitSummaryFeature.Action) : Action()
        data class CourseBenefitsAction(val action: CourseBenefitsFeature.Action) : Action()
    }
}