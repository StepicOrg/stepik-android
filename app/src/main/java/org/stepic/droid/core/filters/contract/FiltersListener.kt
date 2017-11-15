package org.stepic.droid.core.filters.contract

import org.stepic.droid.model.StepikFilter
import java.util.*

interface FiltersListener {
    fun onFiltersChanged (filters : EnumSet<StepikFilter>)
}
