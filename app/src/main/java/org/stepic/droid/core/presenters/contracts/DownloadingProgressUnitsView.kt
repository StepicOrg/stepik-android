package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.UnitLoadingState

interface DownloadingProgressUnitsView {
    fun onNewProgressValue(unitLoadingState: UnitLoadingState)
}
