package org.stepik.android.view.user_reviews.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_user_review_potential.*
import org.stepic.droid.R
import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class UserReviewsPotentialAdapterDelegate : AdapterDelegate<UserCourseReviewItem, DelegateViewHolder<UserCourseReviewItem>>() {
    override fun isForViewType(position: Int, data: UserCourseReviewItem): Boolean =
        data is UserCourseReviewItem.PotentialReviewItem

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<UserCourseReviewItem> =
        ViewHolder(createView(parent, R.layout.item_user_review_potential))

    private class ViewHolder(override val containerView: View) : DelegateViewHolder<UserCourseReviewItem>(containerView), LayoutContainer {

        init {
            containerView.setOnClickListener {  }
        }

//        private val reviewIconWrapper = userReviewIcon.wrapWithGlide()

        override fun onBind(data: UserCourseReviewItem) {
            data as UserCourseReviewItem.PotentialReviewItem
            userReviewCourseTitle.text = data.course.title

            // TODO Decide what to do with reviewIconWrapper
            Glide
                .with(context)
                .asBitmap()
                .load(data.course.cover)
                .placeholder(R.drawable.general_placeholder)
                .fitCenter()
                .into(userReviewIcon)

//            reviewIconWrapper.setImagePath(data.course.cover ?: "", AppCompatResources.getDrawable(context, R.drawable.ic_skip_previous_48dp))
            userReviewRating.total = 5
        }
    }
}