package org.stepic.droid.persistence.storage.dao

import android.app.DownloadManager
import io.reactivex.Observable
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.persistence.model.SystemDownload
import javax.inject.Inject

@AppSingleton
class SystemDownloadsDaoImpl
@Inject
constructor(
        private val downloadManager: DownloadManager
): SystemDownloadsDao {
    override fun get(vararg ids: Long): Observable<List<SystemDownload>> = if (ids.isEmpty()) {
        Observable.just(emptyList())
    } else {
        Observable.create<SystemDownload> { emitter ->
            downloadManager.query(DownloadManager.Query().setFilterById(*ids)).use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID))
                    val bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

                    emitter.onNext(SystemDownload(id, bytesDownloaded, bytesTotal))
                }
            }
            emitter.onComplete()
        }.toList().toObservable()
    }
}