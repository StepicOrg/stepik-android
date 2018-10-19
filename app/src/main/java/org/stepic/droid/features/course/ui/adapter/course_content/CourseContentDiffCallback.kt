package org.stepic.droid.features.course.ui.adapter.course_content

import android.support.v7.util.DiffUtil
import org.stepic.droid.features.course.ui.model.course_content.CourseContentItem

class CourseContentDiffCallback(
        private val oldList: List<CourseContentItem>,
        private val newList: List<CourseContentItem>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int =
            oldList.size

    override fun getNewListSize(): Int =
            newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem == newItem) return true

        return when {
            oldItem is CourseContentItem.SectionItem &&
                    newItem is CourseContentItem.SectionItem ->
                oldItem.section.id == newItem.section.id

            oldItem is CourseContentItem.UnitItem &&
                    newItem is CourseContentItem.UnitItem ->
                oldItem.lesson.id == newItem.lesson.id

            else ->
                false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem == newItem) return true

        return when {
            oldItem is CourseContentItem.SectionItem &&
                    newItem is CourseContentItem.SectionItem ->
                oldItem.section == newItem.section &&
                        oldItem.progress == newItem.progress &&
                        oldItem.downloadProgress == newItem.downloadProgress

            oldItem is CourseContentItem.UnitItem &&
                    newItem is CourseContentItem.UnitItem ->
                oldItem.lesson.id == newItem.lesson.id &&
                        oldItem.unit.id == newItem.unit.id &&
                        oldItem.section == newItem.section &&
                        oldItem.progress == newItem.progress &&
                        oldItem.downloadProgress == newItem.downloadProgress

            else ->
                false
        }
    }
}