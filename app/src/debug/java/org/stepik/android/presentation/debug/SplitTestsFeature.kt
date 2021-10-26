package org.stepik.android.presentation.debug

import org.stepik.android.domain.debug.model.SplitTestData

class SplitTestsFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Content(val splitTestDataList: List<SplitTestData>) : State()
    }

    sealed class Message {
        object InitMessage : Message()
        data class InitSuccess(val splitTestDataList: List<SplitTestData>) : Message()
        data class ChosenGroup(val splitTestData: SplitTestData) : Message()
        data class SetSplitTestDataSuccess(val splitTestData: SplitTestData) : Message()
    }

    sealed class Action {
        object FetchSplitTestData : Action()
        data class SetSplitTestData(val splitTestData: SplitTestData) : Action()
        sealed class ViewAction : Action()
    }
}