package org.stepic.droid.store

import android.content.Context
import android.support.annotation.MainThread
import org.stepic.droid.model.DownloadEntity
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.web.Api
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Singleton

@Singleton
class LessonDownloaderImpl(private val context: Context,
                           private val databaseFacade: DatabaseFacade,
                           private val api: Api,
                           private val downloadManager: IDownloadManager,
                           private val threadPoolExecutor: ThreadPoolExecutor,
                           private val cleanManager: CleanManager,
                           private val storeStateManager: StoreStateManager,
                           private val cancelSniffer: CancelSniffer) : LessonDownloader, DownloadFinishedCallback {

    override fun downloadLesson(lessonId: Long) {
        threadPoolExecutor.execute {
            val lesson = databaseFacade.getLessonById(lessonId) ?: throw Exception("lesson was null, when downloadLesson of LessonDownloader is executed") // FIXME: IT CAN BE BY THE NORMAL EXECUTION, CHANGE IT, THIS LESSON MAY BE NEEDED, WHEN SECTION IS LOADED
            val unitId = databaseFacade.getUnitByLessonId(lessonId)!!.id
            cancelSniffer.removeLessonIdToCancel(unitId)
            lesson.steps?.forEach {
                cancelSniffer.removeStepIdCancel(it)
            }
            downloadManager.addLesson(lesson)
        }
    }

    override fun cancelLessonLoading(lessonId: Long) {
        threadPoolExecutor.execute {
            val lesson = databaseFacade.getLessonById(lessonId)
            cancelSniffer.addLessonToCancel(lessonId)
            lesson!!.steps!!.forEach {
                cancelSniffer.addStepIdCancel(it)
            }
            lesson.steps!!.forEach {
                downloadManager.cancelStep(it)
            }
        }
    }

    override fun deleteWholeLesson(lessonId: Long) {
        threadPoolExecutor.execute {
            val lesson = databaseFacade.getLessonById(lessonId)
            cleanManager.removeLesson(lesson)
        }
    }

    @MainThread
    override fun onDownloadCompleted(downloadEntity: DownloadEntity, isSuccess: Boolean) {
        //todo remove download entity from the local list, after that update states of related lesson/section?, after that remove from database
        threadPoolExecutor.execute {
            val step = databaseFacade.getStepById(downloadEntity.stepId)!!
            val lessonId = databaseFacade.getLessonById(step.lesson)!!.id

            if (isSuccess) {
                storeStateManager.updateUnitLessonState(lessonId)
            } else {
                storeStateManager.updateUnitLessonAfterDeleting(lessonId)
            }
            databaseFacade.deleteDownloadEntityByDownloadId(downloadEntity.downloadId)
        }
    }

}
