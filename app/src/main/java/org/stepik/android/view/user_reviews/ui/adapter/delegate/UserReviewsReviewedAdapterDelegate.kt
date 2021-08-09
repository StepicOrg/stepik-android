package org.stepik.android.view.user_reviews.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_user_review_reviewed.*
import kotlinx.android.synthetic.main.item_user_review_reviewed.userReviewCourseTitle
import kotlinx.android.synthetic.main.item_user_review_reviewed.userReviewIcon
import org.stepic.droid.R
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem
import org.stepik.android.view.base.ui.mapper.DateMapper
import org.stepik.android.view.glide.ui.extension.wrapWithGlide
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class UserReviewsReviewedAdapterDelegate : AdapterDelegate<UserCourseReviewItem, DelegateViewHolder<UserCourseReviewItem>>() {
    override fun isForViewType(position: Int, data: UserCourseReviewItem): Boolean =
        data is UserCourseReviewItem.ReviewedItem

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<UserCourseReviewItem> =
        ViewHolder(createView(parent, R.layout.item_user_review_reviewed))

    private class ViewHolder(override val containerView: View) : DelegateViewHolder<UserCourseReviewItem>(containerView), LayoutContainer {
        private val reviewIconWrapper = userReviewIcon.wrapWithGlide()

        override fun onBind(data: UserCourseReviewItem) {
            data as UserCourseReviewItem.ReviewedItem
            userReviewCourseTitle.text = data.course.title
            userReviewText.text = data.courseReview.text
            reviewIconWrapper.setImagePath(data.course.cover ?: "", AppCompatResources.getDrawable(context, R.drawable.general_placeholder))
            userReviewTime.text = DateMapper.mapToRelativeDate(context, DateTimeHelper.nowUtc(), data.courseReview.updateDate?.time ?: 0)
            userReviewRating.progress = data.courseReview.score
            userReviewRating.total = 5
        }
    }
}