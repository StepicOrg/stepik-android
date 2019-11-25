package org.stepik.android.view.download.ui.adapter

import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import kotlinx.android.synthetic.main.downloaded_course_item.view.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.DownloadItem
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.util.TextUtil
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

        private val downloadedCourseTitle = root.downloadedCourseName
        private val downloadedCourseImage = root.downloadedCourseImage
        private val downloadedCourseStatus = root.downloadedCourseStatus

        private val coursePlaceholderBitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.general_placeholder
        )

        private val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(
            context.resources,
            coursePlaceholderBitmap
        )

        private val imageViewTarget: BitmapImageViewTarget =  RoundedBitmapImageViewTarget(itemView.resources.getDimension(R.dimen.course_image_radius), downloadedCourseImage)

        init {
            root.setOnClickListener { itemData?.let(onItemClick) }
            downloadedCourseStatus.setOnClickListener {
                if (downloadedCourseStatus.status is DownloadProgress.Status.Cached) {
                    itemData?.let(onItemRemoveClick)
                }
            }
            circularBitmapDrawable.cornerRadius = context.resources.getDimension(R.dimen.course_image_radius)
        }

        override fun onBind(data: DownloadItem) {
            downloadedCourseTitle.text = data.course.title
            downloadedCourseStatus.status = data.status

            Glide.with(context)
                .asBitmap()
                .load(data.course.cover)
                .placeholder(circularBitmapDrawable)
                .fitCenter()
                .into(imageViewTarget)
        }
    }
}