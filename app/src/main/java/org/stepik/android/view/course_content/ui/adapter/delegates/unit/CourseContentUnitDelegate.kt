package org.stepik.android.view.course_content.ui.adapter.delegates.unit

import android.graphics.BitmapFactory
import android.support.annotation.DrawableRes
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_course_content_unit.view.*
import org.stepic.droid.R
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget

class CourseContentUnitDelegate(
    adapter: DelegateAdapter<CourseContentItem, DelegateViewHolder<CourseContentItem>>,
    private val unitClickListener: CourseContentUnitClickListener
) : AdapterDelegate<CourseContentItem, DelegateViewHolder<CourseContentItem>>(adapter) {

    override fun onCreateViewHolder(parent: ViewGroup) =
            ViewHolder(createView(parent, R.layout.view_course_content_unit))

    override fun isForViewType(position: Int): Boolean =
            getItemAtPosition(position) is CourseContentItem.UnitItem

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseContentItem>(root) {
        private val unitIcon = root.unitIcon
        private val unitTitle = root.unitTitle
        private val unitTextProgress = root.unitTextProgress
        private val unitProgress = root.unitProgress

        private val unitViewCount = root.unitViewCount
        private val unitRating = root.unitRating
        private val unitRatingIcon = root.unitRatingIcon

        private val unitDownloadStatus = root.unitDownloadStatus

        private val unitIconTarget = RoundedBitmapImageViewTarget(
                context.resources.getDimension(R.dimen.course_image_radius), unitIcon)

        private val unitIconPlaceholder = with(context.resources) {
            val coursePlaceholderBitmap = BitmapFactory.decodeResource(this, R.drawable.general_placeholder)
            val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(this, coursePlaceholderBitmap)
            circularBitmapDrawable.cornerRadius = getDimension(R.dimen.course_image_radius)
            circularBitmapDrawable
        }

        init {
            root.setOnClickListener {
                (itemData as? CourseContentItem.UnitItem)?.let(unitClickListener::onItemClicked)
            }
        }

        override fun onBind(data: CourseContentItem) {
            with(data as CourseContentItem.UnitItem) {
                unitTitle.text = context.resources.getString(R.string.course_content_unit_title,
                        section.position, unit.position, lesson.title)

                unitTextProgress.text = context.resources.getString(R.string.course_content_text_progress,
                        progress.nStepsPassed, progress.nSteps)

                unitProgress.progress = progress.nStepsPassed.toFloat() / progress.nSteps

                unitDownloadStatus.status = downloadStatus

                Glide.with(unitIcon.context)
                        .load(lesson.coverUrl)
                        .asBitmap()
                        .placeholder(unitIconPlaceholder)
                        .centerCrop()
                        .into(unitIconTarget)

                unitViewCount.text = lesson.passedBy.toString()

                @DrawableRes
                val unitRatingDrawableRes = if (lesson.voteDelta < 0) {
                    R.drawable.ic_course_content_dislike
                } else {
                    R.drawable.ic_course_content_like
                }

                unitRatingIcon.setImageResource(unitRatingDrawableRes)
                unitRating.text = Math.abs(lesson.voteDelta).toString()
            }
        }
    }
}