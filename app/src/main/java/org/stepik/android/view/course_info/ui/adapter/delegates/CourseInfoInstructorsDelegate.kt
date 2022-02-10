package org.stepik.android.view.course_info.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewCourseInfoInstructorsBlockBinding
import org.stepik.android.model.user.User
import org.stepik.android.view.course_info.model.CourseInfoItem
import org.stepik.android.view.course_info.ui.adapter.delegates.instructors.CourseInfoInstructorDataAdapterDelegate
import org.stepik.android.view.course_info.ui.adapter.delegates.instructors.CourseInfoInstructorPlaceholderAdapterDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CourseInfoInstructorsDelegate(
    private val onInstructorClicked: (User) -> Unit
) : AdapterDelegate<CourseInfoItem, DelegateViewHolder<CourseInfoItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder =
        ViewHolder(createView(parent, R.layout.view_course_info_instructors_block))

    override fun isForViewType(position: Int, data: CourseInfoItem): Boolean =
        data is CourseInfoItem.WithTitle.InstructorsBlock

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseInfoItem>(root) {
        private val viewBinding: ViewCourseInfoInstructorsBlockBinding by viewBinding { ViewCourseInfoInstructorsBlockBinding.bind(root) }
        private val adapter = DefaultDelegateAdapter<User?>()

        init {
            adapter += CourseInfoInstructorDataAdapterDelegate(onInstructorClicked)
            adapter += CourseInfoInstructorPlaceholderAdapterDelegate()

            viewBinding.blockInstructors.let {
                it.adapter = adapter
                it.layoutManager = LinearLayoutManager(root.context)
                it.isNestedScrollingEnabled = false
            }
        }

        override fun onBind(data: CourseInfoItem) {
            data as CourseInfoItem.WithTitle.InstructorsBlock
            viewBinding.blockHeader.blockIcon.setImageResource(data.type.icon)
            viewBinding.blockHeader.blockTitle.setText(data.type.title)

            adapter.items = data.instructors
        }
    }
}