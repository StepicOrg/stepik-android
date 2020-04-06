package org.stepik.android.presentation.catalog

import org.stepic.droid.model.StepikFilter
import java.util.EnumSet

interface FiltersView {
    sealed class State {
        object Idle : State()
        object Empty : State()
        class FiltersLoaded(val filters: EnumSet<StepikFilter>) : State()
    }
    fun setState(state: State)
}