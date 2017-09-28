package org.stepic.droid.storage

import android.app.DownloadManager

const val DOWNLOAD_STATUS_UNDEFINED = -1

fun DownloadManager.getDownloadStatus(referenceId: Long) : Int {
    val query = DownloadManager.Query()
    query.setFilterById(referenceId)
    return query(query).use {
        return@use if (it.moveToFirst() && it.count > 0) {
            it.getInt(it.getColumnIndex(DownloadManager.COLUMN_STATUS))
        } else {
            DOWNLOAD_STATUS_UNDEFINED
        }
    }
}