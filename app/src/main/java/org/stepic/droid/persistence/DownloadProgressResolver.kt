package org.stepic.droid.persistence

import android.app.DownloadManager
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

internal class DownloadProgressResolver(
        private val downloadManager: DownloadManager
) {
    companion object {
        const val PROGRESS_UPDATE_INTERVAL_MS = 500L
    }

    private val rwLock = ReentrantReadWriteLock()
    private val progresses = mutableMapOf<Long, Int>() // downloadId -> refCount

    private val intervalObservable = Observable.interval(PROGRESS_UPDATE_INTERVAL_MS, TimeUnit.MILLISECONDS).concatMap {
        progressListenerObservable.toList().toObservable()
    }.share() // request every PROGRESS_UPDATE_INTERVAL_MS milliseconds requested progresses only one time

    private val progressListenerObservable = Observable.create<Pair<Long, Float>> { emitter ->
        rwLock.read {
            downloadManager.query(DownloadManager.Query().setFilterById(*progresses.keys.toLongArray())).use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID))
                    val bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

                    emitter.onNext(id to (bytesDownloaded.toFloat() / bytesTotal))
                }
            }
        }
        emitter.onComplete()
    }

    fun getProgresses(vararg ids: Long): Observable<List<Pair<Long, Float>>> =
            Completable.create { // add current ids to refCount map
                rwLock.write {
                    ids.forEach { id -> progresses[id] = progresses[id]?.inc() ?: 1 }
                }
                it.onComplete()
            }.andThen(intervalObservable).doOnDispose { // remove refs for ids
                rwLock.write {
                    ids.forEach { id ->
                        if (progresses[id] == 1) {
                            progresses.remove(id)
                        } else {
                            progresses[id] = progresses[id]!! - 1
                        }
                    }
                }
            }.map {
                it.filter { (downloadId, _) -> downloadId in ids }
            } // filter only requested ids
}