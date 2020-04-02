package org.stepik.android.view.course_reviews.ui.adapter.delegates

import android.graphics.BitmapFactory
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_course_reviews_item.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.resolveColorAttribute
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.course_reviews.model.CourseReviewItem
import org.stepik.android.model.user.User
import org.stepik.android.view.base.ui.mapper.DateMapper
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseReviewDataDelegate(
    private val onUserClicked: (User) -> Unit,
    private val onEditReviewClicked: (CourseReview) -> Unit,
    private val onRemoveReviewClicked: (CourseReview) -> Unit
) : AdapterDelegate<CourseReviewItem, DelegateViewHolder<CourseReviewItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseReviewItem> =
        ViewHolder(createView(parent, R.layout.view_course_reviews_item))

    override fun isForViewType(position: Int, data: CourseReviewItem): Boolean =
        data is CourseReviewItem.Data

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseReviewItem>(root) {
        private val reviewIcon = root.reviewIcon
        private val reviewDate = root.reviewDate
        private val reviewName = root.reviewName
        private val reviewRating = root.reviewRating
        private val reviewText = root.reviewText
        private val reviewMenu = root.reviewMenu
        private val reviewMark = root.reviewMark

        private val reviewIconTarget = RoundedBitmapImageViewTarget(
            context.resources.getDimension(R.dimen.corner_radius), reviewIcon)

        private val reviewIconPlaceholder = with(context.resources) {
            val coursePlaceholderBitmap = BitmapFactory.decodeResource(this, R.drawable.general_placeholder)
            val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(this, coursePlaceholderBitmap)
            circularBitmapDrawable.cornerRadius = getDimension(R.dimen.corner_radius)
            circularBitmapDrawable
        }

        init {
            val userClickListener = View.OnClickListener { (itemData as? CourseReviewItem.Data)?.user?.let(onUserClicked) }
            reviewIcon.setOnClickListener(userClickListener)
            reviewName.setOnClickListener(userClickListener)
            reviewDate.setOnClickListener(userClickListener)

            reviewMenu.setOnClickListener(::showReviewMenu)
        }

        override fun onBind(data: CourseReviewItem) {
            data as CourseReviewItem.Data

            Glide.with(context)
                .asBitmap()
                .load(data.user.avatar ?: "")
                .placeholder(reviewIconPlaceholder)
                .centerCrop()
                .into(reviewIconTarget)

            reviewName.text = data.user.fullName

            reviewDate.text = DateMapper.mapToRelativeDate(context, DateTimeHelper.nowUtc(), data.courseReview.updateDate?.time ?: 0)

            reviewRating.progress = data.courseReview.score
            reviewRating.total = 5

            reviewText.text = data.courseReview.text

            reviewMenu.isVisible = data.isCurrentUserReview
            reviewMark.isVisible = data.isCurrentUserReview
        }

        private fun showReviewMenu(view: View) {
            val courseReview = (itemData as? CourseReviewItem.Data)
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