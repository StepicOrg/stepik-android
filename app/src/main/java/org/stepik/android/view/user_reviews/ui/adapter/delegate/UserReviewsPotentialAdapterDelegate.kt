package org.stepik.android.view.user_reviews.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_user_review_potential.*
import org.stepic.droid.R
import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem
import org.stepik.android.model.Course
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class UserReviewsPotentialAdapterDelegate(
    private val onCourseTitleClicked: (Course) -> Unit,
    private val onWriteReviewClicked: (Long, String, Float) -> Unit
) : AdapterDelegate<UserCourseReviewItem, DelegateViewHolder<UserCourseReviewItem>>() {
    companion object {
        private const val RATING_RESET_DELAY_MS = 750L
    }

    override fun isForViewType(position: Int, data: UserCourseReviewItem): Boolean =
        data is UserCourseReviewItem.PotentialReviewItem

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<UserCourseReviewItem> =
        ViewHolder(createView(parent, R.layout.item_user_review_potential))

    private inner class ViewHolder(override val containerView: View) : DelegateViewHolder<UserCourseReviewItem>(containerView), LayoutContainer {

        init {
            userReviewIcon.setOnClickListener { (itemData as? UserCourseReviewItem.PotentialReviewItem)?.course?.let(onCourseTitleClicked) }
            userReviewCourseTitle.setOnClickListener { (itemData as? UserCourseReviewItem.PotentialReviewItem)?.course?.let(onCourseTitleClicked) }
            userReviewRating.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                val potentialReview = (itemData as? UserCourseReviewItem.PotentialReviewItem) ?: return@setOnRatingBarChangeListener
                if (fromUser) {
                    onWriteReviewClicked(potentialReview.course.id, potentialReview.course.title.toString(), rating)

                    // TODO .postDelayed is not safe, it would be a good idea to replace this
                    ratingBar.postDelayed({ ratingBar.rating = 0f }, RATING_RESET_DELAY_MS)
                }
            }
            userReviewWriteAction.setOnClickListener {
                val potentialReview = (itemData as? UserCourseReviewItem.PotentialReviewItem) ?: return@setOnClickListener
                onWriteReviewClicked(potentialReview.course.id, potentialReview.course.title.toString(), -1f)
            }
        }

        override fun onBind(data: UserCourseReviewItem) {
            data as UserCourseReviewItem.PotentialReviewItem
            userReviewCourseTitle.text = data.course.title

            Glide
                .with(context)
                .asBitmap()
                .load(data.course.cover)
                .placeholder(R.drawable.general_placeholder)
                .fitCenter()
                .into(userReviewIcon)

            userReviewRating.max = 5
        }
    }
}