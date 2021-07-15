package org.stepik.android.presentation.course_revenue

import org.stepik.android.domain.feedback.model.SupportEmailData

interface CourseRevenueFeature {
    data class State(
        val courseRevenueState: CourseRevenueState,
        val courseBenefitSummaryState: CourseBenefitSummaryFeature.State,
        val courseBenefitsState: CourseBenefitsFeature.State,
        val courseBenefitsMonthlyState: CourseBenefitsMonthlyFeature.State
    )

    sealed class CourseRevenueState {
        object Idle : CourseRevenueState()
        object Loading : CourseRevenueState()
        object Error : CourseRevenueState()
        object Content : CourseRevenueState()
    }

    sealed class Message {
        data class InitMessage(val courseId: Long, val forceUpdate: Boolean = false) : Message()
        data class SetupFeedback(val subject: String, val deviceInfo: String) : Message()
        data class SetupFeedbackSuccess(val supportEmailData: SupportEmailData) : Message()
        /**
         * Message Wrappers
         */
        data class CourseBenefitSummaryMessage(val message: CourseBenefitSummaryFeature.Message) : Message()
        data class CourseBenefitsMessage(val message: CourseBenefitsFeature.Message) : Message()
        data class CourseBenefitsMonthlyMessage(val message: CourseBenefitsMonthlyFeature.Message) : Message()
    }

    sealed class Action {
        data class GenerateSupportEmailData(val subject: String, val deviceInfo: String) : Action()
        sealed class ViewAction : Action() {
            data class ShowContactSupport(val supportEmailData: SupportEmailData) : ViewAction()
        }
        /**
         * Action Wrappers
         */
        data class CourseBenefitSummaryAction(val action: CourseBenefitSummaryFeature.Action) : Action()
        data class CourseBenefitsAction(val action: CourseBenefitsFeature.Action) : Action()
        data class CourseBenefitsMonthlyAction(val action: CourseBenefitsMonthlyFeature.Action) : Action()
    }
}