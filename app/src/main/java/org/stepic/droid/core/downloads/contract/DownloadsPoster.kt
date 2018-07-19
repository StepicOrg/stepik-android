package org.stepic.droid.core.downloads.contract

import org.stepic.droid.model.CachedVideo
import org.stepic.droid.model.DownloadingVideoItem
import org.stepik.android.model.Lesson

interface DownloadsPoster {
    fun downloadComplete(stepId: Long, lesson: Lesson, video: CachedVideo)

    fun downloadFailed(downloadId: Long)

    fun downloadUpdate(downloadingVideoItem: DownloadingVideoItem)

    fun finishDownloadVideo(list: List<CachedVideo>, map: Map<Long, Lesson>)

    fun stepRemoved(id: Long)
}
