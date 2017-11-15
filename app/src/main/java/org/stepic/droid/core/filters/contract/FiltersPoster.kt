package org.stepic.droid.core.filters.contract

import org.stepic.droid.model.StepikFilter
import java.util.*

interface FiltersPoster {
    fun filtersChanged(filters: EnumSet<StepikFilter>)
}
