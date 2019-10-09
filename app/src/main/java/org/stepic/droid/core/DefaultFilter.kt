package org.stepic.droid.core

import org.stepic.droid.model.StepikFilter

interface DefaultFilter {
    fun getDefaultFilter(filterValue: StepikFilter): Boolean
}
