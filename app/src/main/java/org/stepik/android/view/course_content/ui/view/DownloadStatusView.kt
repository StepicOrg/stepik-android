package org.stepik.android.view.course_content.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewDownloadStatusBinding
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.util.TextUtil
import org.stepik.android.view.ui.delegate.ViewStateDelegate

class DownloadStatusView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private const val SMALLEST_FORMAT_UNIT = 1024 * 1024L // 1 mb
    }

    var status: DownloadProgress.Status = DownloadProgress.Status.Pending
        set(value) {
            field = value
            viewStateDelegate.switchState(status)

            isEnabled = status !is DownloadProgress.Status.Pending
            when (value) {
                is DownloadProgress.Status.Cached ->
                    statusCached.text = TextUtil.formatBytes(value.bytesTotal, SMALLEST_FORMAT_UNIT)

                is DownloadProgress.Status.InProgress ->
                    statusProgress.progress = (value.progress * statusProgress.max).toInt()

                else -> Unit
            }
        }

    private val statusProgress: ProgressBar
    private val statusCached: TextView

    private val viewStateDelegate = ViewStateDelegate<DownloadProgress.Status>()

    init {
        val binding = ViewDownloadStatusBinding.inflate(LayoutInflater.from(context), this)
        statusCached = binding.statusCached

        viewStateDelegate.addState<DownloadProgress.Status.NotCached>(binding.statusNotCached)
        viewStateDelegate.addState<DownloadProgress.Status.Cached>(statusCached)
        viewStateDelegate.addState<DownloadProgress.Status.Pending>(binding.statusPending)
        viewStateDelegate.addState<DownloadProgress.Status.InProgress>(binding.statusInProgress)

        statusProgress = binding.statusProgress
    }
}
