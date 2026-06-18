package org.stepik.android.view.course_content.ui.adapter.delegates.unit

import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.collection.LongSparseArray
import androidx.core.view.isVisible
import dev.androidbroadcast.vbpd.viewBinding
import com.bumptech.glide.Glide
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewCourseContentUnitBinding
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.util.toFixed
import org.stepik.android.view.course_content.model.CourseContentItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import kotlin.math.abs

class CourseContentUnitDelegate(
    private val unitClickListener: CourseContentUnitClickListener,
    private val unitDownloadStatuses: LongSparseArray<DownloadProgress.Status>
) : AdapterDelegate<CourseContentItem, DelegateViewHolder<CourseContentItem>>() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder =
        ViewHolder(createView(parent, R.layout.view_course_content_unit))

    override fun isForViewType(position: Int, data: CourseContentItem): Boolean =
        data is CourseContentItem.UnitItem

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseContentItem>(root) {
        private val viewBinding: ViewCourseContentUnitBinding by viewBinding { ViewCourseContentUnitBinding.bind(root) }

        init {
            root.setOnClickListener {
                (itemData as? CourseContentItem.UnitItem)?.let(unitClickListener::onItemClicked)
            }

            viewBinding.unitDownloadStatus.setOnClickListener {
                val item = (itemData as? CourseContentItem.UnitItem) ?: return@setOnClickListener
                when (viewBinding.unitDownloadStatus.status) {
                    DownloadProgress.Status.NotCached ->
                        unitClickListener.onItemDownloadClicked(item)

                    is DownloadProgress.Status.InProgress ->
                        unitClickListener.onItemCancelClicked(item)

                    is DownloadProgress.Status.Cached ->
                        unitClickListener.onItemRemoveClicked(item)

                    else -> Unit
                }
            }
        }

        override fun onBind(data: CourseContentItem) {
            with(data as CourseContentItem.UnitItem) {
                viewBinding.unitTitle.text = context.resources.getString(R.string.course_content_unit_title,
                        section.position, unit.position, lesson.title)
                if (progress != null && progress.cost > 0) {
                    val score = progress
                        .score
                        ?.toFloatOrNull()
                        ?: 0f

                    viewBinding.unitTextProgress.text = context.resources.getString(R.string.course_content_text_progress_points,
                        score.toFixed(context.resources.getInteger(R.integer.score_decimal_count)), progress.cost)

                    viewBinding.unitProgress.progress = score / progress.cost.toFloat()
                    viewBinding.unitTextProgress.isVisible = true
                } else {
                    viewBinding.unitProgress.progress = 0f
                    viewBinding.unitTextProgress.isVisible = false
                }

                val timeToComplete = lesson.timeToComplete.takeIf { it > 60 } ?: lesson.steps.size * 60L

                if (timeToComplete > 0) {
                    viewBinding.unitTimeToComplete.isVisible = true

                    val timeToCompleteString = if (timeToComplete in 0 until 3600) {
                        val timeValue = timeToComplete / 60
                        context.resources.getQuantityString(R.plurals.min, timeValue.toInt(), timeValue)
                    } else {
                        context.resources.getString(R.string.course_content_time_to_complete_hours_unit, timeToComplete / 3600)
                    }

                    viewBinding.unitTimeToComplete.text = context.getString(R.string.course_content_time_to_complete, timeToCompleteString)
                } else {
                    viewBinding.unitTimeToComplete.isVisible = false
                }

                viewBinding.unitDownloadStatus.status = unitDownloadStatuses[data.unit.id] ?: DownloadProgress.Status.Pending

                Glide.with(viewBinding.unitIcon.context)
                    .asBitmap()
                    .load(lesson.coverUrl)
                    .placeholder(R.drawable.general_placeholder)
                    .centerCrop()
                    .into(viewBinding.unitIcon)

                viewBinding.unitViewCount.text = lesson.passedBy.toString()

                @DrawableRes
                val unitRatingDrawableRes =
                    if (lesson.voteDelta < 0) {
                        R.drawable.ic_course_content_dislike
                    } else {
                        R.drawable.ic_course_content_like
                    }

                viewBinding.unitRatingIcon.setImageResource(unitRatingDrawableRes)
                viewBinding.unitRating.text = abs(lesson.voteDelta).toString()

                viewBinding.unitDownloadStatus.isVisible = access == CourseContentItem.UnitItem.Access.FULL_ACCESS
                viewBinding.unitDemoAccess.isVisible = access == CourseContentItem.UnitItem.Access.DEMO
                itemView.isEnabled = access != CourseContentItem.UnitItem.Access.NO_ACCESS

                val alpha = if (access != CourseContentItem.UnitItem.Access.NO_ACCESS) 1f else 0.4f
                viewBinding.unitTitle.alpha = alpha
                viewBinding.unitRatingIcon.alpha = alpha
                viewBinding.unitRating.alpha = alpha
                viewBinding.unitViewCount.alpha = alpha
                viewBinding.unitViewCountIcon.alpha = alpha
                viewBinding.unitTimeToComplete.alpha = alpha
                viewBinding.unitTextProgress.alpha = alpha
            }
        }
    }
}
