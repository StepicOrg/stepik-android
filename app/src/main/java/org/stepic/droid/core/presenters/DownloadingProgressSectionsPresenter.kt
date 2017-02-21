package org.stepic.droid.core.presenters

import org.stepic.droid.core.DownloadingProgressSectionPublisher
import org.stepic.droid.core.presenters.contracts.DownloadingProgressSectionsView
import org.stepic.droid.model.Section
import org.stepic.droid.model.SectionLoadingState

class DownloadingProgressSectionsPresenter(
        private val downloadingProgressSectionPublisher: DownloadingProgressSectionPublisher) : PresenterBase<DownloadingProgressSectionsView>() {

    private val downloadCallback = object : DownloadingProgressSectionPublisher.DownloadingProgressCallback {
        override fun onProgressChanged(sectionId: Long, newPortion: Float) {
            view?.onNewProgressValue(SectionLoadingState(sectionId, newPortion))
        }
    }

    fun subscribeToProgressUpdates(sections: List<Section>) {
        val sectionIds = sections.map(Section::id)
        downloadingProgressSectionPublisher.subscribe(sectionIds, downloadCallback)
    }

    override fun detachView(view: DownloadingProgressSectionsView) {
        super.detachView(view)
        downloadingProgressSectionPublisher.unsubscribe()
    }

}
