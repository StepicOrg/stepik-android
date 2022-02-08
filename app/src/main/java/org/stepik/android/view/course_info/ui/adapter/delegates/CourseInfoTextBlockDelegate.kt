package org.stepik.android.view.course_info.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewCourseInfoTextBlockBinding
import org.stepik.android.view.course_info.model.CourseInfoItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseInfoTextBlockDelegate : AdapterDelegate<CourseInfoItem, DelegateViewHolder<CourseInfoItem>>() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder =
        ViewHolder(createView(parent, R.layout.view_course_info_text_block))

    override fun isForViewType(position: Int, data: CourseInfoItem): Boolean =
        data is CourseInfoItem.WithTitle.TextBlock

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseInfoItem>(root) {
        private val viewBinding: ViewCourseInfoTextBlockBinding by viewBinding { ViewCourseInfoTextBlockBinding.bind(root) }

        init {
            viewBinding.blockMessage.textView.setLineSpacing(0f, 1.33f)
        }

        override fun onBind(data: CourseInfoItem) {
            data as CourseInfoItem.WithTitle.TextBlock
            viewBinding.blockHeader.blockIcon.setImageResource(data.type.icon)
            viewBinding.blockHeader.blockTitle.setText(data.type.title)
            viewBinding.blockMessage.setText(data.text)
        }
    }
}