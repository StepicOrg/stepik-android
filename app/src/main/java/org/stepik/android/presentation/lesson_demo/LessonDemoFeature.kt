package org.stepik.android.presentation.lesson_demo

import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseDataResult
import org.stepik.android.presentation.wishlist.WishlistOperationFeature

interface LessonDemoFeature {
    sealed class State {
        object Idle : State()
        data class Content(
            val course: Course,
            val lessonDemoState: LessonDemoState,
            val wishlistOperationState: WishlistOperationFeature.State
        ) : State()
    }

    sealed class Message {
        data class InitMessage(val course: Course, val forceUpdate: Boolean = false) : Message()
        data class FetchLessonDemoDataSuccess(val deeplinkPromoCode: DeeplinkPromoCode, val coursePurchaseDataResult: CoursePurchaseDataResult) : Message()
        object FetchLessonDemoDataFailure : Message()

        object BuyActionMessage : Message()

        /**
         * Wishlist message wrapper
         */
        data class WishlistMessage(val wishlistMessage: WishlistOperationFeature.Message) : Message()
    }

    sealed class Action {
        data class FetchLessonDemoData(val course: Course) : Action()

        /**
         * Wishlist action wrapper
         */
        data class WishlistAction(val action: WishlistOperationFeature.Action) : Action()
        sealed class ViewAction : Action() {
            data class BuyAction(val deeplinkPromoCode: DeeplinkPromoCode, val coursePurchaseData: CoursePurchaseData?) : ViewAction()
        }
    }

    sealed class LessonDemoState {
        object Idle : LessonDemoState()
        object Loading : LessonDemoState()
        object Error : LessonDemoState()
        object Unavailable : LessonDemoState()
        data class Content(val deeplinkPromoCode: DeeplinkPromoCode, val coursePurchaseData: CoursePurchaseData?) : LessonDemoState()
    }
}