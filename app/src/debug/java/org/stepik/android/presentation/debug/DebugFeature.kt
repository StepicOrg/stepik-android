package org.stepik.android.presentation.debug

import org.stepik.android.domain.debug.model.EndpointConfig
import org.stepik.android.domain.debug.model.DebugSettings

interface DebugFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Error : State()
        data class Content(val fcmToken: String, val currentEndpointConfig: EndpointConfig, val endpointConfigSelection: Int) : State()
    }

    sealed class Message {
        data class InitMessage(val forceUpdate: Boolean = false) : Message()
        data class FetchDebugSettingsSuccess(val debugSettings: DebugSettings) : Message()
        object FetchDebugSettingsFailure : Message()
        data class RadioButtonSelectionMessage(val position: Int) : Message()
        object ApplySettingsMessage : Message()
        object RestartApplicationMessage : Message()
    }

    sealed class Action {
        object FetchDebugSettings : Action()
        data class UpdateEndpointConfig(val endpointConfig: EndpointConfig) : Action()
        sealed class ViewAction : Action() {
            object RestartApplication : ViewAction()
        }
    }
}