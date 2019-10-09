package org.stepic.droid.core.filters

import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.core.filters.contract.FiltersListener
import org.stepic.droid.core.filters.contract.FiltersPoster
import org.stepic.droid.model.StepikFilter
import java.util.EnumSet
import javax.inject.Inject

class FiltersPosterImpl
@Inject constructor(
        private val listenerContainer: ListenerContainer<FiltersListener>) : FiltersPoster {

    override fun filtersChanged(filters: EnumSet<StepikFilter>) {
        listenerContainer.asIterable().forEach { it.onFiltersChanged(filters) }
    }

}
