package org.stepik.android.view.course_info.ui.adapter.delegates

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_course_info_instructors_block.view.*
import org.stepic.droid.R
import org.stepik.android.model.user.User
import org.stepik.android.view.course_info.model.CourseInfoItem
import org.stepik.android.view.course_info.ui.adapter.CourseInfoAdapter
import org.stepik.android.view.course_info.ui.adapter.CourseInfoInstructorsAdapter
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate

class CourseInfoInstructorsDelegate(
    private val onInstructorClicked: ((User) -> Unit)? = null
) : AdapterDelegate<CourseInfoItem, CourseInfoAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup): CourseInfoAdapter.ViewHolder =
        ViewHolder(createView(parent, R.layout.view_course_info_instructors_block))

    override fun isForViewType(position: Int, data: CourseInfoItem): Boolean =
        data is CourseInfoItem.WithTitle.InstructorsBlock

    private inner class ViewHolder(root: View) : CourseInfoAdapter.ViewHolderWithTitle(root) {
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