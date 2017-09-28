package org.stepic.droid.storage

import android.app.DownloadManager

const val DOWNLOAD_STATUS_UNDEFINED = -1

fun DownloadManager.getDownloadStatus(referenceId: Long) : Int {
    val query = DownloadManager.Query()
    query.setFilterById(referenceId)
    val cursor = query(query)
    val status = if (cursor.moveToFirst() && cursor.count > 0) {
        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
    } else {
        DOWNLOAD_STATUS_UNDEFINED
    }
    cursor.close()
    return status
}