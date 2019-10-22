package org.stepic.droid.persistence.storage.dao

import android.app.DownloadManager
import android.database.Cursor
import io.reactivex.Single
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.SystemDownloadRecord
import javax.inject.Inject

@PersistenceScope
class SystemDownloadsDaoImpl
@Inject
constructor(
    private val downloadManager: DownloadManager
): SystemDownloadsDao {
    override fun get(vararg ids: Long): Single<List<SystemDownloadRecord>> =
        if (ids.isEmpty()) {
            Single.just(emptyList())
        } else {
            Single
                .create<List<SystemDownloadRecord>> { emitter ->
                    val items = mutableListOf<SystemDownloadRecord>()
                    val cursor: Cursor? = downloadManager.query(DownloadManager.Query().setFilterById(*ids))
                    // cannot use Closable::use extension as Cursor doesn't implement Closable on all platform versions
                    // https://github.com/j256/ormlite-android/issues/20
                    try {
                        if (cursor != null) {
                            while (cursor.moveToNext() && !emitter.isDisposed) {
                                items += SystemDownloadRecord(
                                    id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID)),
                                    title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)),
                                    bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)),
                                    bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)),
                                    status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)),
                                    reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON)),
                                    localUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                                )
                            }
                        }
                        if (!emitter.isDisposed) {
                            emitter.onSuccess(items)
                        }
                    } catch (e: Exception) {
                        if (!emitter.isDisposed) {
                            emitter.onError(e)
                        }
                    } finally {
                        cursor?.close()
                    }
                }
    }
}