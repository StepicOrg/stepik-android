package org.stepik.android.presentation.lesson_demo

import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData

interface LessonDemoFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Error : State()
        data class Content(val deeplinkPromoCode: DeeplinkPromoCode, val coursePurchaseData: CoursePurchaseData?) : State()
    }

    sealed class Message {
        data class InitMessage(val forceUpdate: Boolean = false) : Message()
    }

    sealed class Action {
        object FetchLessonDemoData : Action()
        sealed class ViewAction : Action()
    }
}