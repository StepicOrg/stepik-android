package org.stepik.android.view.course_list.ui.adapter.delegate

import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_course.view.courseItemImage
import kotlinx.android.synthetic.main.item_course.view.courseItemName
import kotlinx.android.synthetic.main.item_visited_course.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepik.android.domain.course.analytic.CourseCardSeenAnalyticEvent
import org.stepik.android.domain.course.analytic.batch.CourseCardSeenAnalyticBatchEvent
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course_list.model.CourseListItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class VisitedCourseListItemAdapterDelegate(
    private val analytic: Analytic,
    private val onItemClicked: (CourseListItem.Data) -> Unit,
    private val isHandleInAppPurchase: Boolean
) : AdapterDelegate<CourseListItem, DelegateViewHolder<CourseListItem>>() {
    override fun isForViewType(position: Int, data: CourseListItem): Boolean =
        data is CourseListItem.Data

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseListItem> {
        val itemView = createView(parent, R.layout.item_visited_course)
        val itemCount = (parent.width - parent.paddingLeft - parent.paddingRight) /
                (itemView.resources.getDimensionPixelSize(R.dimen.visited_course_item_width) + itemView.resources.getDimensionPixelSize(R.dimen.course_item_margin) * 2)

        itemView.layoutParams.apply {
            width = ((parent.width - parent.paddingLeft - parent.paddingRight) / itemCount) - itemView.resources.getDimensionPixelSize(R.dimen.course_item_margin) * 2
        }
        return ViewHolder(itemView)
    }

    private inner class ViewHolder(root: View) : DelegateViewHolder<CourseListItem>(root) {
        private val courseCoverImageTarget =
            RoundedBitmapImageViewTarget(context.resources.getDimension(R.dimen.corner_radius), root.courseItemImage)

        private val coursePlaceholder = BitmapFactory
            .decodeResource(context.resources, R.drawable.general_placeholder)
            .let { bitmap ->
                RoundedBitmapDrawableFactory
                    .create(context.resources, bitmap)
                    .apply {
                        cornerRadius = context.resources.getDimension(R.dimen.corner_radius)
                    }
            }

        private val courseItemName = root.courseItemName
        private val courseItemPrice = root.coursePrice

        init {
            root.setOnClickListener { (itemData as? CourseListItem.Data)?.let(onItemClicked) }
        }

        override fun onBind(data: CourseListItem) {
            data as CourseListItem.Data

            Glide
                .with(context)
                .asBitmap()
                .load(data.course.cover)
                .placeholder(coursePlaceholder)
                .fitCenter()
                .into(courseCoverImageTarget)

            courseItemName.text = data.course.title
            val isEnrolled = data.course.enrollment != 0L

            val (@ColorRes textColor, displayPrice) = when {
                isEnrolled -> {
                    R.color.material_on_surface_disabled to context.resources.getString(R.string.visited_courses_enrolled)
                }
                data.course.isPaid -> {
                    R.color.color_overlay_violet to handleCoursePrice(data)
                }
                else ->
                    R.color.color_overlay_green to context.resources.getString(R.string.course_list_free)
            }

            courseItemPrice.setTextColor(ContextCompat.getColor(context, textColor))
            courseItemPrice.text = displayPrice

            analytic.report(CourseCardSeenAnalyticEvent(data.course.id, data.source))
            analytic.report(CourseCardSeenAnalyticBatchEvent(data.course.id, data.source))
        }
    }

    private fun handleCoursePrice(data: CourseListItem.Data) =
        if (isHandleInAppPurchase && data.course.priceTier != null) {
            (data.courseStats.enrollmentState as? EnrollmentState.NotEnrolledInApp)?.skuWrapper?.sku?.price ?: data.course.displayPrice
        } else {
            data.course.displayPrice
        }
}