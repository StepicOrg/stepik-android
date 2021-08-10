package org.stepik.android.view.user_reviews.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_user_review_reviewed.*
import kotlinx.android.synthetic.main.item_user_review_reviewed.userReviewCourseTitle
import kotlinx.android.synthetic.main.item_user_review_reviewed.userReviewIcon
import kotlinx.android.synthetic.main.item_user_review_reviewed.userReviewRating
import org.stepic.droid.R
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem
import org.stepik.android.model.Course
import org.stepik.android.view.base.ui.mapper.DateMapper
import org.stepik.android.view.glide.ui.extension.wrapWithGlide
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class UserReviewsReviewedAdapterDelegate(
    private val onCourseTitleClicked: (Course) -> Unit
) : AdapterDelegate<UserCourseReviewItem, DelegateViewHolder<UserCourseReviewItem>>() {
    override fun isForViewType(position: Int, data: UserCourseReviewItem): Boolean =
        data is UserCourseReviewItem.ReviewedItem

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<UserCourseReviewItem> =
        ViewHolder(createView(parent, R.layout.item_user_review_reviewed))

    private inner class ViewHolder(override val containerView: View) : DelegateViewHolder<UserCourseReviewItem>(containerView), LayoutContainer {
        init {
            userReviewIcon.setOnClickListener { (itemData as? UserCourseReviewItem.ReviewedItem)?.course?.let(onCourseTitleClicked) }
            userReviewCourseTitle.setOnClickListener { (itemData as? UserCourseReviewItem.ReviewedItem)?.course?.let(onCourseTitleClicked) }
        }
        private val reviewIconWrapper = userReviewIcon.wrapWithGlide()

        override fun onBind(data: UserCourseReviewItem) {
            data as UserCourseReviewItem.ReviewedItem
            userReviewCourseTitle.text = data.course.title
            userReviewText.text = data.courseReview.text
            // TODO Decide what to do with reviewIconWrapper
            Glide
                .with(context)
                .asBitmap()
                .load(data.course.cover)
                .placeholder(R.drawable.general_placeholder)
                .fitCenter()
                .into(userReviewIcon)
//            reviewIconWrapper.setImagePath(data.course.cover ?: "", AppCompatResources.getDrawable(context, R.drawable.general_placeholder))
            userReviewTime.text = DateMapper.mapToRelativeDate(context, DateTimeHelper.nowUtc(), data.courseReview.updateDate?.time ?: 0)
            userReviewRating.progress = data.courseReview.score
            userReviewRating.total = 5
        }
    }
}