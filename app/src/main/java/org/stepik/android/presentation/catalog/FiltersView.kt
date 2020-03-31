package org.stepik.android.presentation.catalog

import org.stepic.droid.model.StepikFilter
import java.util.EnumSet

interface FiltersView {
    fun onFiltersPrepared(filters: EnumSet<StepikFilter>)
}