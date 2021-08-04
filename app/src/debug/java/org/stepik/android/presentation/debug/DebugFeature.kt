package org.stepik.android.presentation.debug

import org.stepik.android.domain.debug.model.DebugSettings

interface DebugFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Error : State()
        data class Content(val fcmToken: String) : State()
    }

    sealed class Message {
        data class InitMessage(val forceUpdate: Boolean = false) : Message()
        data class FetchDebugSettingsSuccess(val debugSettings: DebugSettings) : Message()
        object FetchDebugSettingsFailure : Message()
    }

    sealed class Action {
        object FetchDebugSettings : Action()
        sealed class ViewAction : Action()
    }
}