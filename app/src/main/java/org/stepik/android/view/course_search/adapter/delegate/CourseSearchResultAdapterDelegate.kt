package org.stepik.android.view.course_search.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_course_search_result.*
import org.stepic.droid.R
import org.stepic.droid.util.toFixed
import org.stepik.android.domain.course_search.model.CourseSearchResultListItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import kotlin.math.abs

class CourseSearchResultAdapterDelegate : AdapterDelegate<CourseSearchResultListItem, DelegateViewHolder<CourseSearchResultListItem>>() {
    override fun isForViewType(position: Int, data: CourseSearchResultListItem): Boolean =
        data is CourseSearchResultListItem.Data

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseSearchResultListItem> =
        ViewHolder(createView(parent, R.layout.item_course_search_result))

    private inner class ViewHolder(
        override val containerView: View
    ) : DelegateViewHolder<CourseSearchResultListItem>(containerView), LayoutContainer {
        override fun onBind(data: CourseSearchResultListItem) {
            data as CourseSearchResultListItem.Data
            courseSearchTitle.text = data.courseSearchResult.searchResult.lessonTitle
            Glide.with(courseSearchIcon)
                .asBitmap()
                .load(data.courseSearchResult.searchResult.lessonCoverUrl)
                .placeholder(R.drawable.general_placeholder)
                .centerCrop()
                .into(courseSearchIcon)

            val progress = data.courseSearchResult.progress
            val lesson = data.courseSearchResult.lesson
            val isProgressAvailable = progress != null

            courseSearchProgressPlaceholder.isVisible = data.courseSearchResult.progress == null
            courseSearchTextProgress.isVisible = isProgressAvailable
            courseSearchTimeToComplete.isVisible = isProgressAvailable
            courseSearchViewCountIcon.isVisible = isProgressAvailable
            unitViewCount.isVisible = isProgressAvailable
            courseSearchRatingIcon.isVisible = isProgressAvailable
            unitRating.isVisible = isProgressAvailable

            if (progress != null && progress.cost > 0) {
                val score = progress
                    .score
                    ?.toFloatOrNull()
                    ?: 0f

                courseSearchTextProgress.text = context.resources.getString(R.string.course_content_text_progress_points,
                    score.toFixed(context.resources.getInteger(R.integer.score_decimal_count)), progress.cost)
            } else {
                courseSearchTextProgress.isVisible = false
            }

            if (lesson != null) {
                val timeToComplete = lesson.timeToComplete.takeIf { it > 60 } ?: lesson.steps.size * 60L

                if (timeToComplete > 0) {
                    val timeToCompleteString = if (timeToComplete in 0 until 3600) {
                        val timeValue = timeToComplete / 60
                        context.resources.getQuantityString(R.plurals.min, timeValue.toInt(), timeValue)
                    } else {
                        context.resources.getString(R.string.course_content_time_to_complete_hours_unit, timeToComplete / 3600)
                    }
                    courseSearchTimeToComplete.text = context.getString(R.string.course_content_time_to_complete, timeToCompleteString)
                }

                unitViewCount.text = lesson.passedBy.toString()

                @DrawableRes
                val unitRatingDrawableRes =
                    if (lesson.voteDelta < 0) {
                        R.drawable.ic_course_content_dislike
                    } else {
                        R.drawable.ic_course_content_like
                    }

                courseSearchRatingIcon.setImageResource(unitRatingDrawableRes)
                unitRating.text = abs(lesson.voteDelta).toString()
            }
        }
    }
}