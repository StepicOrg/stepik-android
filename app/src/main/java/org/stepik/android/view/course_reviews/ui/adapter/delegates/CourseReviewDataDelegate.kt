package org.stepik.android.view.course_reviews.ui.adapter.delegates

import android.graphics.BitmapFactory
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_course_reviews_item.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.course_reviews.model.CourseReviewItem
import org.stepik.android.model.user.User
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate
import ru.nobird.android.ui.adapterdelegatessupport.DelegateViewHolder
import java.util.Date
import java.util.TimeZone

class CourseReviewDataDelegate(
    private val onUserClicked: (User) -> Unit,
    private val onEditReviewClicked: (CourseReview) -> Unit,
    private val onRemoveReviewClicked: (CourseReview) -> Unit
) : AdapterDelegate<CourseReviewItem, DelegateViewHolder<CourseReviewItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseReviewItem> =
        ViewHolder(createView(parent, R.layout.view_course_reviews_item))

    override fun isForViewType(position: Int, data: CourseReviewItem): Boolean =
        data is CourseReviewItem.Data

    private inner class ViewHolder(root: View) : DelegateViewHolder<CourseReviewItem>(root) {
        private val reviewIcon = root.reviewIcon
        private val reviewDate = root.reviewDate
        private val reviewName = root.reviewName
        private val reviewRating = root.reviewRating
        private val reviewText = root.reviewText
        private val reviewMenu = root.reviewMenu
        private val reviewMark = root.reviewMark

        private val reviewIconTarget = RoundedBitmapImageViewTarget(
            context.resources.getDimension(R.dimen.course_image_radius), reviewIcon)

        private val reviewIconPlaceholder = with(context.resources) {
            val coursePlaceholderBitmap = BitmapFactory.decodeResource(this, R.drawable.general_placeholder)
            val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(this, coursePlaceholderBitmap)
            circularBitmapDrawable.cornerRadius = getDimension(R.dimen.course_image_radius)
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

            reviewDate.text = DateTimeHelper
                .getPrintableDate(data.courseReview.updateDate ?: Date(), DateTimeHelper.DISPLAY_DATETIME_PATTERN, TimeZone.getDefault())

            reviewRating.progress = data.courseReview.score
            reviewRating.total = 5

            reviewText.text = data.courseReview.text

            reviewMenu.changeVisibility(data.isCurrentUserReview)
            reviewMark.changeVisibility(data.isCurrentUserReview)
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
                    title.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.new_red_color)), 0, title.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
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