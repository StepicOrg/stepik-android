package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.SectionLoadingState

interface DownloadingProgressSectionsView {

    fun onNewProgressValue(state: SectionLoadingState)

}
