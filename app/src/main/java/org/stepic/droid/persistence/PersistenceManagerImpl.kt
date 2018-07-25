package org.stepic.droid.persistence

import android.app.DownloadManager
import android.content.Context
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.ProgressItem
import org.stepic.droid.persistence.storage.PersistentItemDao
import javax.inject.Inject

@AppSingleton
class PersistenceManagerImpl
@Inject
constructor(
        context: Context,
        private val persistentItemDao: PersistentItemDao
): PersistenceManager {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val downloadProgressResolver = DownloadProgressResolver(downloadManager)

    override fun getSectionsProgress(vararg sectionsIds: Long): Observable<ProgressItem> =
            sectionsIds.toObservable().flatMap(::getSectionProgress)

    private fun getSectionProgress(sectionId: Long) =
            getItemProgress(sectionId, persistentItemDao.getItemsBySectionId(sectionId))

    private fun getItemProgress(itemId: Long, persistentObservable: Observable<List<PersistentItem>>) = persistentObservable.flatMap { items ->
        val ids = items.filter {
            it.status == PersistentItem.Status.PENDING
                    || it.status == PersistentItem.Status.FILE_TRANSFER
                    || it.status == PersistentItem.Status.COMPLETED
        }.map { it.downloadId }.toLongArray()

        downloadProgressResolver.getProgresses(*ids)
    }.map {
        var downloaded = 0
        var total = 0

        it.forEach { item ->
            if (item.bytesTotal > 0) {
                downloaded += item.bytesDownloaded
                total += item.bytesTotal
            }
        }

        val progress: Float
        val state: ProgressItem.State
        when {
            total == 0 -> {
                progress = 0f
                state = if (it.isEmpty()) ProgressItem.State.NOT_CACHED else ProgressItem.State.PENDING
            }

            downloaded == total -> {
                progress = 0f
                state = ProgressItem.State.CACHED
            }

            else -> {
                progress = downloaded.toFloat() / total
                state = ProgressItem.State.IN_PROGRESS
            }
        }
        ProgressItem(itemId, state, progress)
    }.distinctUntilChanged() // in order to decrease number of events

    override fun getLessonsProgress(vararg lessonsIds: Long): Observable<ProgressItem> =
            lessonsIds.toObservable().flatMap(::getLessonProgress)

    private fun getLessonProgress(lessonId: Long) =
            getItemProgress(lessonId, persistentItemDao.getItemsByLessonId(lessonId))

    override fun cacheSection(sectionId: Long): Observable<ProgressItem> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cacheLesson(lessonId: Long): Observable<ProgressItem> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDownloadCompleted(downloadId: Long, localPath: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun resolvePath(originalPath: String): Maybe<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}