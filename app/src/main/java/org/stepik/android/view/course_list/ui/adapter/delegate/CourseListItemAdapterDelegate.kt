package org.stepik.android.view.course_list.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.strikeThrough
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_course.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.ui.util.doOnGlobalLayout
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.course.analytic.CourseCardSeenAnalyticEvent
import org.stepik.android.domain.course.analytic.batch.CourseCardSeenAnalyticBatchEvent
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_payments.mapper.DefaultPromoCodeMapper
import org.stepik.android.domain.course_payments.model.DefaultPromoCode
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import org.stepik.android.view.course_list.ui.delegate.CoursePropertiesDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseListItemAdapterDelegate(
    private val analytic: Analytic,
    private val onItemClicked: (CourseListItem.Data) -> Unit,
    private val onContinueCourseClicked: (CourseListItem.Data) -> Unit,
    private val isHandleInAppPurchase: Boolean,
    private val defaultPromoCodeMapper: DefaultPromoCodeMapper,
    private val displayPriceMapper: DisplayPriceMapper,
    private val isNeedExtraMargin: Boolean = false
) : AdapterDelegate<CourseListItem, DelegateViewHolder<CourseListItem>>() {
    override fun isForViewType(position: Int, data: CourseListItem): Boolean =
        data is CourseListItem.Data

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseListItem> {
        val view = createView(parent, R.layout.item_course)
        if (isNeedExtraMargin) {
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = view.resources.getDimensionPixelOffset(R.dimen.course_list_padding)
                rightMargin = view.resources.getDimensionPixelOffset(R.dimen.course_list_padding)
            }
        }
        return ViewHolder(view)
    }

    private inner class ViewHolder(root: View) : DelegateViewHolder<CourseListItem>(root) {
        private val coursePropertiesDelegate = CoursePropertiesDelegate(root, root.coursePropertiesContainer as ViewGroup)
        private val courseItemImage = root.courseItemImage
        private val courseItemName = root.courseItemName
        private val adaptiveCourseMarker = root.adaptiveCourseMarker
        private val courseContinueButton = root.courseContinueButton
        private val courseDescription = root.courseDescription
        private val courseButtonSeparator = root.courseButtonSeparator
        private val courseOldPrice = root.courseOldPrice
        private val coursePrice = root.coursePrice

        init {
            root.setOnClickListener { (itemData as? CourseListItem.Data)?.let(onItemClicked) }
            courseContinueButton.setOnClickListener { (itemData as? CourseListItem.Data)?.let(onContinueCourseClicked) }
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
            courseContinueButton.isVisible = isEnrolled
            courseButtonSeparator.isVisible = isEnrolled
            courseDescription.isVisible = !isEnrolled
            if (!isEnrolled) {
                courseDescription.text = HtmlCompat.fromHtml(data.course.summary ?: "", HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
                courseDescription.doOnGlobalLayout { it.post { it.maxLines = it.height / it.lineHeight } }
            }

            coursePrice.isVisible = !isEnrolled
            val (@ColorRes textColor, displayPrice) = if (data.course.isPaid) {
                handleCoursePrice(data, defaultPromoCode)
            } else {
                R.color.color_overlay_green to context.resources.getString(R.string.course_list_free)
            }

            coursePrice.setTextColor(ContextCompat.getColor(context, textColor))
            coursePrice.text = displayPrice
            courseOldPrice.isVisible = mustShowDefaultPromoCode && !isEnrolled
            courseOldPrice.text = buildSpannedString {
                strikeThrough {
                    append(data.course.displayPrice ?: "")
                }
            }

            adaptiveCourseMarker.isVisible = data.isAdaptive

            coursePropertiesDelegate.setStats(data)

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