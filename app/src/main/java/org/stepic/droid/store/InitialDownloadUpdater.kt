package org.stepic.droid.store

import android.app.DownloadManager
import android.support.annotation.WorkerThread
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.model.DownloadEntity
import org.stepic.droid.store.operations.DatabaseFacade
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Singleton

@Singleton
class InitialDownloadUpdater(private val threadPoolExecutor: ThreadPoolExecutor,
                             private val systemDownloadManager: DownloadManager,
                             private val downloadFinishedCallback: DownloadFinishedCallback,
                             private val mainHandler: IMainHandler,
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
        val cursor = systemDownloadManager.query(query)
        cursor.use { cursor ->
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
        mainHandler.post {
            downloadFinishedCallback.onDownloadCompleted(downloadEntityCompleted, status == STATUS.COMPLETE)
        }
    }


    private enum class STATUS {
        COMPLETE,
        FAIL
    }
}