package org.stepic.droid.persistence.downloads

import io.reactivex.Completable
import org.stepic.droid.persistence.model.DownloadRequest
import org.stepic.droid.persistence.model.PersistentItem

interface DownloadTaskManager {
    fun addTask(request: DownloadRequest): Completable
    fun removeTasks(items: List<PersistentItem>, shouldRemoveFromDb: Boolean = false): Completable
}