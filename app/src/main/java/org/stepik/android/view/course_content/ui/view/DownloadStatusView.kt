package org.stepik.android.view.course_content.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.android.synthetic.main.view_download_status.view.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepic.droid.util.TextUtil
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
                is DownloadProgress.Status.Cached ->
                    statusCached.text = TextUtil.formatBytes(value.bytesTotal)

                is DownloadProgress.Status.InProgress ->
                    statusProgress.progress = (value.progress * statusProgress.max).toInt()
            }
        }

    private val statusProgress: ProgressBar
    private val statusCached: TextView

    private val viewStateDelegate = ViewStateDelegate<DownloadProgress.Status>()

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_download_status, this, true)
        statusCached = view.statusCached

        viewStateDelegate.addState<DownloadProgress.Status.NotCached>(view.statusNotCached)
        viewStateDelegate.addState<DownloadProgress.Status.Cached>(statusCached)
        viewStateDelegate.addState<DownloadProgress.Status.Pending>(view.statusPending)
        viewStateDelegate.addState<DownloadProgress.Status.InProgress>(view.statusInProgress)

        statusCached.setCompoundDrawables(top = R.drawable.ic_download_remove)

        statusProgress = view.statusProgress
    }
}