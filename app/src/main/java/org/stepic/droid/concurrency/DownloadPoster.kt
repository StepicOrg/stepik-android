package org.stepic.droid.concurrency

import com.squareup.otto.Bus

import org.stepic.droid.base.MainApplication
import org.stepic.droid.events.video.DownloadReportEvent
import org.stepic.droid.model.DownloadingVideoItem

import javax.inject.Inject

class DownloadPoster(private val downloadingVideoItem: DownloadingVideoItem) : Function0<Unit> {

    @Inject
    lateinit var bus: Bus

    init {
        MainApplication.component().inject(this)
    }

    override fun invoke(): Unit {
        bus.post(DownloadReportEvent(downloadingVideoItem))
    }
}
