package org.stepik.android.view.user_reviews.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_user_review_reviewed_header.*
import org.stepic.droid.R
import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class UserReviewsReviewedHeaderAdapterDelegate : AdapterDelegate<UserCourseReviewItem, DelegateViewHolder<UserCourseReviewItem>>() {
    override fun isForViewType(position: Int, data: UserCourseReviewItem): Boolean =
        data is UserCourseReviewItem.ReviewedHeader

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<UserCourseReviewItem> =
        ViewHolder(createView(parent, R.layout.item_user_review_reviewed_header))

    private class ViewHolder(override val containerView: View) : DelegateViewHolder<UserCourseReviewItem>(containerView), LayoutContainer {
        override fun onBind(data: UserCourseReviewItem) {
            data as UserCourseReviewItem.ReviewedHeader
            containerView.isVisible = data.reviewedCount != 0
            reviewedText.text = context.resources.getQuantityString(R.plurals.learning_action_review, data.reviewedCount, data.reviewedCount)
        }
    }
}