package org.stepic.droid.store

import org.stepic.droid.store.operations.DatabaseFacade
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Singleton

@Singleton
class LessonDownloaderImpl(private val databaseFacade: DatabaseFacade,
                           private val downloadManager: IDownloadManager,
                           private val threadPoolExecutor: ThreadPoolExecutor,
                           private val cleanManager: CleanManager,
                           private val cancelSniffer: CancelSniffer) : LessonDownloader {

    override fun downloadLesson(lessonId: Long) {
        threadPoolExecutor.execute {
            val lesson = databaseFacade.getLessonById(lessonId) ?: throw Exception("lesson was null, when downloadLesson of LessonDownloader is executed") // FIXME: IT CAN BE BY THE NORMAL EXECUTION, CHANGE IT, THIS LESSON MAY BE NEEDED, WHEN SECTION IS LOADED
            lesson.steps?.forEach {
                cancelSniffer.removeStepIdCancel(it)
            }
            downloadManager.addLesson(lesson)
        }
    }

    override fun cancelLessonLoading(lessonId: Long) {
        threadPoolExecutor.execute {
            val lesson = databaseFacade.getLessonById(lessonId)
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
}
