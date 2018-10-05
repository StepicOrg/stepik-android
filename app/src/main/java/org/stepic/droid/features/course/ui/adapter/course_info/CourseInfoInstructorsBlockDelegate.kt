package org.stepic.droid.features.course.ui.adapter.course_info

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_course_info_instructors_block.view.*
import org.stepic.droid.R
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoBlock
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoInstructorsBlock
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoType
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate

class CourseInfoInstructorsBlockDelegate(
        adapter: CourseInfoBlockAdapter
) : AdapterDelegate<CourseInfoBlock, CourseInfoBlockAdapter.CourseInfoViewHolder<CourseInfoBlock>>(adapter) {
    override fun onCreateViewHolder(parent: ViewGroup) =
            ViewHolder(createView(parent, R.layout.view_course_info_instructors_block))

    override fun isForViewType(position: Int): Boolean =
            getItemAtPosition(position).type == CourseInfoType.INSTRUCTORS

    class ViewHolder(root: View) : CourseInfoBlockAdapter.CourseInfoViewHolder<CourseInfoBlock>(root) {
        private val adapter = CourseInfoInstructorsAdapter()

        init {
            root.blockInstructors.let {
                it.adapter = adapter
                it.layoutManager = LinearLayoutManager(root.context)
            }
        }

        override fun onBind(data: CourseInfoBlock) {
            super.onBind(data)
            data as CourseInfoInstructorsBlock

            adapter.instructors = data.instructors
        }
    }
}