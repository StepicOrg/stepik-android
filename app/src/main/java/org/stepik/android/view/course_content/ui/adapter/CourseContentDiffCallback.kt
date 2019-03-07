package org.stepik.android.view.course_content.ui.adapter

import android.support.v7.util.DiffUtil
import org.stepik.android.view.course_content.model.CourseContentItem

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

            oldItem is CourseContentItem.UnitItemPlaceholder &&
                    newItem is CourseContentItem.UnitItemPlaceholder ->
                oldItem.unitId == newItem.unitId

            else ->
                false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem == newItem) return true

        return when {
            oldItem is CourseContentItem.UnitItemPlaceholder &&
                    newItem is CourseContentItem.UnitItemPlaceholder ->
                oldItem.unitId == newItem.unitId

            else ->
                false
        }
    }
}