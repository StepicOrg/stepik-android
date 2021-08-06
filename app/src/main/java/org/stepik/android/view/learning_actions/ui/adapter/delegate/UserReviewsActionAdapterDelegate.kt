package org.stepik.android.view.learning_actions.ui.adapter.delegate

import android.text.SpannedString
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_learning_action_user_reviews.*
import org.stepic.droid.R
import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem
import org.stepik.android.presentation.user_reviews.UserReviewsFeature
import org.stepik.android.view.learning_actions.model.LearningActionsItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate

class UserReviewsActionAdapterDelegate : AdapterDelegate<LearningActionsItem, DelegateViewHolder<LearningActionsItem>>() {
    override fun isForViewType(position: Int, data: LearningActionsItem): Boolean =
        data is LearningActionsItem.UserReviews

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<LearningActionsItem> {
        val parentWidth = parent.measuredWidth - parent.paddingLeft - parent.paddingRight
        val itemMargin = parent.resources.getDimensionPixelSize(R.dimen.course_item_margin) * 2
        val itemView = createView(parent, R.layout.item_learning_action_user_reviews)
        val itemWidth = parentWidth / 2 - itemMargin
        itemView.updateLayoutParams { width = itemWidth }
        return ViewHolder(itemView)
    }

    private class ViewHolder(override val containerView: View) : DelegateViewHolder<LearningActionsItem>(containerView), LayoutContainer {
        private val viewStateDelegate = ViewStateDelegate<UserReviewsFeature.State>()

        init {
            viewStateDelegate.addState<UserReviewsFeature.State.Idle>()
            viewStateDelegate.addState<UserReviewsFeature.State.Loading>(userReviewsActionTitle, userReviewsActionLoadingView)
            viewStateDelegate.addState<UserReviewsFeature.State.Error>(userReviewsActionTitle)
            viewStateDelegate.addState<UserReviewsFeature.State.Content>(userReviewsActionTitle, userReviewsActionCourseCount)
            containerView.setOnClickListener {  }
        }

        override fun onBind(data: LearningActionsItem) {
            data as LearningActionsItem.UserReviews
            render(data.state)
        }

        private fun render(state: UserReviewsFeature.State) {
            viewStateDelegate.switchState(state)
            userReviewsActionCourseCount.text =
                when (state) {
                    is UserReviewsFeature.State.Content -> {
                        val reviewedHeader = state.userCourseReviewItems.find { it is UserCourseReviewItem.ReviewedHeader }
                        val reviewedCount = (reviewedHeader as? UserCourseReviewItem.ReviewedHeader)?.reviewedCount ?: 0
                        val potentialHeader = state.userCourseReviewItems.find { it is UserCourseReviewItem.PotentialReviewHeader }
                        val potentialCount = (potentialHeader as? UserCourseReviewItem.PotentialReviewHeader)?.potentialReviewCount ?: 0

                        userReviewsPotentialIcon.isVisible = potentialCount != 0

                        if (reviewedCount == 0 && potentialCount == 0) {
                            context.getString(R.string.user_review_learning_action_empty)
                        } else {
                            resolveActionTitle(reviewedCount, potentialCount)
                        }
                    }
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