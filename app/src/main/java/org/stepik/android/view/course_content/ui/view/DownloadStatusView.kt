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
import org.stepik.android.view.ui.delegate.ViewStateDelegate

class DownloadStatusView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    var status: DownloadProgress.Status = DownloadProgress.Status.Pending
        set(value) {
            field = value
            viewStateDelegate.switchState(status)

            isEnabled = status !is DownloadProgress.Status.Pending
            when (value) {
                is DownloadProgress.Status.Cached -> {
                    // show mb
                }

                is DownloadProgress.Status.InProgress ->
                    statusProgress.progress = (value.progress * statusProgress.max).toInt()
            }
        }

    private val statusProgress: ProgressBar

    private val viewStateDelegate = ViewStateDelegate<DownloadProgress.Status>()

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_download_status, this, true)
        viewStateDelegate.addState<DownloadProgress.Status.NotCached>(view.statusNotCached)
        viewStateDelegate.addState<DownloadProgress.Status.Cached>(view.statusCached)
        viewStateDelegate.addState<DownloadProgress.Status.Pending>(view.statusPending)
        viewStateDelegate.addState<DownloadProgress.Status.InProgress>(view.statusInProgress)

        statusProgress = view.statusProgress
    }
}