package org.stepic.droid.core.presenters

import org.stepic.droid.core.presenters.contracts.FilterView
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.storage.operations.Table
import java.util.*

class FilterPresenter(
        val sharedPreferenceHelper: SharedPreferenceHelper) : PresenterBase<FilterView>() {

    private var isInitiated: Boolean = false

    fun acceptFilter(uiFilters: EnumSet<StepikFilter>, courseType: Table) {
        sharedPreferenceHelper.saveFilter(courseType, uiFilters)
        view?.onFilterAccepted()
    }

    fun initFiltersIfNeed(courseType: Table) {
        if (!isInitiated) {
            isInitiated = true
            val filters = sharedPreferenceHelper.getFilter(courseType)
            view?.onFiltersPreparedForView(filters)
        }
    }
}
