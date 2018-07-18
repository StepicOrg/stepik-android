package org.stepic.droid.core.downloads

import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.core.downloads.contract.DownloadsListener
import org.stepic.droid.core.downloads.contract.DownloadsPoster
import org.stepic.droid.model.CachedVideo
import org.stepic.droid.model.DownloadingVideoItem
import org.stepik.android.model.structure.Lesson
import javax.inject.Inject

class DownloadsPosterImpl
@Inject constructor(
        private val listenerContainer: ListenerContainer<DownloadsListener>)
    : DownloadsPoster {
    override fun stepRemoved(id: Long) {
        listenerContainer.asIterable().forEach {
            it.onStepRemoved(id)
        }
    }

    override fun downloadUpdate(downloadingVideoItem: DownloadingVideoItem) {
        listenerContainer.asIterable().forEach {
            it.onDownloadUpdate(downloadingVideoItem)
        }
    }

    override fun downloadComplete(stepId: Long, lesson: Lesson, video: CachedVideo) {
        listenerContainer.asIterable().forEach {
            it.onDownloadComplete(stepId, lesson, video)
        }
    }

    override fun downloadFailed(downloadId: Long) {
        listenerContainer.asIterable().forEach {
            it.onDownloadFailed(downloadId)
        }
    }

    override fun finishDownloadVideo(list: List<CachedVideo>, map: Map<Long, Lesson>) {
        listenerContainer.asIterable().forEach {
            it.onFinishDownloadVideo(list, map)
        }
    }
}


