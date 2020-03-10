package org.stepik.android.view.profile_courses.ui.adapter.diffutil

import androidx.recyclerview.widget.DiffUtil
import org.stepik.android.domain.course_list.model.CourseListItem

class CourseDiffUtilCallback(
    private val oldList: List<CourseListItem.Data>,
    private val newList: List<CourseListItem.Data>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int =
        oldList.size

    override fun getNewListSize(): Int =
        newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition]

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return oldItem == newItem &&
                oldItem.course.title == newItem.course.title &&
                oldItem.course.enrollment == newItem.course.enrollment
    }
}