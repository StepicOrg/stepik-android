package org.stepik.android.view.download.ui.adapter

import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import org.stepic.droid.R
import org.stepic.droid.databinding.DownloadedCourseItemBinding
import org.stepic.droid.persistence.model.DownloadItem
import org.stepic.droid.persistence.model.DownloadProgress
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class DownloadedCoursesAdapterDelegate(
    private val onItemClick: (DownloadItem) -> Unit,
    private val onItemRemoveClick: (DownloadItem) -> Unit
) : AdapterDelegate<DownloadItem, DelegateViewHolder<DownloadItem>>() {
    companion object {
        private const val SMALLEST_FORMAT_UNIT = 1024 * 1024L // 1 mb
    }
    override fun isForViewType(position: Int, data: DownloadItem): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<DownloadItem> =
        ViewHolder(createView(parent, R.layout.downloaded_course_item))

    private inner class ViewHolder(root: View) : DelegateViewHolder<DownloadItem>(root) {
        private val viewBinding: DownloadedCourseItemBinding by viewBinding { DownloadedCourseItemBinding.bind(root) }

        init {
            root.setOnClickListener { itemData?.let(onItemClick) }
            viewBinding.downloadedCourseStatus.setOnClickListener {
                if (viewBinding.downloadedCourseStatus.status is DownloadProgress.Status.Cached) {
                    itemData?.let(onItemRemoveClick)
                }
            }
        }

        override fun onBind(data: DownloadItem) {
            viewBinding.downloadedCourseTitle.text = data.course.title
            viewBinding.downloadedCourseStatus.status = data.status

            Glide.with(context)
                .asBitmap()
                .load(data.course.cover)
                .placeholder(R.drawable.general_placeholder)
                .fitCenter()
                .into(viewBinding.downloadedCourseImage)
        }
    }
}