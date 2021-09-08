package org.stepik.android.view.user_reviews.ui.adapter.delegate

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_user_review_reviewed.*
import kotlinx.android.synthetic.main.item_user_review_reviewed.userReviewCourseTitle
import kotlinx.android.synthetic.main.item_user_review_reviewed.userReviewIcon
import kotlinx.android.synthetic.main.item_user_review_reviewed.userReviewRating
import org.stepic.droid.R
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.resolveColorAttribute
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem
import org.stepik.android.model.Course
import org.stepik.android.view.base.ui.mapper.DateMapper
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class UserReviewsReviewedAdapterDelegate(
    private val onCourseTitleClicked: (Course) -> Unit,
    private val onEditReviewClicked: (CourseReview) -> Unit,
    private val onRemoveReviewClicked: (CourseReview) -> Unit
) : AdapterDelegate<UserCourseReviewItem, DelegateViewHolder<UserCourseReviewItem>>() {
    override fun isForViewType(position: Int, data: UserCourseReviewItem): Boolean =
        data is UserCourseReviewItem.ReviewedItem

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<UserCourseReviewItem> =
        ViewHolder(createView(parent, R.layout.item_user_review_reviewed))

    private inner class ViewHolder(override val containerView: View) : DelegateViewHolder<UserCourseReviewItem>(containerView), LayoutContainer {
        init {
            userReviewIcon.setOnClickListener { (itemData as? UserCourseReviewItem.ReviewedItem)?.course?.let(onCourseTitleClicked) }
            userReviewCourseTitle.setOnClickListener { (itemData as? UserCourseReviewItem.ReviewedItem)?.course?.let(onCourseTitleClicked) }
            userReviewMenu.setOnClickListener(::showReviewMenu)
        }

        override fun onBind(data: UserCourseReviewItem) {
            data as UserCourseReviewItem.ReviewedItem
            userReviewCourseTitle.text = data.course.title
            userReviewText.text = data.courseReview.text

            Glide
                .with(context)
                .asBitmap()
                .load(data.course.cover)
                .placeholder(R.drawable.general_placeholder)
                .fitCenter()
                .into(userReviewIcon)

            userReviewTime.text = DateMapper.mapToRelativeDate(context, DateTimeHelper.nowUtc(), data.courseReview.updateDate?.time ?: 0)
            userReviewRating.progress = data.courseReview.score
            userReviewRating.total = 5
        }

        private fun showReviewMenu(view: View) {
            val courseReview = (itemData as? UserCourseReviewItem.ReviewedItem)
                ?.courseReview
                ?: return

            val popupMenu = PopupMenu(context, view)
            popupMenu.inflate(R.menu.course_review_menu)

            popupMenu
                .menu
                .findItem(R.id.course_review_menu_remove)
                ?.let { menuItem ->
                    val title = SpannableString(menuItem.title)
                    title.setSpan(ForegroundColorSpan(context.resolveColorAttribute(R.attr.colorError)), 0, title.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    menuItem.title = title
                }

            popupMenu
                .setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.course_review_menu_edit ->
                            onEditReviewClicked(courseReview)

                        R.id.course_review_menu_remove ->
                            onRemoveReviewClicked(courseReview)
                    }
                    true
                }

            popupMenu.show()
        }
    }
}