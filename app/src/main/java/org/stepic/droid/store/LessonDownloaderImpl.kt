package org.stepic.droid.store

import android.app.DownloadManager
import android.content.Context
import android.support.annotation.WorkerThread
import org.stepic.droid.concurrency.SingleThreadExecutor
import org.stepic.droid.model.DownloadEntity
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.web.IApi
import javax.inject.Singleton

@Singleton
class LessonDownloaderImpl(private val context: Context,
                           private val databaseFacade: DatabaseFacade,
                           private val api: IApi,
                           private val systemDownloadManager: DownloadManager,
                           private val singleThreadExecutor: SingleThreadExecutor) : LessonDownloader {

    private val currentDownloadingEntitiesMap = HashMap<Long, DownloadEntity>()

    init {
        singleThreadExecutor.execute {
            currentDownloadingEntitiesMap.putAll(databaseFacade
                    .getAllDownloadEntities()
                    .filterNotNull()
                    .associateBy(DownloadEntity::downloadId))
            checkDownloadEntitiesOnAppStarted()
        }
    }

    override fun downloadLesson(lessonId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cancelLessonLoading(lessonId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteWholeLesson(lessonId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Check if download entities are loaded -> update states of lesson entities and after that remove download entities,
     * Events about completing of downloading should be received from DownloadCompleteReceiver, but sometimes app have been killed by
     * system and events is not received -> download entities are not removed and app load lesson infinitely.
     *
     * should be executed on singleThreadExecutor
     */
    private fun checkDownloadEntitiesOnAppStarted() {
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
        //todo remove download entity from the local list, after that update states of related lesson/section?, after that remove from database
    }


    private enum class STATUS {
        COMPLETE,
        FAIL
    }

}
