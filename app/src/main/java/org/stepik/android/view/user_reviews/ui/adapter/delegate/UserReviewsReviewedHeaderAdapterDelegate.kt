package org.stepik.android.view.user_reviews.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemUserReviewReviewedHeaderBinding
import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class UserReviewsReviewedHeaderAdapterDelegate : AdapterDelegate<UserCourseReviewItem, DelegateViewHolder<UserCourseReviewItem>>() {
    override fun isForViewType(position: Int, data: UserCourseReviewItem): Boolean =
        data is UserCourseReviewItem.ReviewedHeader

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<UserCourseReviewItem> =
        ViewHolder(createView(parent, R.layout.item_user_review_reviewed_header))

    private class ViewHolder(root: View) : DelegateViewHolder<UserCourseReviewItem>(root) {
        private val viewBinding: ItemUserReviewReviewedHeaderBinding by viewBinding { ItemUserReviewReviewedHeaderBinding.bind(root) }

        override fun onBind(data: UserCourseReviewItem) {
            data as UserCourseReviewItem.ReviewedHeader
            viewBinding.reviewedText.text = context.resources.getQuantityString(R.plurals.learning_action_review, data.reviewedCount, data.reviewedCount)
        }
    }
}
