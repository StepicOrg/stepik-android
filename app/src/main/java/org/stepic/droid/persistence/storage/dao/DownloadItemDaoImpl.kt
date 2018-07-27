package org.stepic.droid.persistence.storage.dao

import android.app.DownloadManager
import io.reactivex.Observable
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.persistence.model.DownloadItem
import javax.inject.Inject

@AppSingleton
class DownloadItemDaoImpl
@Inject
constructor(
        private val downloadManager: DownloadManager
): DownloadItemDao {
    override fun get(vararg ids: Long): Observable<List<DownloadItem>> = if (ids.isEmpty()) {
        Observable.just(emptyList())
    } else {
        Observable.create<DownloadItem> { emitter ->
            downloadManager.query(DownloadManager.Query().setFilterById(*ids)).use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID))
                    val bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

                    emitter.onNext(DownloadItem(id, bytesDownloaded, bytesTotal))
                }
            }
            emitter.onComplete()
        }.toList().toObservable()
    }
}