package org.stepik.android.view.user_reviews.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemUserReviewPotentialHeaderBinding
import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class UserReviewsPotentialHeaderAdapterDelegate : AdapterDelegate<UserCourseReviewItem, DelegateViewHolder<UserCourseReviewItem>>() {
    override fun isForViewType(position: Int, data: UserCourseReviewItem): Boolean =
        data is UserCourseReviewItem.PotentialReviewHeader

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<UserCourseReviewItem> =
        ViewHolder(createView(parent, R.layout.item_user_review_potential_header))

    private class ViewHolder(root: View) : DelegateViewHolder<UserCourseReviewItem>(root) {
        private val viewBinding: ItemUserReviewPotentialHeaderBinding by viewBinding { ItemUserReviewPotentialHeaderBinding.bind(root) }

        override fun onBind(data: UserCourseReviewItem) {
            data as UserCourseReviewItem.PotentialReviewHeader
            viewBinding.potentialReviewText.text = context.getString(
                R.string.user_review_potential_review_header,
                context.resources.getQuantityString(
                    R.plurals.potential_review,
                    data.potentialReviewCount,
                    data.potentialReviewCount
                )
            )
        }
    }
}