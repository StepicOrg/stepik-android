package org.stepik.android.presentation.debug

import org.stepic.droid.analytic.experiments.SplitTest
import org.stepik.android.domain.debug.model.SplitTestData

class SplitTestsFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Content(val splitTestDataList: List<SplitTestData>) : State()
    }

    sealed class Message {
        data class InitMessage(val splitTests: Set<SplitTest<*>>) : Message()
        data class InitSuccess(val splitTestDataList: List<SplitTestData>) : Message()
        data class ChosenGroup(val splitTestData: SplitTestData) : Message()
        data class SetSplitTestDataSuccess(val splitTestData: SplitTestData) : Message()
    }

    sealed class Action {
        data class FetchSplitTestData(val splitTests: Set<SplitTest<*>>) : Action()
        data class SetSplitTestData(val splitTestData: SplitTestData) : Action()
        sealed class ViewAction : Action()
    }
}