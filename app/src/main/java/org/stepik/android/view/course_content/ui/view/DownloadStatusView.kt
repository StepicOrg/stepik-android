package org.stepik.android.view.course_content.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.view_download_status.view.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.ui.util.hideAllChildren

class DownloadStatusView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    var status: DownloadProgress.Status = DownloadProgress.Status.Pending
        set(value) {
            field = value
            invalidateStatus()
        }

    private val statusNotCached: View
    private val statusCached: View
    private val statusPending: View
    private val statusInProgress: View

    private val statusProgress: ProgressBar

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_download_status, this, true)
        statusNotCached  = view.statusNotCached
        statusCached     = view.statusCached
        statusPending    = view.statusPending
        statusInProgress = view.statusInProgress

        statusProgress   = view.statusProgress
    }

    private fun invalidateStatus() {
        hideAllChildren()
        isEnabled = true
        val status = this.status
        when (status) {
            DownloadProgress.Status.NotCached ->
                statusNotCached

            DownloadProgress.Status.Cached ->
                statusCached

            DownloadProgress.Status.Pending -> {
                isEnabled = false
                statusPending
            }

            is DownloadProgress.Status.InProgress -> {
                statusProgress.progress = (status.progress * statusProgress.max).toInt()
                statusInProgress
            }
        }.visibility = View.VISIBLE
    }
}