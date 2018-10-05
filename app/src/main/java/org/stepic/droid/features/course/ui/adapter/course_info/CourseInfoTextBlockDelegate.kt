package org.stepic.droid.features.course.ui.adapter.course_info

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_course_info_text_block.view.*
import org.stepic.droid.R
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoBlock
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoTextBlock
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoType
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate

class CourseInfoTextBlockDelegate(
        adapter: CourseInfoBlockAdapter
) : AdapterDelegate<CourseInfoBlock, CourseInfoBlockAdapter.CourseInfoViewHolder<CourseInfoBlock>>(adapter) {
    override fun onCreateViewHolder(parent: ViewGroup) =
            ViewHolder(createView(parent, R.layout.view_course_info_text_block))

    override fun isForViewType(position: Int): Boolean =
            getItemAtPosition(position).type != CourseInfoType.INSTRUCTORS

    class ViewHolder(root: View) : CourseInfoBlockAdapter.CourseInfoViewHolder<CourseInfoBlock>(root) {
        private val blockMessage = root.blockMessage

        override fun onBind(data: CourseInfoBlock) {
            super.onBind(data)
            data as CourseInfoTextBlock
            blockMessage.text = data.message
        }
    }
}