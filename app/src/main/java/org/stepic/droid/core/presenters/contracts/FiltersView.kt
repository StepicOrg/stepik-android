package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.StepikFilter
import java.util.EnumSet

interface FiltersView {
    fun onFiltersPrepared(filters: EnumSet<StepikFilter>)
}
