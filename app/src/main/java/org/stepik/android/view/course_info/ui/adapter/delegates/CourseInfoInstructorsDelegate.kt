package org.stepik.android.view.course_info.ui.adapter.delegates

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_course_info_instructors_block.view.*
import org.stepic.droid.R
import org.stepik.android.view.course_info.ui.adapter.CourseInfoAdapter
import org.stepik.android.view.course_info.ui.adapter.CourseInfoInstructorsAdapter
import org.stepik.android.view.course_info.model.CourseInfoItem
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate
import org.stepik.android.model.user.User

class CourseInfoInstructorsDelegate(
        adapter: CourseInfoAdapter,
        private val onInstructorClicked: ((User) -> Unit)? = null
) : AdapterDelegate<CourseInfoItem, CourseInfoAdapter.ViewHolder>(adapter) {
    override fun onCreateViewHolder(parent: ViewGroup) =
            ViewHolder(createView(parent, R.layout.view_course_info_instructors_block))

    override fun isForViewType(position: Int): Boolean =
            getItemAtPosition(position) is CourseInfoItem.WithTitle.InstructorsBlock

    inner class ViewHolder(root: View) : CourseInfoAdapter.ViewHolderWithTitle(root) {
        private val adapter = CourseInfoInstructorsAdapter(onInstructorClicked)

        init {
            root.blockInstructors.let {
                it.adapter = adapter
                it.layoutManager = LinearLayoutManager(root.context)
                it.isNestedScrollingEnabled = false
            }
        }

        override fun onBind(data: CourseInfoItem) {
            super.onBind(data)
            data as CourseInfoItem.WithTitle.InstructorsBlock

            adapter.instructors = data.instructors
        }
    }
}