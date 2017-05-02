package org.stepic.droid.storage

import android.app.DownloadManager
import android.database.Cursor
import android.support.annotation.WorkerThread
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.model.DownloadEntity
import org.stepic.droid.storage.operations.DatabaseFacade
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@AppSingleton
class InitialDownloadUpdater
@Inject constructor(private val threadPoolExecutor: ThreadPoolExecutor,
                    private val systemDownloadManager: DownloadManager,
                    private val storeStateManager: StoreStateManager,
                    private val databaseFacade: DatabaseFacade) {


    fun onCreateApp() {
        threadPoolExecutor.execute {
            val currentDownloadingEntitiesMap = databaseFacade
                    .getAllDownloadEntities()
                    .filterNotNull()
                    .associateBy(DownloadEntity::downloadId)
            checkDownloadEntitiesOnAppStarted(currentDownloadingEntitiesMap)
        }

    }

    /**
     * Check if download entities are loaded -> update states of lesson entities and after that remove download entities,
     * Events about completing of downloading should be received from DownloadCompleteReceiver, but sometimes app have been killed by
     * system and events is not received -> download entities are not removed and app load lesson infinitely.
     *
     * should be executed on singleThreadExecutor
     */
    @WorkerThread
    private fun checkDownloadEntitiesOnAppStarted(currentDownloadingEntitiesMap: Map<Long, DownloadEntity>) {
        if (currentDownloadingEntitiesMap.isEmpty()) {
            return
        }

        val query = DownloadManager.Query()
        query.setFilterById(*currentDownloadingEntitiesMap.keys.toLongArray())
        val cursor : Cursor? = systemDownloadManager.query(query)
        cursor?.use { cursor ->
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val columnStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                val downloadId = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID))

                val relatedDownloadEntity = currentDownloadingEntitiesMap.get(downloadId.toLong())
                relatedDownloadEntity?.let {
                    if (columnStatus == DownloadManager.STATUS_SUCCESSFUL) {
                        onDownloadEntityFinished(it, STATUS.COMPLETE)
                    } else if (columnStatus == DownloadManager.STATUS_FAILED) {
                        onDownloadEntityFinished(it, STATUS.FAIL)
                    }
                }

                if (relatedDownloadEntity != null && (columnStatus == DownloadManager.STATUS_SUCCESSFUL)) {

                }

                cursor.moveToNext()
            }
        }
    }

    @WorkerThread
    private fun onDownloadEntityFinished(downloadEntityCompleted: DownloadEntity, status: STATUS) {
        val step = databaseFacade.getStepById(downloadEntityCompleted.stepId)!!
        val lessonId = databaseFacade.getLessonById(step.lesson)!!.id
        when (status) {
            STATUS.COMPLETE -> storeStateManager.updateUnitLessonState(lessonId)
            STATUS.FAIL -> storeStateManager.updateUnitLessonAfterDeleting(lessonId)

        }
        databaseFacade.deleteDownloadEntityByDownloadId(downloadEntityCompleted.downloadId)
    }


    private enum class STATUS {
        COMPLETE,
        FAIL
    }
}