package org.stepic.droid.core.presenters

import org.stepic.droid.core.presenters.contracts.FilterView
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import java.util.*

class FilterPresenter(
        val sharedPreferenceHelper: SharedPreferenceHelper) : PresenterBase<FilterView>() {

    private var isInitiated: Boolean = false
    private var oldValues : EnumSet<StepikFilter>? = null

    fun acceptFilter(uiFilters: EnumSet<StepikFilter>) {
        sharedPreferenceHelper.saveFilter(uiFilters, oldValues)
        view?.onFilterAccepted()
    }

    fun initFiltersIfNeed() {
        if (!isInitiated) {
            isInitiated = true
            val filters = sharedPreferenceHelper.filter
            view?.onFiltersPreparedForView(filters)
        }
    }

    override fun attachView(view: FilterView) {
        super.attachView(view)
        if (oldValues == null) {
            oldValues = sharedPreferenceHelper.filter
        }
    }

}
