package org.stepic.droid.persistence.service

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DownloadCompleteReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                .takeIf { it != -1L } ?: return

        val serviceIntent = Intent()
                .putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId)

        DownloadCompleteService.enqueueWork(context, serviceIntent)
    }
}