package org.stepik.android.view.course_info.ui.adapter.delegates

import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_course_info_instructors_block.view.*
import org.stepic.droid.R
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import org.stepik.android.model.user.User
import org.stepik.android.view.course_info.model.CourseInfoItem
import org.stepik.android.view.course_info.ui.adapter.CourseInfoAdapter
import org.stepik.android.view.course_info.ui.adapter.delegates.instructors.CourseInfoInstructorDataAdapterDelegate
import org.stepik.android.view.course_info.ui.adapter.delegates.instructors.CourseInfoInstructorPlaceholderAdapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CourseInfoInstructorsDelegate(
    private val onInstructorClicked: (User) -> Unit
) : AdapterDelegate<CourseInfoItem, CourseInfoAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder =
        ViewHolder(createView(parent, R.layout.view_course_info_instructors_block))

    override fun isForViewType(position: Int, data: CourseInfoItem): Boolean =
        data is CourseInfoItem.WithTitle.InstructorsBlock

    inner class ViewHolder(root: View) : CourseInfoAdapter.ViewHolderWithTitle(root) {
        private val adapter = DefaultDelegateAdapter<User?>()

        init {
            adapter += CourseInfoInstructorDataAdapterDelegate(onInstructorClicked)
            adapter += CourseInfoInstructorPlaceholderAdapterDelegate()

            root.blockInstructors.let {
                it.adapter = adapter
                it.layoutManager = LinearLayoutManager(root.context)
                it.isNestedScrollingEnabled = false
            }
        }

        override fun onBind(data: CourseInfoItem) {
            super.onBind(data)
            data as CourseInfoItem.WithTitle.InstructorsBlock

            adapter.items = data.instructors
        }
    }
}