package org.stepik.android.view.course_info.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewCourseInfoVideoBinding
import org.stepik.android.view.course_info.model.CourseInfoItem
import org.stepik.android.view.video_player.model.VideoPlayerMediaData
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseInfoVideoBlockDelegate(
    private val onVideoClicked: ((VideoPlayerMediaData) -> Unit)?
) : AdapterDelegate<CourseInfoItem, DelegateViewHolder<CourseInfoItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder =
        ViewHolder(createView(parent, R.layout.view_course_info_video))

    override fun isForViewType(position: Int, data: CourseInfoItem): Boolean =
        data is CourseInfoItem.VideoBlock

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseInfoItem>(root) {
        private val viewBinding: ViewCourseInfoVideoBinding by viewBinding { ViewCourseInfoVideoBinding.bind(root) }

        init {
            root.setOnClickListener {
                (itemData as? CourseInfoItem.VideoBlock)?.let { item ->
                    onVideoClicked?.invoke(item.videoMediaData)
                }
            }
        }

        override fun onBind(data: CourseInfoItem) {
            data as CourseInfoItem.VideoBlock
            Glide.with(viewBinding.videoThumbnail.context)
                .load(data.videoMediaData.externalVideo?.thumbnail ?: "")
                .placeholder(R.drawable.general_placeholder)
                .into(viewBinding.videoThumbnail)
        }
    }
}