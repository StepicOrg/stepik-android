package org.stepik.android.presentation.debug

import org.stepic.droid.analytic.experiments.SplitTest
import org.stepik.android.domain.debug.model.SplitGroupData

class SplitGroupFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Content(val splitGroupDataList: List<SplitGroupData>) : State()
    }

    sealed class Message {
        data class InitMessage(val splitGroups: Set<SplitTest<*>>) : Message()
        data class InitSuccess(val splitGroupDataList: List<SplitGroupData>) : Message()
        data class ChosenGroup(val splitGroupData: SplitGroupData) : Message()
        data class SetSplitGroupDataSuccess(val splitGroupData: SplitGroupData) : Message()
    }

    sealed class Action {
        data class FetchSplitGroupData(val splitGroups: Set<SplitTest<*>>) : Action()
        data class SetSplitGroupData(val splitGroupData: SplitGroupData) : Action()
        sealed class ViewAction : Action()
    }
}