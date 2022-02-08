package org.stepik.android.view.course_info.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewCourseInfoAboutBinding
import org.stepik.android.view.course_info.model.CourseInfoItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseInfoAboutAdapterDelegate : AdapterDelegate<CourseInfoItem, DelegateViewHolder<CourseInfoItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseInfoItem> =
        ViewHolder(createView(parent, R.layout.view_course_info_about))

    override fun isForViewType(position: Int, data: CourseInfoItem): Boolean =
        data is CourseInfoItem.AboutBlock

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseInfoItem>(root) {
        private val viewBinding: ViewCourseInfoAboutBinding by viewBinding { ViewCourseInfoAboutBinding.bind(root) }

        init {
            viewBinding.aboutText.textView.setLineSpacing(0f, 1.33f)
        }

        override fun onBind(data: CourseInfoItem) {
            data as CourseInfoItem.AboutBlock
            viewBinding.aboutText.setText(data.text)
        }
    }
}