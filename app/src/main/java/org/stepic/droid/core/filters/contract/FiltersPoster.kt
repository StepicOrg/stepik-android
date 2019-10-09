package org.stepic.droid.core.filters.contract

import org.stepic.droid.model.StepikFilter
import java.util.EnumSet

interface FiltersPoster {
    fun filtersChanged(filters: EnumSet<StepikFilter>)
}
