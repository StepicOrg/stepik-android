package org.stepik.android.view.course_reviews.ui.adapter

import android.support.v7.util.DiffUtil
import org.stepik.android.domain.course_reviews.model.CourseReviewItem

class CourseReviewsDiffCallback(
    private val oldList: List<CourseReviewItem>,
    private val newList: List<CourseReviewItem>
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
            oldItem is CourseReviewItem.Data
                    && newItem is CourseReviewItem.Data
                    && oldItem.courseReview.id == newItem.courseReview.id
                -> true

            oldItem is CourseReviewItem.Placeholder
                    && newItem is CourseReviewItem.Placeholder
                -> true

            else
                -> false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem == newItem) return true

        return when {
            oldItem is CourseReviewItem.Data
                    && newItem is CourseReviewItem.Data
                    && oldItem.user == newItem.user
                    && oldItem.courseReview.text == newItem.courseReview.text
                    && oldItem.courseReview.score == newItem.courseReview.score
                -> true

            oldItem is CourseReviewItem.Placeholder
                    && newItem is CourseReviewItem.Placeholder
                -> true

            else
                -> false
        }
    }
}