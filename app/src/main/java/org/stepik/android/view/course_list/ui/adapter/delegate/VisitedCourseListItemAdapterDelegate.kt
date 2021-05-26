package org.stepik.android.view.course_list.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.text.buildSpannedString
import androidx.core.text.strikeThrough
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_course.view.courseItemImage
import kotlinx.android.synthetic.main.item_course.view.courseItemName
import kotlinx.android.synthetic.main.item_visited_course.view.*
import kotlinx.android.synthetic.main.item_visited_course.view.coursePrice
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.course.analytic.CourseCardSeenAnalyticEvent
import org.stepik.android.domain.course.analytic.batch.CourseCardSeenAnalyticBatchEvent
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_payments.mapper.DefaultPromoCodeMapper
import org.stepik.android.domain.course_payments.model.DefaultPromoCode
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class VisitedCourseListItemAdapterDelegate(
    private val analytic: Analytic,
    private val onItemClicked: (CourseListItem.Data) -> Unit,
    private val isHandleInAppPurchase: Boolean,
    private val defaultPromoCodeMapper: DefaultPromoCodeMapper,
    private val displayPriceMapper: DisplayPriceMapper
) : AdapterDelegate<CourseListItem, DelegateViewHolder<CourseListItem>>() {
    override fun isForViewType(position: Int, data: CourseListItem): Boolean =
        data is CourseListItem.Data

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseListItem> {
        val parentWidth = parent.measuredWidth - parent.paddingLeft - parent.paddingRight
        val itemMargin = parent.resources.getDimensionPixelSize(R.dimen.course_item_margin) * 2
        val itemView = createView(parent, R.layout.item_visited_course)
        val itemCount = parentWidth / (itemView.resources.getDimensionPixelSize(R.dimen.visited_course_item_width) + itemMargin)

        val itemWidth = parentWidth / itemCount - itemMargin
        itemView.updateLayoutParams { width = itemWidth }
        return ViewHolder(itemView)
    }

    private inner class ViewHolder(root: View) : DelegateViewHolder<CourseListItem>(root) {
        private val courseItemImage = root.courseItemImage
        private val courseItemName = root.courseItemName
        private val courseItemOldPrice = root.courseOldPrice
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
                .placeholder(R.drawable.general_placeholder)
                .fitCenter()
                .into(courseItemImage)

            courseItemName.text = data.course.title

            val defaultPromoCode = defaultPromoCodeMapper.mapToDefaultPromoCode(data.course)
            val mustShowDefaultPromoCode = defaultPromoCode != DefaultPromoCode.EMPTY &&
                    (defaultPromoCode.defaultPromoCodeExpireDate == null || defaultPromoCode.defaultPromoCodeExpireDate.time > DateTimeHelper.nowUtc())

            val isEnrolled = data.course.enrollment != 0L

            val (@ColorRes textColor, displayPrice) =
                when {
                    isEnrolled ->
                        R.color.material_on_surface_disabled to context.resources.getString(R.string.visited_courses_enrolled)

                    data.course.isPaid ->
                        handleCoursePrice(data, defaultPromoCode)

                    else ->
                        R.color.color_overlay_green to context.resources.getString(R.string.course_list_free)
                }

            courseItemPrice.setTextColor(AppCompatResources.getColorStateList(context, textColor))
            courseItemPrice.text = displayPrice

            courseItemOldPrice.isVisible = mustShowDefaultPromoCode && !isEnrolled
            courseItemOldPrice.text = buildSpannedString {
                strikeThrough {
                    append(data.course.displayPrice ?: "")
                }
            }

            analytic.report(CourseCardSeenAnalyticEvent(data.course.id, data.source))
            analytic.report(CourseCardSeenAnalyticBatchEvent(data.course.id, data.source))
        }
    }

    private fun handleCoursePrice(data: CourseListItem.Data, defaultPromoCode: DefaultPromoCode): Pair<Int, String?> =
        when {
            isHandleInAppPurchase && data.course.priceTier != null ->
                R.color.color_overlay_violet to ((data.courseStats.enrollmentState as? EnrollmentState.NotEnrolledInApp)?.skuWrapper?.sku?.price ?: data.course.displayPrice)
            defaultPromoCode != DefaultPromoCode.EMPTY && (defaultPromoCode.defaultPromoCodeExpireDate == null || defaultPromoCode.defaultPromoCodeExpireDate.time > DateTimeHelper.nowUtc()) ->
                R.color.color_overlay_red to displayPriceMapper.mapToDisplayPrice(data.course.currencyCode ?: "", defaultPromoCode.defaultPromoCodePrice)
            else ->
                R.color.color_overlay_violet to data.course.displayPrice
        }
}