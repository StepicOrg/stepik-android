package org.stepic.droid.core.downloads.contract

import org.stepic.droid.model.CachedVideo
import org.stepic.droid.model.DownloadingVideoItem
import org.stepic.droid.model.Lesson

interface DownloadsListener {
    fun onDownloadComplete(stepId: Long, lesson: Lesson, video: CachedVideo)

    fun onDownloadUpdate(downloadingVideoItem: DownloadingVideoItem)

    fun onFinishDownloadVideo(list: List<CachedVideo>, map: Map<Long, Lesson>)

    fun onStepRemoved(id: Long)
}
