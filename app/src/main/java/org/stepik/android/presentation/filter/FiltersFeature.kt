package org.stepik.android.presentation.filter

import org.stepic.droid.model.StepikFilter
import java.util.EnumSet

interface FiltersFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        class FiltersLoaded(val filters: EnumSet<StepikFilter>) : State()
    }

    sealed class Message {
        data class InitMessage(val forceUpdate: Boolean = false) : Message()
        data class FiltersChanged(val filters: EnumSet<StepikFilter>) : Message()
        data class LoadFiltersSuccess(val filters: EnumSet<StepikFilter>) : Message()
        object LoadFiltersError : Message()
    }

    sealed class Action {
        object LoadFilters : Action()
        data class ChangeFilters(val filters: EnumSet<StepikFilter>) : Action()
        sealed class ViewAction : Action()
    }
}