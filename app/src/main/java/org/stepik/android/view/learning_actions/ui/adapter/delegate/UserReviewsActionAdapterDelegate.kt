package org.stepik.android.view.learning_actions.ui.adapter.delegate

import android.text.SpannedString
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemLearningActionUserReviewsBinding
import org.stepik.android.presentation.user_reviews.UserReviewsFeature
import org.stepik.android.view.learning_actions.model.LearningActionsItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate

class UserReviewsActionAdapterDelegate(private val onClick: () -> Unit) : AdapterDelegate<LearningActionsItem, DelegateViewHolder<LearningActionsItem>>() {
    override fun isForViewType(position: Int, data: LearningActionsItem): Boolean =
        data is LearningActionsItem.UserReviews

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<LearningActionsItem> {
        val parentWidth = parent.measuredWidth - parent.paddingLeft - parent.paddingRight
        val itemMargin = parent.resources.getDimensionPixelSize(R.dimen.course_item_margin) * 2
        val itemView = createView(parent, R.layout.item_learning_action_user_reviews)
        val itemWidth = parentWidth / 2 - itemMargin
        itemView.updateLayoutParams { width = itemWidth }
        return ViewHolder(itemView, onClick)
    }

    private class ViewHolder(
        root: View,
        private val onClick: () -> Unit
    ) : DelegateViewHolder<LearningActionsItem>(root) {
        private val viewBinding: ItemLearningActionUserReviewsBinding by viewBinding { ItemLearningActionUserReviewsBinding.bind(root) }

        private val viewStateDelegate = ViewStateDelegate<UserReviewsFeature.State>()

        init {
            viewStateDelegate.addState<UserReviewsFeature.State.Idle>()
            viewStateDelegate.addState<UserReviewsFeature.State.Loading>(viewBinding.userReviewsActionTitle, viewBinding.userReviewsActionLoadingView)
            viewStateDelegate.addState<UserReviewsFeature.State.Error>(viewBinding.userReviewsActionTitle)
            viewStateDelegate.addState<UserReviewsFeature.State.Empty>(viewBinding.userReviewsActionTitle, viewBinding.userReviewsActionCourseCount)
            viewStateDelegate.addState<UserReviewsFeature.State.Content>(viewBinding.userReviewsActionTitle, viewBinding.userReviewsActionCourseCount)
            itemView.setOnClickListener { onClick() }
        }

        override fun onBind(data: LearningActionsItem) {
            data as LearningActionsItem.UserReviews
            render(data.state)
        }

        private fun render(state: UserReviewsFeature.State) {
            viewStateDelegate.switchState(state)
            viewBinding.userReviewsPotentialIcon.isVisible = state is UserReviewsFeature.State.Content && state.userCourseReviewsResult.potentialReviewItems.isNotEmpty()
            viewBinding.userReviewsActionCourseCount.text =
                when (state) {
                    is UserReviewsFeature.State.Empty ->
                        context.getString(R.string.user_review_learning_action_empty)

                    is UserReviewsFeature.State.Content ->
                        resolveActionTitle(state.userCourseReviewsResult.reviewedReviewItems.size, state.userCourseReviewsResult.potentialReviewItems.size)

                    else ->
                        ""
                }
        }

        private fun resolveActionTitle(userReviewsCount: Int, userReviewsPotentialCount: Int): SpannedString =
            buildSpannedString {
                if (userReviewsCount > 0) {
                    append(context.resources.getQuantityString(R.plurals.learning_action_review, userReviewsCount, userReviewsCount))
                } else {
                    append(context.getString(R.string.user_review_learning_action_empty))
                }
                if (userReviewsPotentialCount > 0) {
                    append(" ")
                    color(ContextCompat.getColor(context, R.color.color_overlay_green)) {
                        append(context.getString(R.string.user_review_potential_learning_action_count, userReviewsPotentialCount))
                    }
                }
            }
    }
}