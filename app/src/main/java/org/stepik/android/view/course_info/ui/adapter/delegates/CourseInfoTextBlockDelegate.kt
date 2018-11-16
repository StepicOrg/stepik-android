package org.stepik.android.view.course_info.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_course_info_text_block.view.*
import org.stepic.droid.R
import org.stepik.android.view.course_info.ui.adapter.CourseInfoAdapter
import org.stepik.android.view.course_info.model.CourseInfoItem
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate

class CourseInfoTextBlockDelegate(
        adapter: CourseInfoAdapter
) : AdapterDelegate<CourseInfoItem, CourseInfoAdapter.ViewHolder>(adapter) {
    override fun onCreateViewHolder(parent: ViewGroup) =
        ViewHolder(
            createView(parent, R.layout.view_course_info_text_block)
        )

    override fun isForViewType(position: Int): Boolean =
            getItemAtPosition(position) is CourseInfoItem.WithTitle.TextBlock

    class ViewHolder(root: View) : CourseInfoAdapter.ViewHolderWithTitle(root) {
        private val blockMessage = root.blockMessage

        override fun onBind(data: CourseInfoItem) {
            super.onBind(data)
            data as CourseInfoItem.WithTitle.TextBlock
            blockMessage.text = data.text
        }
    }
}