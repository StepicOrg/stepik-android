package org.stepik.android.presentation.features

interface FeaturesFeature {
    sealed class State {
        object Idle : State()

        object Loading : State()

        object Empty : State()

        data class Success(val rubricatorUrl: String) : State()
    }

    sealed class Message {
        object InitMessage : Message()

        data class FetchRubricatorUrlSuccess(val rubricatorUrl: String) : Message()

        object FetchRubricatorUrlError : Message()
    }

    sealed class Action {
        object FetchRubricatorUrl : Action()

        sealed class ViewAction : Action()
    }
}