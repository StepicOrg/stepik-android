package org.stepik.android.view.course_reviews.ui.adapter.delegates

import android.graphics.BitmapFactory
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_course_reviews_item.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.course_reviews.model.CourseReviewItem
import java.util.*

class CourseReviewDataDelegate(
    adapter: DelegateAdapter<CourseReviewItem, DelegateViewHolder<CourseReviewItem>>
) : AdapterDelegate<CourseReviewItem, DelegateViewHolder<CourseReviewItem>>(adapter) {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseReviewItem> =
        ViewHolder(createView(parent, R.layout.view_course_reviews_item))

    override fun isForViewType(position: Int): Boolean =
        getItemAtPosition(position) is CourseReviewItem.Data

    class ViewHolder(root: View) : DelegateViewHolder<CourseReviewItem>(root) {
        private val reviewIcon = root.reviewIcon
        private val reviewDate = root.reviewDate
        private val reviewName = root.reviewName
        private val reviewRating = root.reviewRating
        private val reviewText = root.reviewText

        private val reviewIconTarget = RoundedBitmapImageViewTarget(
            context.resources.getDimension(R.dimen.course_image_radius), reviewIcon)

        private val reviewIconPlaceholder = with(context.resources) {
            val coursePlaceholderBitmap = BitmapFactory.decodeResource(this, R.drawable.general_placeholder)
            val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(this, coursePlaceholderBitmap)
            circularBitmapDrawable.cornerRadius = getDimension(R.dimen.course_image_radius)
            circularBitmapDrawable
        }

        override fun onBind(data: CourseReviewItem) {
            data as CourseReviewItem.Data

            Glide.with(context)
                .load(data.user.avatar ?: "")
                .asBitmap()
                .placeholder(reviewIconPlaceholder)
                .centerCrop()
                .into(reviewIconTarget)

            reviewName.text = data.user.fullName

            reviewDate.text = DateTimeHelper
                .getPrintableDate(data.courseReview.updateDate ?: Date(), DateTimeHelper.DISPLAY_DATETIME_PATTERN, TimeZone.getDefault())

            reviewRating.progress = data.courseReview.score
            reviewRating.total = 5

            reviewText.text = data.courseReview.text
        }
    }
}