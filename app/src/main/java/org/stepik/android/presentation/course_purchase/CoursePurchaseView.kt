package org.stepik.android.presentation.course_purchase

interface CoursePurchaseView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Error : State()
        object Success : State()
    }
}